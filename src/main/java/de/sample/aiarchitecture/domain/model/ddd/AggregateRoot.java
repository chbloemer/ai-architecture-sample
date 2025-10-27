package de.sample.aiarchitecture.domain.model.ddd;

import java.util.List;

/**
 * Marker interface for Aggregate Roots.
 *
 * <p>An aggregate root is the entry point to an aggregate - a cluster of domain objects that are
 * treated as a single unit. The aggregate root is responsible for maintaining invariants within
 * the aggregate and collecting domain events that occur during state changes.
 *
 * <p><b>Domain Events:</b>
 *
 * <p>Aggregate roots collect domain events when state changes occur. These events are published
 * after the aggregate is persisted, enabling eventual consistency and loose coupling between
 * aggregates and bounded contexts.
 *
 * <p><b>Usage Pattern:</b>
 *
 * <pre>
 * // 1. Aggregate performs business operation and raises event
 * product.changePrice(newPrice);
 *
 * // 2. Repository saves aggregate
 * productRepository.save(product);
 *
 * // 3. Application service publishes events
 * product.domainEvents().forEach(eventPublisher::publish);
 *
 * // 4. Clear events after publishing
 * product.clearDomainEvents();
 * </pre>
 *
 * @param <T> the aggregate root type
 * @param <ID> the aggregate root ID type
 */
public interface AggregateRoot<T extends AggregateRoot<T, ID>, ID extends Id>
    extends Entity<T, ID> {

  /**
   * Returns the list of domain events that occurred during state changes.
   *
   * <p>These events should be published after the aggregate is successfully persisted and then
   * cleared.
   *
   * @return list of domain events (never null, may be empty)
   */
  List<DomainEvent> domainEvents();

  /**
   * Clears all domain events.
   *
   * <p>This should be called after events have been successfully published to prevent duplicate
   * event publishing.
   */
  void clearDomainEvents();
}
