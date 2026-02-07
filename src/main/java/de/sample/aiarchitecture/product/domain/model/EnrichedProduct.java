package de.sample.aiarchitecture.product.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Enriched Domain Model representing a product with pricing and stock data.
 *
 * <p>This domain concept combines product state from the Product aggregate with
 * external data from the Pricing and Inventory contexts. It owns business logic
 * that requires cross-context data, such as purchase eligibility and stock checks.
 *
 * <p><b>Responsibility Split:</b>
 * <ul>
 *   <li>Product aggregate: owns identity and descriptive data (mutations)</li>
 *   <li>EnrichedProduct: owns cross-context business rules (evaluation)</li>
 * </ul>
 *
 * @param productId the product identifier
 * @param sku the stock keeping unit
 * @param name the product name
 * @param description the product description
 * @param category the product category
 * @param imageUrl the product image URL
 * @param currentPrice the current price from the Pricing context
 * @param stockQuantity the available stock from the Inventory context
 * @param isAvailable whether the product is available for purchase
 */
public record EnrichedProduct(
    ProductId productId,
    String sku,
    String name,
    String description,
    String category,
    String imageUrl,
    Money currentPrice,
    int stockQuantity,
    boolean isAvailable
) implements Value {

  public EnrichedProduct {
    if (productId == null) {
      throw new IllegalArgumentException("Product ID cannot be null");
    }
    if (sku == null || sku.isBlank()) {
      throw new IllegalArgumentException("SKU cannot be null or blank");
    }
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    if (category == null || category.isBlank()) {
      throw new IllegalArgumentException("Category cannot be null or blank");
    }
    if (currentPrice == null) {
      throw new IllegalArgumentException("Current price cannot be null");
    }
  }

  /**
   * Creates an EnrichedProduct with the specified values.
   *
   * @param productId the product identifier
   * @param sku the stock keeping unit
   * @param name the product name
   * @param description the product description
   * @param category the product category
   * @param imageUrl the product image URL
   * @param article the external article data (pricing and stock)
   * @return a new EnrichedProduct instance
   */
  public static EnrichedProduct of(
      final ProductId productId,
      final String sku,
      final String name,
      final String description,
      final String category,
      final String imageUrl,
      final ProductArticle article) {
    return new EnrichedProduct(
        productId,
        sku,
        name,
        description,
        category,
        imageUrl,
        article.currentPrice(),
        article.stockQuantity(),
        article.isAvailable());
  }

  /**
   * Creates an EnrichedProduct from a Product aggregate and external article data.
   *
   * <p>This factory method combines the product's state with external pricing and
   * stock data to create an enriched read model for display.
   *
   * @param product the product aggregate
   * @param article the external article data (pricing and stock)
   * @return a new EnrichedProduct instance
   */
  public static EnrichedProduct from(final Product product, final ProductArticle article) {
    if (product == null) {
      throw new IllegalArgumentException("Product cannot be null");
    }
    if (article == null) {
      throw new IllegalArgumentException("Article data cannot be null");
    }
    return new EnrichedProduct(
        product.id(),
        product.sku().value(),
        product.name().value(),
        product.description().value(),
        product.category().name(),
        product.imageUrl().value(),
        article.currentPrice(),
        article.stockQuantity(),
        article.isAvailable());
  }

  /**
   * Checks if the product is in stock.
   *
   * @return true if stock quantity is greater than zero
   */
  public boolean isInStock() {
    return stockQuantity > 0;
  }

  /**
   * Business rule: Product can be purchased if available and in stock.
   *
   * @return true if the product can be added to cart and purchased
   */
  public boolean canPurchase() {
    return isAvailable && isInStock();
  }

  /**
   * Business rule: Check if requested quantity can be fulfilled.
   *
   * @param requestedQuantity the quantity to check
   * @return true if there is sufficient stock for the requested quantity
   */
  public boolean hasStockFor(final int requestedQuantity) {
    return stockQuantity >= requestedQuantity;
  }
}
