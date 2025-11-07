package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.marker.Entity;
import de.sample.aiarchitecture.sharedkernel.domain.common.Price;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import org.jspecify.annotations.NonNull;

/**
 * CartItem Entity.
 *
 * <p>Represents an item within a shopping cart. This is an entity within the ShoppingCart
 * aggregate and should only be created and modified through the ShoppingCart aggregate root.
 *
 * <p><b>Important:</b> CartItem is NOT an aggregate root. It cannot exist outside of a
 * ShoppingCart and has package-private constructors to enforce this boundary.
 */
public final class CartItem implements Entity<CartItem, CartItemId> {

  private final CartItemId id;
  private final ProductId productId; // Reference to Product by ID only
  private Quantity quantity;
  private final Price priceAtAddition; // Price snapshot when item was added

  /**
   * Package-private constructor - CartItems can only be created through ShoppingCart.
   */
  CartItem(
      @NonNull final CartItemId id,
      @NonNull final ProductId productId,
      @NonNull final Quantity quantity,
      @NonNull final Price priceAtAddition) {
    this.id = id;
    this.productId = productId;
    this.quantity = quantity;
    this.priceAtAddition = priceAtAddition;
  }

  @Override
  public CartItemId id() {
    return id;
  }

  public ProductId productId() {
    return productId;
  }

  public Quantity quantity() {
    return quantity;
  }

  public Price priceAtAddition() {
    return priceAtAddition;
  }

  /**
   * Package-private - only ShoppingCart aggregate can modify quantity.
   */
  void updateQuantity(@NonNull final Quantity newQuantity) {
    if (newQuantity == null) {
      throw new IllegalArgumentException("Quantity cannot be null");
    }
    this.quantity = newQuantity;
  }

  /**
   * Package-private - only ShoppingCart aggregate can increase quantity.
   */
  void increaseQuantity() {
    this.quantity = this.quantity.increase();
  }

  /**
   * Package-private - only ShoppingCart aggregate can decrease quantity.
   */
  void decreaseQuantity() {
    this.quantity = this.quantity.decrease();
  }
}
