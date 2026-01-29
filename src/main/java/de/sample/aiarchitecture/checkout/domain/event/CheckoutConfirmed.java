package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.marker.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a checkout was confirmed by the customer.
 *
 * <p>This event is raised when a customer reviews and confirms their order
 * from the review step.
 */
public record CheckoutConfirmed(
    @NonNull UUID eventId,
    @NonNull CheckoutSessionId sessionId,
    @NonNull Money totalAmount,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CheckoutConfirmed now(
      @NonNull final CheckoutSessionId sessionId, @NonNull final Money totalAmount) {
    return new CheckoutConfirmed(UUID.randomUUID(), sessionId, totalAmount, Instant.now(), 1);
  }
}
