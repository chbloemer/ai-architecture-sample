package de.sample.aiarchitecture.infrastructure.config;

import de.sample.aiarchitecture.domain.model.ddd.AggregateRoot;
import de.sample.aiarchitecture.domain.model.ddd.DomainEvent;
import de.sample.aiarchitecture.infrastructure.api.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring-based implementation of the DomainEventPublisher interface.
 *
 * <p>This implementation bridges the domain layer and Spring's event infrastructure by delegating
 * to Spring's ApplicationEventPublisher. It enables loose coupling between aggregates and event
 * handlers while keeping the application layer framework-independent.
 *
 * <p><b>Implementation Details:</b>
 * <ul>
 *   <li>Uses Spring's ApplicationEventPublisher for event distribution
 *   <li>Events are published synchronously by default
 *   <li>Event listeners can use @Async for asynchronous processing
 *   <li>Logs all published events for debugging and auditing
 * </ul>
 *
 * <p><b>Alternative Implementations:</b> This interface-based design allows for alternative
 * implementations such as message broker integration, in-memory publishing for testing, or custom
 * event buses.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisher.class);

  private final ApplicationEventPublisher eventPublisher;

  public SpringDomainEventPublisher(final ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  /**
   * Publishes a single domain event using Spring's ApplicationEventPublisher.
   *
   * <p>The event will be published synchronously by default unless event listeners are configured
   * with @Async.
   *
   * @param event the domain event to publish
   * @throws IllegalArgumentException if event is null
   */
  @Override
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
   * <p>This method should be called after successfully persisting an aggregate. It publishes all
   * collected events and then clears them to prevent duplicate publishing.
   *
   * @param aggregate the aggregate containing domain events
   * @throws IllegalArgumentException if aggregate is null
   */
  @Override
  public void publishAndClearEvents(final AggregateRoot<?, ?> aggregate) {
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
