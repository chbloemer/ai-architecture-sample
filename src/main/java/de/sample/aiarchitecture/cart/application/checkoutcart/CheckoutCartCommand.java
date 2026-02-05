package de.sample.aiarchitecture.cart.application.checkoutcart;

/**
 * Input model for checking out a shopping cart.
 *
 * @param cartId the cart ID to checkout
 */
public record CheckoutCartCommand(String cartId) {

  /**
   * Compact constructor with validation.
   */
  public CheckoutCartCommand {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
  }
}
