package de.sample.aiarchitecture.cart.adapter.outgoing.event;

import de.sample.aiarchitecture.cart.domain.event.CartCheckedOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Outgoing event adapter that translates the internal {@link CartCheckedOut} domain event into a
 * {@link CartCheckedOutEvent} integration event for cross-context consumption.
 *
 * <p>This adapter acts as an Anti-Corruption Layer between the Cart context's domain model and
 * external consumers, ensuring that internal domain changes do not leak across bounded context
 * boundaries.
 */
@Component
public class CartCheckedOutEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(CartCheckedOutEventPublisher.class);

  private final ApplicationEventPublisher publisher;

  public CartCheckedOutEventPublisher(final ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  /** Listens for the internal domain event and publishes the integration event. */
  @EventListener
  public void on(final CartCheckedOut domainEvent) {
    final CartCheckedOutEvent integrationEvent = CartCheckedOutEvent.from(domainEvent);

    logger.info(
        "Publishing CartCheckedOutEvent v{} for cart: {}",
        integrationEvent.version(),
        integrationEvent.cartId().value());

    publisher.publishEvent(integrationEvent);
  }
}
