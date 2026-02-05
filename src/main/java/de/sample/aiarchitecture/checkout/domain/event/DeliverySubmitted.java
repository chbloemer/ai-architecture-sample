package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.DeliveryAddress;
import de.sample.aiarchitecture.checkout.domain.model.ShippingOption;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that delivery details were submitted for a checkout session.
 *
 * <p>This event is raised when a customer completes the delivery step,
 * providing their shipping address and selecting a shipping option.
 */
public record DeliverySubmitted(
    UUID eventId,
    CheckoutSessionId sessionId,
    String deliveryAddress,
    String shippingOptionId,
    Money shippingCost,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static DeliverySubmitted now(
      final CheckoutSessionId sessionId,
      final DeliveryAddress address,
      final ShippingOption shippingOption) {
    return new DeliverySubmitted(
        UUID.randomUUID(),
        sessionId,
        address.formattedAddress(),
        shippingOption.id(),
        shippingOption.cost(),
        Instant.now(),
        1);
  }
}
