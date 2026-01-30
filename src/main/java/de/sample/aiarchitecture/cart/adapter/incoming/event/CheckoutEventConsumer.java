package de.sample.aiarchitecture.cart.adapter.incoming.event;

import de.sample.aiarchitecture.cart.application.completecart.CompleteCartCommand;
import de.sample.aiarchitecture.cart.application.completecart.CompleteCartInputPort;
import de.sample.aiarchitecture.checkout.domain.event.CheckoutConfirmed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for checkout-related integration events in the Cart context.
 *
 * <p><b>Cross-Context Integration:</b> This listener enables eventual consistency between
 * the Checkout and Cart bounded contexts. When a checkout is confirmed, the cart is marked
 * as completed.
 *
 * <p><b>Why Events Instead of Direct Calls?</b>
 *
 * <ul>
 *   <li>Maintains bounded context isolation - Checkout doesn't depend on Cart internals
 *   <li>Eventual consistency - Cart status update happens asynchronously
 *   <li>Loose coupling - Contexts communicate through events, not direct dependencies
 *   <li>Extensibility - Other contexts can listen to the same event
 * </ul>
 *
 * <p><b>Architectural Pattern:</b> This is an "incoming event adapter" in the Cart context,
 * receiving integration events published by the Checkout context.
 */
@Component
public class CheckoutEventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(CheckoutEventConsumer.class);

  private final CompleteCartInputPort completeCartUseCase;

  public CheckoutEventConsumer(final CompleteCartInputPort completeCartUseCase) {
    this.completeCartUseCase = completeCartUseCase;
  }

  /**
   * Handles the CheckoutConfirmed integration event by completing the cart.
   *
   * <p>This demonstrates <b>eventual consistency</b> between bounded contexts:
   *
   * <ol>
   *   <li>Checkout context publishes CheckoutConfirmed event
   *   <li>Cart context listens and marks cart as completed asynchronously
   *   <li>If completion fails, the checkout is still confirmed (eventual consistency)
   * </ol>
   *
   * @param event the checkout confirmed integration event from Checkout context
   */
  @EventListener
  public void onCheckoutConfirmed(final CheckoutConfirmed event) {
    logger.info(
        "Received CheckoutConfirmed integration event v{} for cart: {}, session: {}",
        event.version(),
        event.cartId().value(),
        event.sessionId().value());

    try {
      final CompleteCartCommand command =
          new CompleteCartCommand(event.cartId().value().toString());

      completeCartUseCase.execute(command);

      logger.info(
          "Cart {} marked as completed after checkout confirmation",
          event.cartId().value());

    } catch (IllegalStateException e) {
      logger.warn(
          "Could not complete cart {} - may already be completed or in unexpected state: {}",
          event.cartId().value(),
          e.getMessage());
      // Cart might already be completed - this is fine for idempotency
    } catch (IllegalArgumentException e) {
      logger.error(
          "Failed to complete cart {} after checkout confirmation - cart not found: {}",
          event.cartId().value(),
          e.getMessage());
      // In production: consider compensating action or alert
    }
  }
}
