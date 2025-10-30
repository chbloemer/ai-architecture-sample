package de.sample.aiarchitecture.application.cart;

import org.jspecify.annotations.NonNull;

/**
 * Input model for creating a shopping cart.
 *
 * @param customerId the customer ID who owns the cart
 */
public record CreateCartInput(@NonNull String customerId) {

  /**
   * Compact constructor with validation.
   */
  public CreateCartInput {
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }
}
