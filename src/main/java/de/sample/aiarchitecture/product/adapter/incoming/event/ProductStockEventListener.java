package de.sample.aiarchitecture.product.adapter.incoming.event;

import de.sample.aiarchitecture.cart.domain.event.CartCheckedOut;
import de.sample.aiarchitecture.product.application.usecase.reduceproductstock.ReduceProductStockCommand;
import de.sample.aiarchitecture.product.application.usecase.reduceproductstock.ReduceProductStockUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for cart-related domain events in the Product context.
 *
 * <p><b>Cross-Context Integration:</b> This listener enables eventual consistency between
 * the Cart and Product bounded contexts. When a cart is checked out, we reduce product stock.
 *
 * <p><b>Why Events Instead of Direct Calls?</b>
 * <ul>
 *   <li>Maintains bounded context isolation - Cart doesn't depend on Product
 *   <li>Eventual consistency - Stock reduction happens asynchronously
 *   <li>Loose coupling - Contexts communicate through events, not direct dependencies
 *   <li>Extensibility - Other contexts can listen to the same event
 * </ul>
 *
 * <p><b>Architectural Pattern:</b> This is an "incoming event adapter" in the Product context,
 * receiving events published by the Cart context.
 */
@Component
public class ProductStockEventListener {

  private static final Logger logger = LoggerFactory.getLogger(ProductStockEventListener.class);

  private final ReduceProductStockUseCase reduceProductStockUseCase;

  public ProductStockEventListener(final ReduceProductStockUseCase reduceProductStockUseCase) {
    this.reduceProductStockUseCase = reduceProductStockUseCase;
  }

  /**
   * Handles the CartCheckedOut event by reducing product stock.
   *
   * <p>This demonstrates <b>eventual consistency</b> between bounded contexts:
   * <ol>
   *   <li>Cart context publishes CartCheckedOut event
   *   <li>Product context listens and reduces stock asynchronously
   *   <li>If stock reduction fails, the cart is still checked out (eventual consistency)
   * </ol>
   *
   * @param event the cart checked out event
   */
  @EventListener
  public void onCartCheckedOut(final CartCheckedOut event) {
    logger.info("Received CartCheckedOut event for cart: {}", event.cartId().value());

    event.items().forEach(itemInfo -> {
      try {
        final ReduceProductStockCommand command = new ReduceProductStockCommand(
            itemInfo.productId().value().toString(),
            itemInfo.quantity()
        );

        reduceProductStockUseCase.execute(command);

        logger.info("Reduced stock for product {} by {} units",
            itemInfo.productId().value(),
            itemInfo.quantity());

      } catch (Exception e) {
        logger.error("Failed to reduce stock for product {} after cart checkout: {}",
            itemInfo.productId().value(),
            e.getMessage());
        // In production: publish CompensationEvent, trigger workflow, or alert admins
      }
    });
  }
}
