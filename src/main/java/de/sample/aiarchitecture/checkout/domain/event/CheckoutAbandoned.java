package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutStep;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a checkout session was abandoned by the customer.
 *
 * <p>This event is raised when a customer explicitly cancels their checkout
 * or navigates away from the checkout flow.
 */
public record CheckoutAbandoned(
    @NonNull UUID eventId,
    @NonNull CheckoutSessionId sessionId,
    @NonNull CheckoutStep abandonedAtStep,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CheckoutAbandoned now(
      @NonNull final CheckoutSessionId sessionId, @NonNull final CheckoutStep abandonedAtStep) {
    return new CheckoutAbandoned(UUID.randomUUID(), sessionId, abandonedAtStep, Instant.now(), 1);
  }
}
