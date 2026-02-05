package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CartItemId;
import de.sample.aiarchitecture.cart.domain.model.Quantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that the quantity of a cart item was changed.
 *
 * <p>This event is raised when a customer increases or decreases the quantity
 * of an existing item in their shopping cart.
 */
public record CartItemQuantityChanged(
    UUID eventId,
    CartId cartId,
    CartItemId cartItemId,
    ProductId productId,
    Quantity oldQuantity,
    Quantity newQuantity,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CartItemQuantityChanged now(
      final CartId cartId,
      final CartItemId cartItemId,
      final ProductId productId,
      final Quantity oldQuantity,
      final Quantity newQuantity) {
    return new CartItemQuantityChanged(
        UUID.randomUUID(),
        cartId,
        cartItemId,
        productId,
        oldQuantity,
        newQuantity,
        Instant.now(),
        1);
  }
}
