package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.PaymentSelection;
import de.sample.aiarchitecture.sharedkernel.domain.marker.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that payment method was submitted for a checkout session.
 *
 * <p>This event is raised when a customer completes the payment step,
 * selecting their preferred payment method.
 */
public record PaymentSubmitted(
    @NonNull UUID eventId,
    @NonNull CheckoutSessionId sessionId,
    @NonNull String paymentProviderId,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static PaymentSubmitted now(
      @NonNull final CheckoutSessionId sessionId, @NonNull final PaymentSelection payment) {
    return new PaymentSubmitted(
        UUID.randomUUID(), sessionId, payment.providerId().value(), Instant.now(), 1);
  }
}
