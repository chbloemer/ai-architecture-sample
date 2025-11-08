package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CartItemId;
import de.sample.aiarchitecture.cart.domain.model.Quantity;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import de.sample.aiarchitecture.sharedkernel.domain.marker.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that the quantity of a cart item was changed.
 *
 * <p>This event is raised when a customer increases or decreases the quantity
 * of an existing item in their shopping cart.
 */
public record CartItemQuantityChanged(
    @NonNull UUID eventId,
    @NonNull CartId cartId,
    @NonNull CartItemId cartItemId,
    @NonNull ProductId productId,
    @NonNull Quantity oldQuantity,
    @NonNull Quantity newQuantity,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CartItemQuantityChanged now(
      @NonNull final CartId cartId,
      @NonNull final CartItemId cartItemId,
      @NonNull final ProductId productId,
      @NonNull final Quantity oldQuantity,
      @NonNull final Quantity newQuantity) {
    return new CartItemQuantityChanged(
        UUID.randomUUID(),
        cartId,
        cartItemId,
        productId,
        oldQuantity,
        newQuantity,
        Instant.now(),
        1);
  }
}
