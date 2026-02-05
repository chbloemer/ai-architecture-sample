package de.sample.aiarchitecture.inventory.adapter.incoming.event;

import de.sample.aiarchitecture.checkout.domain.event.CheckoutConfirmed;
import de.sample.aiarchitecture.inventory.application.reducestock.ReduceStockCommand;
import de.sample.aiarchitecture.inventory.application.reducestock.ReduceStockInputPort;
import de.sample.aiarchitecture.inventory.application.reducestock.ReduceStockResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for CheckoutConfirmed integration events in the Inventory context.
 *
 * <p><b>Cross-Context Integration:</b> This listener enables eventual consistency between
 * the Checkout and Inventory bounded contexts. When a checkout is confirmed, the stock
 * is reduced for all ordered products.
 *
 * <p><b>Why Events Instead of Direct Calls?</b>
 * <ul>
 *   <li>Maintains bounded context isolation - Checkout doesn't depend on Inventory internals</li>
 *   <li>Eventual consistency - Stock reduction happens asynchronously</li>
 *   <li>Loose coupling - Contexts communicate through events, not direct dependencies</li>
 *   <li>Extensibility - Other contexts can listen to the same event</li>
 * </ul>
 *
 * <p><b>Architectural Pattern:</b> This is an "incoming event adapter" in the Inventory context,
 * receiving integration events published by the Checkout context.
 */
@Component
public class CheckoutConfirmedEventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(CheckoutConfirmedEventConsumer.class);

  private final ReduceStockInputPort reduceStockUseCase;

  public CheckoutConfirmedEventConsumer(final ReduceStockInputPort reduceStockUseCase) {
    this.reduceStockUseCase = reduceStockUseCase;
  }

  /**
   * Handles the CheckoutConfirmed integration event by reducing stock for all ordered items.
   *
   * <p>This demonstrates <b>eventual consistency</b> between bounded contexts:
   * <ol>
   *   <li>Checkout context publishes CheckoutConfirmed event</li>
   *   <li>Inventory context listens and reduces stock for each item</li>
   *   <li>If stock reduction fails for an item, it's logged but doesn't roll back the checkout</li>
   * </ol>
   *
   * @param event the checkout confirmed integration event from Checkout context
   */
  @EventListener
  public void onCheckoutConfirmed(final CheckoutConfirmed event) {
    logger.info(
        "Received CheckoutConfirmed integration event v{} for session: {}, cart: {}, {} items",
        event.version(),
        event.sessionId().value(),
        event.cartId().value(),
        event.items().size());

    int successCount = 0;
    int failureCount = 0;

    for (final CheckoutConfirmed.LineItemInfo item : event.items()) {
      try {
        final ReduceStockCommand command = new ReduceStockCommand(
            item.productId().value().toString(),
            item.quantity());

        final ReduceStockResult result = reduceStockUseCase.execute(command);

        if (result.success()) {
          successCount++;
          logger.debug(
              "Stock reduced for product {}: {} -> {}",
              item.productId().value(),
              result.previousStock(),
              result.newStock());
        } else {
          failureCount++;
          logger.warn(
              "Failed to reduce stock for product {}: {}",
              item.productId().value(),
              result.errorMessage());
        }

      } catch (Exception e) {
        failureCount++;
        logger.error(
            "Exception while reducing stock for product {}: {}",
            item.productId().value(),
            e.getMessage());
        // Continue processing other items - eventual consistency allows partial failures
      }
    }

    logger.info(
        "Stock reduction completed for checkout session {}: {} successful, {} failed",
        event.sessionId().value(),
        successCount,
        failureCount);
  }
}
