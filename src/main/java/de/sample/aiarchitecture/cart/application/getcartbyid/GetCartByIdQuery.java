package de.sample.aiarchitecture.cart.application.getcartbyid;

/**
 * Input model for retrieving a cart by ID.
 *
 * @param cartId the cart ID
 */
public record GetCartByIdQuery(String cartId) {

  /**
   * Compact constructor with validation.
   */
  public GetCartByIdQuery {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
  }
}
