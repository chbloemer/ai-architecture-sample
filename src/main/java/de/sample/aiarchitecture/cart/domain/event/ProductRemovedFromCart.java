package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a product was removed from a shopping cart.
 *
 * <p>This event is raised when a customer removes an entire product (all quantity)
 * from their shopping cart.
 */
public record ProductRemovedFromCart(
    @NonNull UUID eventId,
    @NonNull CartId cartId,
    @NonNull ProductId productId,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static ProductRemovedFromCart now(
      @NonNull final CartId cartId,
      @NonNull final ProductId productId) {
    return new ProductRemovedFromCart(UUID.randomUUID(), cartId, productId, Instant.now(), 1);
  }
}
