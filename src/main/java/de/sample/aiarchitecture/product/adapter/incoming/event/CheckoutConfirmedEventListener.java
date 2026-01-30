package de.sample.aiarchitecture.product.adapter.incoming.event;

import de.sample.aiarchitecture.checkout.domain.event.CheckoutConfirmed;
import de.sample.aiarchitecture.product.adapter.incoming.event.acl.CheckoutEventTranslator;
import de.sample.aiarchitecture.product.application.reduceproductstock.ReduceProductStockCommand;
import de.sample.aiarchitecture.product.application.reduceproductstock.ReduceProductStockUseCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for CheckoutConfirmed integration events in the Product context.
 *
 * <p><b>Cross-Context Integration:</b> This listener enables eventual consistency between
 * the Checkout and Product bounded contexts. When a checkout is confirmed, we reduce product
 * availability for each line item in the order.
 *
 * <p><b>Anti-Corruption Layer Pattern:</b> This listener uses {@link CheckoutEventTranslator}
 * to translate Checkout context events into Product context commands. This protects Product
 * from changes in Checkout's event schema and maintains bounded context isolation.
 *
 * <p><b>Why Events Instead of Direct Calls?</b>
 *
 * <ul>
 *   <li>Maintains bounded context isolation - Checkout doesn't depend on Product
 *   <li>Eventual consistency - Stock reduction happens asynchronously
 *   <li>Loose coupling - Contexts communicate through events, not direct dependencies
 *   <li>Extensibility - Other contexts can listen to the same event
 * </ul>
 *
 * <p><b>Architectural Pattern:</b> This is an "incoming event adapter" in the Product context,
 * receiving integration events published by the Checkout context.
 *
 * @see CheckoutEventTranslator
 */
@Component
public class CheckoutConfirmedEventListener {

  private static final Logger logger = LoggerFactory.getLogger(CheckoutConfirmedEventListener.class);

  private final ReduceProductStockUseCase reduceProductStockUseCase;
  private final CheckoutEventTranslator checkoutEventTranslator;

  public CheckoutConfirmedEventListener(
      final ReduceProductStockUseCase reduceProductStockUseCase,
      final CheckoutEventTranslator checkoutEventTranslator) {
    this.reduceProductStockUseCase = reduceProductStockUseCase;
    this.checkoutEventTranslator = checkoutEventTranslator;
  }

  /**
   * Handles the CheckoutConfirmed integration event by reducing product availability.
   *
   * <p>This demonstrates <b>eventual consistency</b> between bounded contexts:
   *
   * <ol>
   *   <li>Checkout context publishes CheckoutConfirmed event
   *   <li>Product context listens and reduces availability asynchronously via ACL
   *   <li>If availability reduction fails, the checkout is still confirmed (eventual consistency)
   * </ol>
   *
   * <p><b>Anti-Corruption Layer:</b> Uses {@link CheckoutEventTranslator} to translate Checkout's
   * event into Product's commands. This protects Product from changes in Checkout's event schema.
   *
   * @param event the checkout confirmed integration event from Checkout context
   */
  @EventListener
  public void onCheckoutConfirmed(final CheckoutConfirmed event) {
    logger.info(
        "Received CheckoutConfirmed integration event v{} for session: {}",
        event.version(),
        event.sessionId().value());

    try {
      // Use Anti-Corruption Layer to translate Checkout event → Product commands
      final List<ReduceProductStockCommand> commands = checkoutEventTranslator.translate(event);

      logger.debug("ACL translated event into {} stock reduction commands", commands.size());

      // Execute commands in Product's ubiquitous language
      commands.forEach(
          command -> {
            try {
              reduceProductStockUseCase.execute(command);

              logger.info(
                  "Reduced availability for product {} by {} units after checkout confirmation",
                  command.productId(),
                  command.quantity());

            } catch (Exception e) {
              logger.error(
                  "Failed to reduce availability for product {} after checkout confirmation: {}",
                  command.productId(),
                  e.getMessage());
              // In production: publish CompensationEvent, trigger Saga, or alert admins
            }
          });

    } catch (CheckoutEventTranslator.UnsupportedEventVersionException e) {
      logger.error(
          "Cannot process CheckoutConfirmed event - unsupported version: {}. "
              + "ACL may need updating. Event ID: {}",
          event.version(),
          event.eventId(),
          e);
      // In production: alert DevOps team, version mismatch between contexts
    }
  }
}
