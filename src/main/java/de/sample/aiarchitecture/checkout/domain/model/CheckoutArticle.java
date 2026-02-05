package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Value Object representing checkout's view of article data from external contexts.
 *
 * <p>This is a domain-level value object that contains article information
 * needed during checkout operations, including pricing and availability.
 */
public record CheckoutArticle(
    ProductId productId,
    String name,
    Money currentPrice,
    int availableStock,
    boolean isAvailable)
    implements Value {

  public CheckoutArticle {
    if (productId == null) {
      throw new IllegalArgumentException("Product ID cannot be null");
    }
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    if (currentPrice == null) {
      throw new IllegalArgumentException("Current price cannot be null");
    }
    if (availableStock < 0) {
      throw new IllegalArgumentException("Available stock cannot be negative");
    }
  }

  /**
   * Checks if there is sufficient stock for the requested quantity.
   *
   * @param quantity the quantity to check
   * @return true if available stock is greater than or equal to the requested quantity
   */
  public boolean hasStockFor(final int quantity) {
    return availableStock >= quantity;
  }

  /**
   * Creates a new CheckoutArticle with the specified values.
   *
   * @param productId the product identifier
   * @param name the product name
   * @param currentPrice the current price
   * @param availableStock the available stock quantity
   * @param isAvailable whether the product is available for purchase
   * @return a new CheckoutArticle instance
   */
  public static CheckoutArticle of(
      final ProductId productId,
      final String name,
      final Money currentPrice,
      final int availableStock,
      final boolean isAvailable) {
    return new CheckoutArticle(productId, name, currentPrice, availableStock, isAvailable);
  }
}
