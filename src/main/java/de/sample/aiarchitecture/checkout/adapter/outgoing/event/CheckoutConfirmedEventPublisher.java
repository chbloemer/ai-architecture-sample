package de.sample.aiarchitecture.checkout.adapter.outgoing.event;

import de.sample.aiarchitecture.checkout.domain.event.CheckoutConfirmed;
import de.sample.aiarchitecture.checkout.events.CheckoutConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Outgoing event adapter that translates the internal {@link CheckoutConfirmed} domain event into a
 * {@link CheckoutConfirmedEvent} integration event for cross-context consumption.
 *
 * <p>This adapter acts as an Anti-Corruption Layer between the Checkout context's domain model and
 * external consumers, ensuring that internal domain changes do not leak across bounded context
 * boundaries.
 */
@Component
public class CheckoutConfirmedEventPublisher {

  private static final Logger logger =
      LoggerFactory.getLogger(CheckoutConfirmedEventPublisher.class);

  private final ApplicationEventPublisher publisher;

  public CheckoutConfirmedEventPublisher(final ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  /** Listens for the internal domain event and publishes the integration event. */
  @EventListener
  public void on(final CheckoutConfirmed domainEvent) {
    final var items =
        domainEvent.items().stream()
            .map(item -> new CheckoutConfirmedEvent.LineItemInfo(item.productId(), item.quantity()))
            .toList();

    final var integrationEvent =
        new CheckoutConfirmedEvent(
            domainEvent.eventId(),
            domainEvent.sessionId().value(),
            domainEvent.cartId().value(),
            domainEvent.customerId().value(),
            domainEvent.totalAmount(),
            items,
            domainEvent.occurredOn(),
            1);

    logger.info(
        "Publishing CheckoutConfirmedEvent v{} for session: {}",
        integrationEvent.version(),
        integrationEvent.sessionId());

    publisher.publishEvent(integrationEvent);
  }
}
