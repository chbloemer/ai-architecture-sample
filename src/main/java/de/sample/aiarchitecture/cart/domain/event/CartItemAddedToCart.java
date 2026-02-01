package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.Quantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that an item was added to a shopping cart.
 */
public record CartItemAddedToCart(
    @NonNull UUID eventId,
    @NonNull CartId cartId,
    @NonNull ProductId productId,
    @NonNull Quantity quantity,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CartItemAddedToCart now(
      @NonNull final CartId cartId,
      @NonNull final ProductId productId,
      @NonNull final Quantity quantity) {
    return new CartItemAddedToCart(UUID.randomUUID(), cartId, productId, quantity, Instant.now(), 1);
  }
}
