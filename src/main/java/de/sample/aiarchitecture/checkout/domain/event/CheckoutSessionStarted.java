package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.marker.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a checkout session was started.
 *
 * <p>This event is raised when a customer begins the checkout process
 * from their shopping cart.
 */
public record CheckoutSessionStarted(
    @NonNull UUID eventId,
    @NonNull CheckoutSessionId sessionId,
    @NonNull CartId cartId,
    @NonNull CustomerId customerId,
    @NonNull Money subtotal,
    int lineItemCount,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CheckoutSessionStarted now(
      @NonNull final CheckoutSessionId sessionId,
      @NonNull final CartId cartId,
      @NonNull final CustomerId customerId,
      @NonNull final Money subtotal,
      final int lineItemCount) {
    return new CheckoutSessionStarted(
        UUID.randomUUID(), sessionId, cartId, customerId, subtotal, lineItemCount, Instant.now(), 1);
  }
}
