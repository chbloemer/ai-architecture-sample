package de.sample.aiarchitecture.product.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Value Object representing an enriched product with pricing and stock data.
 *
 * <p>This read model combines product state from the Product aggregate with
 * external data from the Pricing and Inventory contexts. It provides a complete
 * view of a product for display purposes.
 *
 * @param productId the product identifier
 * @param sku the stock keeping unit
 * @param name the product name
 * @param description the product description
 * @param category the product category
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
   * @param article the external article data (pricing and stock)
   * @return a new EnrichedProduct instance
   */
  public static EnrichedProduct of(
      final ProductId productId,
      final String sku,
      final String name,
      final String description,
      final String category,
      final ProductArticle article) {
    return new EnrichedProduct(
        productId,
        sku,
        name,
        description,
        category,
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
}
