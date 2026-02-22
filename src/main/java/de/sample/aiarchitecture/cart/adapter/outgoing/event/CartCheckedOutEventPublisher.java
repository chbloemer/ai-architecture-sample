package de.sample.aiarchitecture.cart.adapter.outgoing.event;

import de.sample.aiarchitecture.cart.domain.event.CartCheckedOut;
import de.sample.aiarchitecture.cart.events.CartCheckedOutEvent;
import java.util.UUID;
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
    final var items =
        domainEvent.items().stream()
            .map(item -> new CartCheckedOutEvent.ItemInfo(item.productId(), item.quantity()))
            .toList();

    final var integrationEvent =
        new CartCheckedOutEvent(
            domainEvent.eventId(),
            UUID.fromString(domainEvent.cartId().value()),
            domainEvent.customerId().value(),
            domainEvent.totalAmount(),
            domainEvent.itemCount(),
            items,
            domainEvent.occurredOn(),
            1);

    logger.info(
        "Publishing CartCheckedOutEvent v{} for cart: {}",
        integrationEvent.version(),
        integrationEvent.cartId());

    publisher.publishEvent(integrationEvent);
  }
}
