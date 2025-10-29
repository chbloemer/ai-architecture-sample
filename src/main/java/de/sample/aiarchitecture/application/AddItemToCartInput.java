package de.sample.aiarchitecture.application;

import org.jspecify.annotations.NonNull;

/**
 * Input model for adding an item to a shopping cart.
 *
 * @param cartId the cart ID
 * @param productId the product ID to add
 * @param quantity the quantity to add
 */
public record AddItemToCartInput(
    @NonNull String cartId,
    @NonNull String productId,
    int quantity
) {

  /**
   * Compact constructor with validation.
   */
  public AddItemToCartInput {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
  }
}
