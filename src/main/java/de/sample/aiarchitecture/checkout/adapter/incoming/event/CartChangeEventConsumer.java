package de.sample.aiarchitecture.checkout.adapter.incoming.event;

import de.sample.aiarchitecture.cart.domain.event.CartCleared;
import de.sample.aiarchitecture.cart.domain.event.CartItemAddedToCart;
import de.sample.aiarchitecture.cart.domain.event.CartItemQuantityChanged;
import de.sample.aiarchitecture.cart.domain.event.ProductRemovedFromCart;
import de.sample.aiarchitecture.checkout.application.synccheckoutwithcart.SyncCheckoutWithCartCommand;
import de.sample.aiarchitecture.checkout.application.synccheckoutwithcart.SyncCheckoutWithCartInputPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for cart-related domain events in the Checkout context.
 *
 * <p><b>Cross-Context Integration:</b> This listener enables eventual consistency between
 * the Cart and Checkout bounded contexts. When the cart contents change during an active
 * checkout session, the session's line items are synchronized.
 *
 * <p><b>Why Events Instead of Direct Calls?</b>
 * <ul>
 *   <li>Maintains bounded context isolation - Cart doesn't depend on Checkout internals</li>
 *   <li>Eventual consistency - Checkout session sync happens after cart transaction commits</li>
 *   <li>Loose coupling - Contexts communicate through events, not direct dependencies</li>
 * </ul>
 *
 * <p><b>Architectural Pattern:</b> This is an "incoming event adapter" in the Checkout context,
 * receiving domain events published by the Cart context.
 */
@Component
public class CartChangeEventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(CartChangeEventConsumer.class);

  private final SyncCheckoutWithCartInputPort syncCheckoutWithCartUseCase;

  public CartChangeEventConsumer(final SyncCheckoutWithCartInputPort syncCheckoutWithCartUseCase) {
    this.syncCheckoutWithCartUseCase = syncCheckoutWithCartUseCase;
  }

  /**
   * Handles CartItemAddedToCart events by syncing the checkout session.
   *
   * <p>When a customer adds an item to their cart while in checkout, the checkout
   * session's line items are updated to reflect the change.
   *
   * @param event the cart item added event
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCartItemAdded(final CartItemAddedToCart event) {
    logger.debug("Cart item added event for cart {}, syncing checkout session",
        event.cartId().value());
    syncCheckoutSession(event.cartId().value().toString());
  }

  /**
   * Handles ProductRemovedFromCart events by syncing the checkout session.
   *
   * <p>When a customer removes a product from their cart while in checkout, the checkout
   * session's line items are updated to reflect the change.
   *
   * @param event the product removed event
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onProductRemoved(final ProductRemovedFromCart event) {
    logger.debug("Product removed event for cart {}, syncing checkout session",
        event.cartId().value());
    syncCheckoutSession(event.cartId().value().toString());
  }

  /**
   * Handles CartItemQuantityChanged events by syncing the checkout session.
   *
   * <p>When a customer changes the quantity of an item in their cart while in checkout,
   * the checkout session's line items are updated to reflect the change.
   *
   * @param event the quantity changed event
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCartItemQuantityChanged(final CartItemQuantityChanged event) {
    logger.debug("Cart item quantity changed event for cart {}, syncing checkout session",
        event.cartId().value());
    syncCheckoutSession(event.cartId().value().toString());
  }

  /**
   * Handles CartCleared events by syncing the checkout session.
   *
   * <p>When a customer clears their cart while in checkout, this is logged but the
   * checkout session is not modified (empty cart cannot be synced). The user will
   * see an error when trying to continue checkout.
   *
   * @param event the cart cleared event
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCartCleared(final CartCleared event) {
    logger.warn("Cart {} was cleared during checkout - checkout session will have stale data",
        event.cartId().value());
    // Note: We don't sync here because an empty cart cannot be synced.
    // The checkout flow will handle this gracefully when the user tries to proceed.
  }

  private void syncCheckoutSession(final String cartId) {
    try {
      final var response = syncCheckoutWithCartUseCase.execute(
          new SyncCheckoutWithCartCommand(cartId));

      if (response.synced()) {
        logger.info("Checkout session {} synced with cart {} - {} items",
            response.sessionId(), cartId, response.itemCount());
      }
    } catch (Exception e) {
      logger.error("Failed to sync checkout session with cart {}: {}",
          cartId, e.getMessage(), e);
      // Swallow exception - don't fail the cart operation because of checkout sync failure
    }
  }
}
