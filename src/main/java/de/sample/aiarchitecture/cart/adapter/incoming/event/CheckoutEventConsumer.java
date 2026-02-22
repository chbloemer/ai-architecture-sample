package de.sample.aiarchitecture.cart.adapter.incoming.event;

import de.sample.aiarchitecture.cart.application.completecart.CompleteCartCommand;
import de.sample.aiarchitecture.cart.application.completecart.CompleteCartInputPort;
import de.sample.aiarchitecture.checkout.adapter.outgoing.event.CheckoutConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Incoming event adapter consuming {@link CheckoutConfirmedEvent} integration events from the
 * Checkout context.
 *
 * <p>When a checkout is confirmed, this consumer marks the corresponding cart as completed,
 * enabling eventual consistency between the Checkout and Cart bounded contexts.
 */
@Component
public class CheckoutEventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(CheckoutEventConsumer.class);

  private final CompleteCartInputPort completeCartUseCase;

  public CheckoutEventConsumer(final CompleteCartInputPort completeCartUseCase) {
    this.completeCartUseCase = completeCartUseCase;
  }

  /**
   * Handles the CheckoutConfirmedEvent integration event by completing the cart.
   *
   * @param event the checkout confirmed integration event from Checkout context
   */
  @EventListener
  public void onCheckoutConfirmed(final CheckoutConfirmedEvent event) {
    logger.info(
        "Received CheckoutConfirmedEvent v{} for cart: {}, session: {}",
        event.version(),
        event.cartId().value(),
        event.sessionId().value());

    try {
      final CompleteCartCommand command =
          new CompleteCartCommand(event.cartId().value().toString());

      completeCartUseCase.execute(command);

      logger.info(
          "Cart {} marked as completed after checkout confirmation", event.cartId().value());

    } catch (IllegalStateException e) {
      logger.warn(
          "Could not complete cart {} - may already be completed or in unexpected state: {}",
          event.cartId().value(),
          e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error(
          "Failed to complete cart {} after checkout confirmation - cart not found: {}",
          event.cartId().value(),
          e.getMessage());
    }
  }
}
