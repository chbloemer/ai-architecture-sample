package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a checkout session was started.
 *
 * <p>This event is raised when a customer begins the checkout process
 * from their shopping cart.
 */
public record CheckoutSessionStarted(
    UUID eventId,
    CheckoutSessionId sessionId,
    CartId cartId,
    CustomerId customerId,
    Money subtotal,
    int lineItemCount,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CheckoutSessionStarted now(
      final CheckoutSessionId sessionId,
      final CartId cartId,
      final CustomerId customerId,
      final Money subtotal,
      final int lineItemCount) {
    return new CheckoutSessionStarted(
        UUID.randomUUID(), sessionId, cartId, customerId, subtotal, lineItemCount, Instant.now(), 1);
  }
}
