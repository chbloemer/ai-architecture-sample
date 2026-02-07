package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a shopping cart was abandoned by the customer.
 */
public record CartAbandoned(
    UUID eventId,
    CartId cartId,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CartAbandoned now(final CartId cartId) {
    return new CartAbandoned(UUID.randomUUID(), cartId, Instant.now(), 1);
  }
}
