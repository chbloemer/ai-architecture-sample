package de.sample.aiarchitecture.checkout.application.startcheckout;

import org.jspecify.annotations.NonNull;

/**
 * Input model for starting a checkout session.
 *
 * @param cartId the ID of the cart to checkout
 */
public record StartCheckoutCommand(@NonNull String cartId) {

  /**
   * Compact constructor with validation.
   */
  public StartCheckoutCommand {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
  }
}
