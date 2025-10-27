package de.sample.aiarchitecture.infrastructure.api;

import de.sample.aiarchitecture.domain.model.ddd.AggregateRoot;
import de.sample.aiarchitecture.domain.model.ddd.DomainEvent;

/**
 * Service Provider Interface (SPI) for publishing domain events.
 *
 * <p>This interface defines the contract for publishing domain events from the application layer to
 * the infrastructure layer, enabling loose coupling between aggregates and event handlers.
 *
 * <p>The application layer depends on this interface (in infrastructure.api), while concrete
 * implementations reside in the infrastructure layer, following the Dependency Inversion Principle.
 *
 * <p><b>Usage Pattern:</b>
 *
 * <pre>
 * // In Application Service after saving aggregate:
 * Product product = productRepository.save(product);
 * domainEventPublisher.publishAndClearEvents(product);
 * </pre>
 *
 * <p><b>Benefits:</b>
 * <ul>
 *   <li>Application layer remains framework-independent
 *   <li>Events are published only after successful persistence
 *   <li>Enables asynchronous event handling
 *   <li>Supports eventual consistency across aggregates
 *   <li>Easy to swap implementations or mock for testing
 * </ul>
 *
 * <p><b>Implementation Note:</b> Concrete implementations should ensure events are published only
 * after successful persistence to maintain data consistency.
 */
public interface DomainEventPublisher {

  /**
   * Publishes a single domain event.
   *
   * <p>The event will be published to the underlying event infrastructure (e.g., Spring's
   * ApplicationEventPublisher, message broker, etc.).
   *
   * @param event the domain event to publish
   * @throws IllegalArgumentException if event is null
   */
  void publish(DomainEvent event);

  /**
   * Publishes all domain events from an aggregate and clears them.
   *
   * <p>This method should be called after successfully persisting an aggregate. It publishes all
   * collected events and then clears them to prevent duplicate publishing.
   *
   * @param aggregate the aggregate containing domain events
   * @throws IllegalArgumentException if aggregate is null
   */
  void publishAndClearEvents(AggregateRoot<?, ?> aggregate);
}
