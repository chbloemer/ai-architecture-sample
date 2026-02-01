package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Domain Event indicating that a checkout was successfully completed.
 *
 * <p>This event is raised when payment has been processed and the order
 * is finalized.
 */
public record CheckoutCompleted(
    @NonNull UUID eventId,
    @NonNull CheckoutSessionId sessionId,
    @NonNull Money totalAmount,
    @Nullable String orderReference,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CheckoutCompleted now(
      @NonNull final CheckoutSessionId sessionId,
      @NonNull final Money totalAmount,
      @Nullable final String orderReference) {
    return new CheckoutCompleted(
        UUID.randomUUID(), sessionId, totalAmount, orderReference, Instant.now(), 1);
  }
}
