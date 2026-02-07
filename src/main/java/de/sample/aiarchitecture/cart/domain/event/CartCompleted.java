package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a shopping cart was completed after checkout confirmation.
 */
public record CartCompleted(
    UUID eventId,
    CartId cartId,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CartCompleted now(final CartId cartId) {
    return new CartCompleted(UUID.randomUUID(), cartId, Instant.now(), 1);
  }
}
