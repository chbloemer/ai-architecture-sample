package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.marker.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Integration Event indicating that a checkout was confirmed by the customer.
 *
 * <p>This event is raised when a customer reviews and confirms their order from the review step.
 * As an integration event, it is published across bounded contexts to trigger order creation
 * and payment processing in other contexts.
 *
 * <p><b>Consumers:</b>
 * <ul>
 *   <li>Order context - to create an Order from the confirmed checkout</li>
 *   <li>Payment context - to initiate payment processing</li>
 *   <li>Inventory context - to reserve stock for the order</li>
 * </ul>
 */
public record CheckoutConfirmed(
    @NonNull UUID eventId,
    @NonNull CheckoutSessionId sessionId,
    @NonNull CartId cartId,
    @NonNull CustomerId customerId,
    @NonNull Money totalAmount,
    @NonNull Instant occurredOn,
    int version)
    implements IntegrationEvent {

  public static CheckoutConfirmed now(
      @NonNull final CheckoutSessionId sessionId,
      @NonNull final CartId cartId,
      @NonNull final CustomerId customerId,
      @NonNull final Money totalAmount) {
    return new CheckoutConfirmed(
        UUID.randomUUID(), sessionId, cartId, customerId, totalAmount, Instant.now(), 1);
  }
}
