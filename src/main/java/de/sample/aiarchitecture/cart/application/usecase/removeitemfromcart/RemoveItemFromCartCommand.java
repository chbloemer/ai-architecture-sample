package de.sample.aiarchitecture.cart.application.usecase.removeitemfromcart;

import org.jspecify.annotations.NonNull;

/**
 * Command to remove an item from a shopping cart.
 *
 * <p>This is an immutable command object that encapsulates the data needed
 * to execute the "Remove Item from Cart" use case.
 *
 * @param cartId the ID of the cart
 * @param productId the ID of the product to remove
 */
public record RemoveItemFromCartCommand(
    @NonNull String cartId,
    @NonNull String productId) {

  public RemoveItemFromCartCommand {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
  }
}
