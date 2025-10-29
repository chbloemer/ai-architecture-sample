package de.sample.aiarchitecture.application.cart;

import org.jspecify.annotations.NonNull;

/**
 * Input model for checking out a shopping cart.
 *
 * @param cartId the cart ID to checkout
 */
public record CheckoutCartInput(@NonNull String cartId) {

  /**
   * Compact constructor with validation.
   */
  public CheckoutCartInput {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
  }
}
