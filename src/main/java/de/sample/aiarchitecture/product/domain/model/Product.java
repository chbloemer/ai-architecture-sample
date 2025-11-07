package de.sample.aiarchitecture.product.domain.model;

import de.sample.aiarchitecture.product.domain.event.ProductPriceChanged;

import de.sample.aiarchitecture.sharedkernel.domain.marker.BaseAggregateRoot;
import de.sample.aiarchitecture.sharedkernel.domain.common.Price;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import org.jspecify.annotations.NonNull;

/**
 * Product Aggregate Root.
 *
 * <p>Represents a product in the e-commerce catalog with its attributes and business logic.
 * This is the root of the Product aggregate, and all modifications to the product
 * must go through this aggregate root to maintain invariants.
 *
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Price must always be positive
 *   <li>Stock cannot be negative
 *   <li>SKU must be unique across all products
 * </ul>
 *
 * <p><b>Domain Events:</b>
 * <ul>
 *   <li>{@link ProductCreated} - when a new product is created
 *   <li>{@link ProductPriceChanged} - when the product price changes
 * </ul>
 */
public final class Product extends BaseAggregateRoot<Product, ProductId> {

  private final ProductId id;
  private final SKU sku;
  private ProductName name;
  private ProductDescription description;
  private Price price;
  private Category category;
  private ProductStock stock;

  public Product(
      @NonNull final ProductId id,
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final ProductDescription description,
      @NonNull final Price price,
      @NonNull final Category category,
      @NonNull final ProductStock stock) {
    this.id = id;
    this.sku = sku;
    this.name = name;
    this.description = description;
    this.price = price;
    this.category = category;
    this.stock = stock;
  }

  @Override
  public ProductId id() {
    return id;
  }

  public SKU sku() {
    return sku;
  }

  public ProductName name() {
    return name;
  }

  public ProductDescription description() {
    return description;
  }

  public Price price() {
    return price;
  }

  public Category category() {
    return category;
  }

  public ProductStock stock() {
    return stock;
  }

  /**
   * Changes the price of this product.
   *
   * <p>Raises a {@link ProductPriceChanged} domain event.
   *
   * @param newPrice the new price (must be positive)
   * @throws IllegalArgumentException if price is null
   */
  public void changePrice(@NonNull final Price newPrice) {
    if (newPrice == null) {
      throw new IllegalArgumentException("New price cannot be null");
    }

    final Price oldPrice = this.price;
    this.price = newPrice;

    // Raise domain event
    registerEvent(ProductPriceChanged.now(this.id, oldPrice, newPrice));
  }

  /**
   * Updates the stock quantity for this product.
   *
   * @param newStock the new stock quantity
   * @throws IllegalArgumentException if stock is null
   */
  public void updateStock(@NonNull final ProductStock newStock) {
    if (newStock == null) {
      throw new IllegalArgumentException("New stock cannot be null");
    }
    this.stock = newStock;
  }

  /**
   * Increases the stock by the specified amount.
   *
   * @param amount the amount to add to stock
   * @throws IllegalArgumentException if amount is negative
   */
  public void increaseStock(final int amount) {
    this.stock = this.stock.add(amount);
  }

  /**
   * Decreases the stock by the specified amount.
   *
   * @param amount the amount to subtract from stock
   * @throws IllegalArgumentException if amount is negative or exceeds current stock
   */
  public void decreaseStock(final int amount) {
    this.stock = this.stock.subtract(amount);
  }

  /**
   * Updates the product name.
   *
   * @param newName the new name
   */
  public void updateName(@NonNull final ProductName newName) {
    if (newName == null) {
      throw new IllegalArgumentException("New name cannot be null");
    }
    this.name = newName;
  }

  /**
   * Updates the product description.
   *
   * @param newDescription the new description
   */
  public void updateDescription(@NonNull final ProductDescription newDescription) {
    if (newDescription == null) {
      throw new IllegalArgumentException("New description cannot be null");
    }
    this.description = newDescription;
  }

  /**
   * Updates the product category.
   *
   * @param newCategory the new category
   */
  public void updateCategory(@NonNull final Category newCategory) {
    if (newCategory == null) {
      throw new IllegalArgumentException("New category cannot be null");
    }
    this.category = newCategory;
  }

  /**
   * Checks if the product is available for purchase.
   *
   * @return true if the product has stock available
   */
  public boolean isAvailable() {
    return stock.isAvailable();
  }

  /**
   * Checks if the product has sufficient stock for the requested quantity.
   *
   * @param requestedQuantity the quantity to check
   * @return true if sufficient stock is available
   */
  public boolean hasStockFor(final int requestedQuantity) {
    return stock.hasStock(requestedQuantity);
  }
}
