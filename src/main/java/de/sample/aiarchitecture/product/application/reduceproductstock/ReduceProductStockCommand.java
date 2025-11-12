package de.sample.aiarchitecture.product.application.reduceproductstock;

import org.jspecify.annotations.NonNull;

/**
 * Command for reducing product stock quantity.
 *
 * @param productId the product ID
 * @param quantity the quantity to reduce
 */
public record ReduceProductStockCommand(
    @NonNull String productId,
    int quantity
) {

  /**
   * Compact constructor with validation.
   */
  public ReduceProductStockCommand {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
  }
}
