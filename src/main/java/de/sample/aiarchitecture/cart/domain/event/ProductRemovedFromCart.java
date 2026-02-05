package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a product was removed from a shopping cart.
 *
 * <p>This event is raised when a customer removes an entire product (all quantity)
 * from their shopping cart.
 */
public record ProductRemovedFromCart(
    UUID eventId,
    CartId cartId,
    ProductId productId,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static ProductRemovedFromCart now(
      final CartId cartId,
      final ProductId productId) {
    return new ProductRemovedFromCart(UUID.randomUUID(), cartId, productId, Instant.now(), 1);
  }
}
