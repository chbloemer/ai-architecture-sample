package de.sample.aiarchitecture.inventory.domain.model;

import de.sample.aiarchitecture.inventory.domain.event.StockDecreased;
import de.sample.aiarchitecture.inventory.domain.event.StockIncreased;
import de.sample.aiarchitecture.inventory.domain.event.StockLevelCreated;
import de.sample.aiarchitecture.inventory.domain.event.StockReserved;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.BaseAggregateRoot;
import org.jspecify.annotations.NonNull;

/**
 * StockLevel Aggregate Root.
 *
 * <p>Manages inventory stock levels for a product, tracking both available and reserved quantities.
 * Reserved stock represents inventory that has been earmarked for pending orders but not yet shipped.
 *
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Available quantity cannot be negative
 *   <li>Reserved quantity cannot exceed available quantity
 *   <li>Stock can only be decreased if sufficient quantity exists
 *   <li>Stock can only be reserved if sufficient unreserved quantity exists
 * </ul>
 *
 * <p><b>Domain Events:</b>
 * <ul>
 *   <li>{@link StockLevelCreated} - when a new stock level is created
 *   <li>{@link StockIncreased} - when stock is increased
 *   <li>{@link StockDecreased} - when stock is decreased
 *   <li>{@link StockReserved} - when stock is reserved
 * </ul>
 */
public final class StockLevel extends BaseAggregateRoot<StockLevel, StockLevelId> {

  private final StockLevelId id;
  private final ProductId productId;
  private StockQuantity availableQuantity;
  private StockQuantity reservedQuantity;

  private StockLevel(
      @NonNull final StockLevelId id,
      @NonNull final ProductId productId,
      @NonNull final StockQuantity availableQuantity,
      @NonNull final StockQuantity reservedQuantity) {
    this.id = id;
    this.productId = productId;
    this.availableQuantity = availableQuantity;
    this.reservedQuantity = reservedQuantity;
  }

  /**
   * Creates a new StockLevel for a product with an initial quantity.
   *
   * @param productId the product this stock level is for
   * @param initialQuantity the initial available quantity
   * @return a new StockLevel instance
   */
  public static StockLevel create(
      @NonNull final ProductId productId,
      final int initialQuantity) {
    if (productId == null) {
      throw new IllegalArgumentException("ProductId cannot be null");
    }

    final StockLevel stockLevel = new StockLevel(
        StockLevelId.generate(),
        productId,
        StockQuantity.of(initialQuantity),
        StockQuantity.of(0));

    stockLevel.registerEvent(StockLevelCreated.now(
        stockLevel.id,
        productId,
        stockLevel.availableQuantity));

    return stockLevel;
  }

  @Override
  public StockLevelId id() {
    return id;
  }

  public ProductId productId() {
    return productId;
  }

  public StockQuantity availableQuantity() {
    return availableQuantity;
  }

  public StockQuantity reservedQuantity() {
    return reservedQuantity;
  }

  /**
   * Increases the available stock quantity.
   *
   * <p>Use this when receiving new stock into inventory.
   *
   * @param amount the amount to add to available stock
   * @throws IllegalArgumentException if amount is negative
   */
  public void increaseStock(final int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }

    final StockQuantity addedQuantity = StockQuantity.of(amount);
    this.availableQuantity = StockQuantity.of(this.availableQuantity.value() + amount);

    registerEvent(StockIncreased.now(
        this.id,
        this.productId,
        addedQuantity,
        this.availableQuantity));
  }

  /**
   * Decreases the available stock quantity.
   *
   * <p>Use this when shipping stock out of inventory.
   *
   * @param amount the amount to subtract from available stock
   * @throws IllegalArgumentException if amount is negative or exceeds available quantity
   */
  public void decreaseStock(final int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }
    if (amount > this.availableQuantity.value()) {
      throw new IllegalArgumentException(
          "Cannot decrease stock by " + amount + ", only " + this.availableQuantity.value() + " available");
    }

    final StockQuantity removedQuantity = StockQuantity.of(amount);
    this.availableQuantity = StockQuantity.of(this.availableQuantity.value() - amount);

    // If reserved quantity now exceeds available, adjust it
    if (this.reservedQuantity.value() > this.availableQuantity.value()) {
      this.reservedQuantity = this.availableQuantity;
    }

    registerEvent(StockDecreased.now(
        this.id,
        this.productId,
        removedQuantity,
        this.availableQuantity));
  }

  /**
   * Reserves stock for a pending order.
   *
   * <p>Reserved stock remains in available quantity but is earmarked and cannot be reserved again.
   *
   * @param amount the amount to reserve
   * @throws IllegalArgumentException if amount is negative or exceeds unreserved stock
   */
  public void reserve(final int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }

    final int unreserved = this.availableQuantity.value() - this.reservedQuantity.value();
    if (amount > unreserved) {
      throw new IllegalArgumentException(
          "Cannot reserve " + amount + ", only " + unreserved + " unreserved stock available");
    }

    final StockQuantity reservedAmount = StockQuantity.of(amount);
    this.reservedQuantity = StockQuantity.of(this.reservedQuantity.value() + amount);

    registerEvent(StockReserved.now(
        this.id,
        this.productId,
        reservedAmount));
  }

  /**
   * Releases previously reserved stock.
   *
   * <p>Use this when an order is cancelled and reserved stock should be made available again.
   *
   * @param amount the amount to release from reservation
   * @throws IllegalArgumentException if amount is negative or exceeds reserved quantity
   */
  public void release(final int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }
    if (amount > this.reservedQuantity.value()) {
      throw new IllegalArgumentException(
          "Cannot release " + amount + ", only " + this.reservedQuantity.value() + " reserved");
    }

    this.reservedQuantity = StockQuantity.of(this.reservedQuantity.value() - amount);
  }

  /**
   * Checks if there is any unreserved stock available.
   *
   * @return true if available quantity exceeds reserved quantity
   */
  public boolean isAvailable() {
    return this.availableQuantity.value() > this.reservedQuantity.value();
  }
}
