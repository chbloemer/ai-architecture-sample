package de.sample.aiarchitecture.cart.application.usecase.checkoutcart;

import org.jspecify.annotations.NonNull;

/**
 * Input model for checking out a shopping cart.
 *
 * @param cartId the cart ID to checkout
 */
public record CheckoutCartCommand(@NonNull String cartId) {

  /**
   * Compact constructor with validation.
   */
  public CheckoutCartCommand {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
  }
}
