package de.sample.aiarchitecture.cart.adapter.outgoing.event;

import de.sample.aiarchitecture.cart.domain.event.CartCleared;
import de.sample.aiarchitecture.cart.domain.event.CartItemAddedToCart;
import de.sample.aiarchitecture.cart.domain.event.CartItemQuantityChanged;
import de.sample.aiarchitecture.cart.domain.event.ProductRemovedFromCart;
import de.sample.aiarchitecture.cart.events.CartContentsChangedEvent;
import de.sample.aiarchitecture.cart.events.CartContentsChangedEvent.ChangeType;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Outgoing event adapter that translates internal cart domain events into a consolidated {@link
 * CartContentsChangedEvent} integration event for cross-context consumption.
 *
 * <p>Listens to CartItemAddedToCart, ProductRemovedFromCart, CartItemQuantityChanged, and
 * CartCleared domain events and publishes a single integration event type that other modules can
 * consume.
 */
@Component
public class CartContentsChangedEventPublisher {

  private static final Logger logger =
      LoggerFactory.getLogger(CartContentsChangedEventPublisher.class);

  private final ApplicationEventPublisher publisher;

  public CartContentsChangedEventPublisher(final ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @EventListener
  public void onItemAdded(final CartItemAddedToCart event) {
    publish(UUID.fromString(event.cartId().value()), ChangeType.ITEM_ADDED);
  }

  @EventListener
  public void onItemRemoved(final ProductRemovedFromCart event) {
    publish(UUID.fromString(event.cartId().value()), ChangeType.ITEM_REMOVED);
  }

  @EventListener
  public void onQuantityChanged(final CartItemQuantityChanged event) {
    publish(UUID.fromString(event.cartId().value()), ChangeType.QUANTITY_CHANGED);
  }

  @EventListener
  public void onCartCleared(final CartCleared event) {
    publish(UUID.fromString(event.cartId().value()), ChangeType.CART_CLEARED);
  }

  private void publish(UUID cartId, ChangeType changeType) {
    var integrationEvent = CartContentsChangedEvent.now(cartId, changeType);
    logger.debug("Publishing CartContentsChangedEvent [{}] for cart: {}", changeType, cartId);
    publisher.publishEvent(integrationEvent);
  }
}
