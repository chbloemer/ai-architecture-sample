package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that all items were removed from a shopping cart.
 *
 * <p>This event is raised when a customer clears their entire shopping cart,
 * removing all items at once.
 */
public record CartCleared(
    @NonNull UUID eventId,
    @NonNull CartId cartId,
    int itemsCleared,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CartCleared now(@NonNull final CartId cartId, final int itemsCleared) {
    return new CartCleared(UUID.randomUUID(), cartId, itemsCleared, Instant.now(), 1);
  }
}
