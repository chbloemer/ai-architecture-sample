package de.sample.aiarchitecture.product.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.BaseAggregateRoot;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;

/**
 * Product Aggregate Root.
 *
 * <p>Represents a product in the e-commerce catalog with its attributes and business logic.
 * This is the root of the Product aggregate, and all modifications to the product
 * must go through this aggregate root to maintain invariants.
 *
 * <p>Product context owns identity (productId, sku) and description (name, description, category).
 *
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>SKU must be unique across all products
 * </ul>
 *
 * <p><b>Domain Events:</b>
 * <ul>
 *   <li>{@link ProductCreated} - when a new product is created
 * </ul>
 *
 * <p><b>Note:</b> Pricing is managed by the Pricing bounded context. Use PricingService
 * to get current prices for products. Stock/availability is managed by the Inventory
 * bounded context. Use InventoryService to get stock information.
 */
public final class Product extends BaseAggregateRoot<Product, ProductId> {

  private final ProductId id;
  private final SKU sku;
  private ProductName name;
  private ProductDescription description;
  private Category category;

  public Product(
      final ProductId id,
      final SKU sku,
      final ProductName name,
      final ProductDescription description,
      final Category category) {
    this.id = id;
    this.sku = sku;
    this.name = name;
    this.description = description;
    this.category = category;
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

  public Category category() {
    return category;
  }

  /**
   * Updates the product name.
   *
   * @param newName the new name
   */
  public void updateName(final ProductName newName) {
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
  public void updateDescription(final ProductDescription newDescription) {
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
  public void updateCategory(final Category newCategory) {
    if (newCategory == null) {
      throw new IllegalArgumentException("New category cannot be null");
    }
    this.category = newCategory;
  }

  /**
   * Pushes product state to an interested party through the Interest Interface pattern.
   *
   * <p>This method enables the aggregate to control what state is exposed without
   * exposing its internal structure. The interest receives state through callback
   * methods, allowing it to build read models or projections.
   *
   * @param interest the interested party that will receive state
   */
  public void provideStateTo(final ProductStateInterest interest) {
    if (interest == null) {
      throw new IllegalArgumentException("Interest cannot be null");
    }
    interest.receiveProductId(this.id);
    interest.receiveSku(this.sku.value());
    interest.receiveName(this.name.value());
    interest.receiveDescription(this.description.value());
    interest.receiveCategory(this.category.name());
  }
}
