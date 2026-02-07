package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.Quantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that an item was added to a shopping cart.
 */
public record CartItemAddedToCart(
    UUID eventId,
    CartId cartId,
    ProductId productId,
    Quantity quantity,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CartItemAddedToCart now(
      final CartId cartId,
      final ProductId productId,
      final Quantity quantity) {
    return new CartItemAddedToCart(UUID.randomUUID(), cartId, productId, quantity, Instant.now(), 1);
  }
}
