package de.sample.aiarchitecture.inventory.adapter.incoming.event;

import de.sample.aiarchitecture.checkout.adapter.outgoing.event.CheckoutConfirmedEvent;
import de.sample.aiarchitecture.inventory.application.reducestock.ReduceStockCommand;
import de.sample.aiarchitecture.inventory.application.reducestock.ReduceStockInputPort;
import de.sample.aiarchitecture.inventory.application.reducestock.ReduceStockResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Incoming event adapter consuming {@link CheckoutConfirmedEvent} integration events from the
 * Checkout context.
 *
 * <p>When a checkout is confirmed, this consumer reduces stock for all ordered products, enabling
 * eventual consistency between the Checkout and Inventory bounded contexts.
 */
@Component
public class CheckoutConfirmedEventConsumer {

  private static final Logger logger =
      LoggerFactory.getLogger(CheckoutConfirmedEventConsumer.class);

  private final ReduceStockInputPort reduceStockUseCase;

  public CheckoutConfirmedEventConsumer(final ReduceStockInputPort reduceStockUseCase) {
    this.reduceStockUseCase = reduceStockUseCase;
  }

  /**
   * Handles the CheckoutConfirmedEvent integration event by reducing stock for all ordered items.
   *
   * @param event the checkout confirmed integration event from Checkout context
   */
  @EventListener
  public void onCheckoutConfirmed(final CheckoutConfirmedEvent event) {
    logger.info(
        "Received CheckoutConfirmedEvent v{} for session: {}, cart: {}, {} items",
        event.version(),
        event.sessionId().value(),
        event.cartId().value(),
        event.items().size());

    int successCount = 0;
    int failureCount = 0;

    for (final CheckoutConfirmedEvent.LineItemInfo item : event.items()) {
      try {
        final ReduceStockCommand command =
            new ReduceStockCommand(item.productId().value().toString(), item.quantity());

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
      }
    }

    logger.info(
        "Stock reduction completed for checkout session {}: {} successful, {} failed",
        event.sessionId().value(),
        successCount,
        failureCount);
  }
}
