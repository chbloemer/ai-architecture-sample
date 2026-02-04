package de.sample.aiarchitecture.inventory.domain.model;

import de.sample.aiarchitecture.inventory.domain.event.StockChanged;
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
 *   <li>{@link StockChanged} - when stock levels change (increase, decrease, reserve, release)
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

    stockLevel.registerEvent(StockChanged.now(
        stockLevel.id,
        productId,
        StockQuantity.of(0),
        stockLevel.availableQuantity,
        StockQuantity.of(0),
        stockLevel.reservedQuantity));

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

    final StockQuantity previousAvailable = this.availableQuantity;
    this.availableQuantity = StockQuantity.of(this.availableQuantity.value() + amount);

    registerEvent(StockChanged.now(
        this.id,
        this.productId,
        previousAvailable,
        this.availableQuantity,
        this.reservedQuantity,
        this.reservedQuantity));
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

    final StockQuantity previousAvailable = this.availableQuantity;
    this.availableQuantity = StockQuantity.of(this.availableQuantity.value() - amount);

    // If reserved quantity now exceeds available, adjust it
    if (this.reservedQuantity.value() > this.availableQuantity.value()) {
      final StockQuantity previousReserved = this.reservedQuantity;
      this.reservedQuantity = this.availableQuantity;

      registerEvent(StockChanged.now(
          this.id,
          this.productId,
          previousAvailable,
          this.availableQuantity,
          previousReserved,
          this.reservedQuantity));
    } else {
      registerEvent(StockChanged.now(
          this.id,
          this.productId,
          previousAvailable,
          this.availableQuantity,
          this.reservedQuantity,
          this.reservedQuantity));
    }
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

    final StockQuantity previousReserved = this.reservedQuantity;
    this.reservedQuantity = StockQuantity.of(this.reservedQuantity.value() + amount);

    registerEvent(StockChanged.now(
        this.id,
        this.productId,
        this.availableQuantity,
        this.availableQuantity,
        previousReserved,
        this.reservedQuantity));
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

    final StockQuantity previousReserved = this.reservedQuantity;
    this.reservedQuantity = StockQuantity.of(this.reservedQuantity.value() - amount);

    registerEvent(StockChanged.now(
        this.id,
        this.productId,
        this.availableQuantity,
        this.availableQuantity,
        previousReserved,
        this.reservedQuantity));
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
