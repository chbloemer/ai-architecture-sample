package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.BuyerInfo;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that buyer information was submitted for a checkout session.
 *
 * <p>This event is raised when a customer completes the buyer info step,
 * providing their contact details.
 */
public record BuyerInfoSubmitted(
    UUID eventId,
    CheckoutSessionId sessionId,
    String email,
    String firstName,
    String lastName,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static BuyerInfoSubmitted now(
      final CheckoutSessionId sessionId, final BuyerInfo buyerInfo) {
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
