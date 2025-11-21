package de.sample.aiarchitecture.product.adapter.incoming.event;

import de.sample.aiarchitecture.product.domain.event.ProductCreated;
import de.sample.aiarchitecture.product.domain.event.ProductPriceChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for Product domain events.
 *
 * <p>Demonstrates handling domain events to implement cross-cutting concerns like logging, audit
 * trails, notifications, or updating read models for CQRS.
 *
 * <p><b>Transactional Event Handling:</b>
 *
 * <p>Uses {@code @TransactionalEventListener} to ensure events are only handled after the
 * transaction commits successfully. This guarantees:
 *
 * <ul>
 *   <li>Events are only published for successful operations
 *   <li>No duplicate events if transaction is rolled back
 *   <li>Consistent state when event handlers execute
 * </ul>
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Logging domain events for audit trail
 *   <li>Triggering notifications (email, webhooks, etc.)
 *   <li>Updating denormalized read models
 *   <li>Coordinating with other bounded contexts
 * </ul>
 *
 * <p><b>Note:</b> In a production system, you might want to use {@code @Async} to handle
 * events asynchronously and avoid blocking the main transaction.
 */
@Component
public class ProductEventConsumer {

  private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);

  /**
   * Handles ProductCreated events after transaction commit.
   *
   * <p>This handler only executes after the transaction commits successfully, ensuring the
   * product was actually persisted before processing the event.
   *
   * <p>This is where you might:
   * <ul>
   *   <li>Send notification to inventory team
   *   <li>Update search index for product catalog
   *   <li>Publish event to message queue for other services
   *   <li>Create audit log entry
   * </ul>
   *
   * @param event the product created event
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onProductCreated(final ProductCreated event) {
    log.info(
        "Product created: {} (SKU: {}, Name: {}) at {}",
        event.productId().value(),
        event.sku().value(),
        event.name().value(),
        event.occurredOn());

    // Example: Update search index
    // searchIndexService.indexProduct(event.productId());

    // Example: Send notification
    // notificationService.sendProductCreatedNotification(event);

    // Example: Publish to message queue for other microservices
    // messagePublisher.publish("product.created", event);
  }

  /**
   * Handles ProductPriceChanged events after transaction commit.
   *
   * <p>This handler only executes after the transaction commits successfully, ensuring the
   * price change was actually persisted before processing the event.
   *
   * <p>This is where you might:
   * <ul>
   *   <li>Notify pricing team of price changes
   *   <li>Update price history for analytics
   *   <li>Trigger repricing of active shopping carts
   *   <li>Send price alert to customers watching this product
   * </ul>
   *
   * @param event the product price changed event
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onProductPriceChanged(final ProductPriceChanged event) {
    log.info(
        "Product price changed: {} from {} to {} at {}",
        event.productId().value(),
        event.oldPrice().value(),
        event.newPrice().value(),
        event.occurredOn());

    // Example: Store price history
    // priceHistoryService.recordPriceChange(event);

    // Example: Notify customers
    // customerNotificationService.sendPriceChangeAlert(event.productId(), event.newPrice());

    // Example: Update analytics
    // analyticsService.trackPriceChange(event);
  }
}
