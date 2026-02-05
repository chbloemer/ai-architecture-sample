package de.sample.aiarchitecture.inventory.application.reducestock;

import org.jspecify.annotations.NonNull;

/**
 * Command for reducing stock of a product.
 *
 * @param productId the product ID (UUID string)
 * @param quantity the quantity to reduce
 */
public record ReduceStockCommand(
    @NonNull String productId,
    int quantity) {

  public ReduceStockCommand {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
  }
}
