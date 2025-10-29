package de.sample.aiarchitecture.application.cart;

import org.jspecify.annotations.NonNull;

/**
 * Input model for retrieving a cart by ID.
 *
 * @param cartId the cart ID
 */
public record GetCartByIdInput(@NonNull String cartId) {

  /**
   * Compact constructor with validation.
   */
  public GetCartByIdInput {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
  }
}
