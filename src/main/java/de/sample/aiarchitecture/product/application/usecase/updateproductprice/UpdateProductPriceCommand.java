package de.sample.aiarchitecture.product.application.usecase.updateproductprice;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

/**
 * Input model for updating a product's price.
 *
 * @param productId the product ID
 * @param newPriceAmount the new price amount
 * @param newPriceCurrency the new price currency
 */
public record UpdateProductPriceCommand(
    @NonNull String productId,
    @NonNull BigDecimal newPriceAmount,
    @NonNull String newPriceCurrency
) {

  /**
   * Compact constructor with validation.
   */
  public UpdateProductPriceCommand {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
    if (newPriceAmount == null || newPriceAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price amount must be positive");
    }
    if (newPriceCurrency == null || newPriceCurrency.isBlank()) {
      throw new IllegalArgumentException("Price currency cannot be null or blank");
    }
  }
}
