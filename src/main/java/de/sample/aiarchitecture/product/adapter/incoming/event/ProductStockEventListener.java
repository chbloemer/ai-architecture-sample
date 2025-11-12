package de.sample.aiarchitecture.product.adapter.incoming.event;

import de.sample.aiarchitecture.cart.domain.event.CartCheckedOut;
import de.sample.aiarchitecture.product.adapter.incoming.event.acl.CartEventTranslator;
import de.sample.aiarchitecture.product.application.reduceproductstock.ReduceProductStockCommand;
import de.sample.aiarchitecture.product.application.reduceproductstock.ReduceProductStockUseCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for cart-related integration events in the Product context.
 *
 * <p><b>Cross-Context Integration:</b> This listener enables eventual consistency between
 * the Cart and Product bounded contexts. When a cart is checked out, we reduce product stock.
 *
 * <p><b>Anti-Corruption Layer Pattern:</b> This listener uses {@link CartEventTranslator}
 * to translate Cart context events into Product context commands. This protects Product
 * from changes in Cart's event schema and maintains bounded context isolation.
 *
 * <p><b>Why Events Instead of Direct Calls?</b>
 *
 * <ul>
 *   <li>Maintains bounded context isolation - Cart doesn't depend on Product
 *   <li>Eventual consistency - Stock reduction happens asynchronously
 *   <li>Loose coupling - Contexts communicate through events, not direct dependencies
 *   <li>Extensibility - Other contexts can listen to the same event
 * </ul>
 *
 * <p><b>Why Anti-Corruption Layer?</b>
 *
 * <ul>
 *   <li>Isolation - Product doesn't directly depend on Cart's event structure
 *   <li>Evolution - Cart can change its events without breaking Product
 *   <li>Versioning - ACL handles multiple event versions transparently
 *   <li>Translation - Converts Cart's language to Product's language
 * </ul>
 *
 * <p><b>Architectural Pattern:</b> This is an "incoming event adapter" in the Product context,
 * receiving integration events published by the Cart context.
 *
 * @see CartEventTranslator
 */
@Component
public class ProductStockEventListener {

  private static final Logger logger = LoggerFactory.getLogger(ProductStockEventListener.class);

  private final ReduceProductStockUseCase reduceProductStockUseCase;
  private final CartEventTranslator cartEventTranslator;

  public ProductStockEventListener(
      final ReduceProductStockUseCase reduceProductStockUseCase,
      final CartEventTranslator cartEventTranslator) {
    this.reduceProductStockUseCase = reduceProductStockUseCase;
    this.cartEventTranslator = cartEventTranslator;
  }

  /**
   * Handles the CartCheckedOut integration event by reducing product stock.
   *
   * <p>This demonstrates <b>eventual consistency</b> between bounded contexts:
   *
   * <ol>
   *   <li>Cart context publishes CartCheckedOut event
   *   <li>Product context listens and reduces stock asynchronously via ACL
   *   <li>If stock reduction fails, the cart is still checked out (eventual consistency)
   * </ol>
   *
   * <p><b>Anti-Corruption Layer:</b> Uses {@link CartEventTranslator} to translate Cart's
   * event into Product's commands. This protects Product from changes in Cart's event schema.
   *
   * <p><b>Version Handling:</b> The ACL handles different event versions transparently.
   * Product context doesn't need to know which version Cart is publishing.
   *
   * @param event the cart checked out integration event from Cart context
   */
  @EventListener
  public void onCartCheckedOut(final CartCheckedOut event) {
    logger.info(
        "Received CartCheckedOut integration event v{} for cart: {}",
        event.version(),
        event.cartId().value());

    try {
      // Use Anti-Corruption Layer to translate Cart event → Product commands
      final List<ReduceProductStockCommand> commands = cartEventTranslator.translate(event);

      logger.debug("ACL translated event into {} stock reduction commands", commands.size());

      // Execute commands in Product's ubiquitous language
      commands.forEach(
          command -> {
            try {
              reduceProductStockUseCase.execute(command);

              logger.info(
                  "Reduced stock for product {} by {} units", command.productId(), command.quantity());

            } catch (Exception e) {
              logger.error(
                  "Failed to reduce stock for product {} after cart checkout: {}",
                  command.productId(),
                  e.getMessage());
              // In production: publish CompensationEvent, trigger Saga, or alert admins
            }
          });

    } catch (CartEventTranslator.UnsupportedEventVersionException e) {
      logger.error(
          "Cannot process CartCheckedOut event - unsupported version: {}. "
              + "ACL may need updating. Event ID: {}",
          event.version(),
          event.eventId(),
          e);
      // In production: alert DevOps team, version mismatch between contexts
    }
  }
}
