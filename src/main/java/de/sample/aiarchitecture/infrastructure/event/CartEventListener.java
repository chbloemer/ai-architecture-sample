package de.sample.aiarchitecture.infrastructure.event;

import de.sample.aiarchitecture.domain.model.cart.CartCheckedOut;
import de.sample.aiarchitecture.domain.model.cart.CartItemAddedToCart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for Shopping Cart domain events.
 *
 * <p>Demonstrates handling domain events to implement eventual consistency across bounded contexts.
 * For example, when a cart is checked out, we might need to:
 *
 * <ul>
 *   <li>Reserve inventory in the Product context
 *   <li>Create an order in the Order context (if we had one)
 *   <li>Trigger payment processing
 *   <li>Send confirmation email
 * </ul>
 *
 * <p><b>Transactional Event Handling:</b>
 *
 * <p>Uses {@code @TransactionalEventListener} to ensure events are only handled after the
 * transaction commits successfully. This guarantees that the cart state changes were persisted
 * before triggering downstream operations.
 *
 * <p>This is the DDD pattern of using events to coordinate between aggregates and bounded contexts
 * while maintaining loose coupling (Vaughn Vernon's Rule #4: Use eventual consistency outside the
 * boundary).
 */
@Component
public class CartEventListener {

  private static final Logger log = LoggerFactory.getLogger(CartEventListener.class);

  /**
   * Handles CartItemAddedToCart events after transaction commit.
   *
   * <p>This handler only executes after the transaction commits successfully, ensuring the
   * cart item was actually added before processing the event.
   *
   * <p>This is where you might:
   * <ul>
   *   <li>Update cart abandonment tracking
   *   <li>Trigger product recommendation engine
   *   <li>Track analytics (which products are added together)
   *   <li>Update customer's recent activity
   * </ul>
   *
   * @param event the cart item added event
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCartItemAdded(final CartItemAddedToCart event) {
    log.info(
        "Item added to cart: Product {} (Quantity: {}) added to Cart {} at {}",
        event.productId().value(),
        event.quantity().value(),
        event.cartId().value(),
        event.occurredOn());

    // Example: Track analytics
    // analyticsService.trackCartItemAdded(event);

    // Example: Update recommendations
    // recommendationService.updateBasedOnCartItem(event.customerId(), event.productId());

    // Example: Reset cart abandonment timer
    // cartAbandonmentService.resetTimer(event.cartId());
  }

  /**
   * Handles CartCheckedOut events after transaction commit.
   *
   * <p>This handler only executes after the transaction commits successfully, ensuring the
   * checkout was actually completed before triggering downstream operations.
   *
   * <p>This is the key integration point for eventual consistency across bounded contexts.
   * When a cart is checked out, we need to coordinate with other aggregates/contexts:
   *
   * <ul>
   *   <li>Reserve inventory (Product context)
   *   <li>Create order (Order context)
   *   <li>Process payment (Payment context)
   *   <li>Send confirmation (Notification context)
   * </ul>
   *
   * <p>Each of these operations happens in its own transaction, achieving eventual consistency
   * rather than strong consistency. This is the DDD recommended approach for cross-aggregate
   * operations.
   *
   * @param event the cart checked out event
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCartCheckedOut(final CartCheckedOut event) {
    log.info(
        "Cart checked out: Cart {} for Customer {} - Total: {} EUR ({} items) at {}",
        event.cartId().value(),
        event.customerId().value(),
        event.totalAmount().amount(),
        event.itemCount(),
        event.occurredOn());

    // Example: Reserve inventory for all cart items
    // This demonstrates cross-aggregate coordination via events
    // inventoryService.reserveInventory(event.cartId());

    // Example: Create order in Order bounded context
    // This demonstrates cross-context coordination
    // orderService.createOrderFromCart(event.cartId(), event.customerId(), event.totalAmount());

    // Example: Trigger payment processing
    // paymentService.initiatePayment(event.cartId(), event.totalAmount());

    // Example: Send confirmation email
    // emailService.sendOrderConfirmation(event.customerId(), event.cartId());

    // Example: Update customer analytics
    // analyticsService.trackCheckout(event);
  }
}
