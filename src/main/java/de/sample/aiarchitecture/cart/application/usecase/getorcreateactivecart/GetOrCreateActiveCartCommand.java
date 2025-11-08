package de.sample.aiarchitecture.cart.application.usecase.getorcreateactivecart;

import org.jspecify.annotations.NonNull;

/**
 * Command to get or create an active cart for a customer.
 *
 * @param customerId the customer ID
 */
public record GetOrCreateActiveCartCommand(@NonNull String customerId) {

  public GetOrCreateActiveCartCommand {
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }
}
