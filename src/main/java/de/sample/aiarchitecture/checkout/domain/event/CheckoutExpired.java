package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutStep;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a checkout session expired due to inactivity.
 *
 * <p>This event is raised when a checkout session times out without
 * being completed or explicitly abandoned.
 */
public record CheckoutExpired(
    @NonNull UUID eventId,
    @NonNull CheckoutSessionId sessionId,
    @NonNull CheckoutStep expiredAtStep,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CheckoutExpired now(
      @NonNull final CheckoutSessionId sessionId, @NonNull final CheckoutStep expiredAtStep) {
    return new CheckoutExpired(UUID.randomUUID(), sessionId, expiredAtStep, Instant.now(), 1);
  }
}
