package de.sample.aiarchitecture.domain.model.ddd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for Aggregate Roots providing domain event collection.
 *
 * <p>This class provides a reusable implementation of domain event collection that aggregate roots
 * can use by extending this class. It handles the storage and management of domain events that
 * occur during aggregate state changes.
 *
 * <p><b>Usage:</b>
 *
 * <pre>
 * public final class Product extends BaseAggregateRoot&lt;Product, ProductId&gt; {
 *   // ... fields
 *
 *   public void changePrice(Price newPrice) {
 *     Price oldPrice = this.price;
 *     this.price = newPrice;
 *
 *     // Raise domain event
 *     registerEvent(new ProductPriceChanged(this.id, oldPrice, newPrice));
 *   }
 * }
 * </pre>
 *
 * @param <T> the aggregate root type
 * @param <ID> the aggregate root ID type
 */
public abstract class BaseAggregateRoot<T extends AggregateRoot<T, ID>, ID extends Id>
    implements AggregateRoot<T, ID> {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  /**
   * Registers a domain event to be published after the aggregate is persisted.
   *
   * <p>Call this method from within your aggregate's business methods when something significant
   * happens that other parts of the system might care about. This method is public to allow
   * factories to register events during aggregate creation.
   *
   * @param event the domain event to register
   */
  public void registerEvent(final DomainEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Domain event cannot be null");
    }
    this.domainEvents.add(event);
  }

  @Override
  public List<DomainEvent> domainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  @Override
  public void clearDomainEvents() {
    this.domainEvents.clear();
  }
}
