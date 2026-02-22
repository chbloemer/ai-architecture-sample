package de.sample.aiarchitecture.cart.events;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when cart contents change.
 *
 * <p>Consolidates multiple internal cart domain events (CartItemAddedToCart,
 * ProductRemovedFromCart, CartItemQuantityChanged, CartCleared) into a single cross-module event.
 * The Checkout context consumes this event to sync checkout sessions instead of listening to
 * individual domain events.
 */
public record CartContentsChangedEvent(
    UUID eventId, UUID cartId, ChangeType changeType, Instant occurredOn, int version)
    implements IntegrationEvent {

  /** The type of change that occurred in the cart. */
  public enum ChangeType {
    ITEM_ADDED,
    ITEM_REMOVED,
    QUANTITY_CHANGED,
    CART_CLEARED
  }

  /** Creates a new event with the given cart ID and change type. */
  public static CartContentsChangedEvent now(UUID cartId, ChangeType changeType) {
    return new CartContentsChangedEvent(UUID.randomUUID(), cartId, changeType, Instant.now(), 1);
  }
}
