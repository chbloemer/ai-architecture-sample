package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.DeliveryAddress;
import de.sample.aiarchitecture.checkout.domain.model.ShippingOption;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.marker.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that delivery details were submitted for a checkout session.
 *
 * <p>This event is raised when a customer completes the delivery step,
 * providing their shipping address and selecting a shipping option.
 */
public record DeliverySubmitted(
    @NonNull UUID eventId,
    @NonNull CheckoutSessionId sessionId,
    @NonNull String deliveryAddress,
    @NonNull String shippingOptionId,
    @NonNull Money shippingCost,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static DeliverySubmitted now(
      @NonNull final CheckoutSessionId sessionId,
      @NonNull final DeliveryAddress address,
      @NonNull final ShippingOption shippingOption) {
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
