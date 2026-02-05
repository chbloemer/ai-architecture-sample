package de.sample.aiarchitecture.pricing.application.setproductprice;

import java.math.BigDecimal;

/**
 * Input model for setting or updating a product's price.
 *
 * @param productId the product ID
 * @param priceAmount the price amount (must be greater than zero)
 * @param priceCurrency the price currency code (e.g., "EUR", "USD")
 */
public record SetProductPriceCommand(
    String productId,
    BigDecimal priceAmount,
    String priceCurrency) {

  /**
   * Compact constructor with validation.
   */
  public SetProductPriceCommand {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
    if (priceAmount == null) {
      throw new IllegalArgumentException("Price amount cannot be null");
    }
    if (priceAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price amount must be greater than zero");
    }
    if (priceCurrency == null || priceCurrency.isBlank()) {
      throw new IllegalArgumentException("Price currency cannot be null or blank");
    }
  }
}
