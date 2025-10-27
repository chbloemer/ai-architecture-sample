package de.sample.aiarchitecture.infrastructure.api;

import de.sample.aiarchitecture.domain.model.ddd.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Infrastructure service for publishing domain events.
 *
 * <p>This service bridges the domain layer and Spring's event infrastructure by publishing
 * domain events to Spring's ApplicationEventPublisher, enabling loose coupling between
 * aggregates and event handlers.
 *
 * <p><b>Usage Pattern:</b>
 *
 * <pre>
 * // In Application Service after saving aggregate:
 * Product product = productRepository.save(product);
 * domainEventPublisher.publishEvents(product);
 * </pre>
 *
 * <p><b>Benefits:</b>
 * <ul>
 *   <li>Domain layer remains framework-independent
 *   <li>Events are published only after successful persistence
 *   <li>Enables asynchronous event handling
 *   <li>Supports eventual consistency across aggregates
 * </ul>
 */
@Component
public class DomainEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

  private final ApplicationEventPublisher eventPublisher;

  public DomainEventPublisher(final ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  /**
   * Publishes a single domain event.
   *
   * <p>The event will be published synchronously by default unless event listeners are configured
   * with @Async.
   *
   * @param event the domain event to publish
   */
  public void publish(final DomainEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Domain event cannot be null");
    }

    log.debug(
        "Publishing domain event: {} (ID: {}, timestamp: {})",
        event.getClass().getSimpleName(),
        event.eventId(),
        event.occurredOn());

    eventPublisher.publishEvent(event);
  }

  /**
   * Publishes all domain events from an aggregate and clears them.
   *
   * <p>This method should be called after successfully persisting an aggregate. It publishes
   * all collected events and then clears them to prevent duplicate publishing.
   *
   * @param aggregate the aggregate containing domain events
   */
  public void publishAndClearEvents(
      final de.sample.aiarchitecture.domain.model.ddd.AggregateRoot<?, ?> aggregate) {
    if (aggregate == null) {
      throw new IllegalArgumentException("Aggregate cannot be null");
    }

    final var events = aggregate.domainEvents();

    if (events.isEmpty()) {
      return;
    }

    log.debug(
        "Publishing {} domain event(s) from aggregate: {}",
        events.size(),
        aggregate.getClass().getSimpleName());

    events.forEach(this::publish);

    aggregate.clearDomainEvents();
  }
}
