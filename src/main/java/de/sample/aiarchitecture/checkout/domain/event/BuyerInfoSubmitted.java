package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.BuyerInfo;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that buyer information was submitted for a checkout session.
 *
 * <p>This event is raised when a customer completes the buyer info step,
 * providing their contact details.
 */
public record BuyerInfoSubmitted(
    @NonNull UUID eventId,
    @NonNull CheckoutSessionId sessionId,
    @NonNull String email,
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static BuyerInfoSubmitted now(
      @NonNull final CheckoutSessionId sessionId, @NonNull final BuyerInfo buyerInfo) {
    return new BuyerInfoSubmitted(
        UUID.randomUUID(),
        sessionId,
        buyerInfo.email(),
        buyerInfo.firstName(),
        buyerInfo.lastName(),
        Instant.now(),
        1);
  }
}
