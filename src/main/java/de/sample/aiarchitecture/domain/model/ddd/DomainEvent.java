package de.sample.aiarchitecture.domain.model.ddd;

import java.time.Instant;
import java.util.UUID;

/**
 * Interface for Domain Events.
 *
 * <p>Domain Events represent something that happened in the domain that domain experts care about.
 * They capture important occurrences in the business process and enable loose coupling between
 * bounded contexts.
 *
 * <p><b>Characteristics:</b>
 *
 * <ul>
 *   <li>Immutable (final classes or records)
 *   <li>Named in the past tense (e.g., ProductCreated, CartCheckedOut, PriceChanged)
 *   <li>Include timestamp, unique ID, and event-specific data
 *   <li>Should NOT have Spring annotations (@Component, @EventListener)
 *   <li>Part of the Ubiquitous Language
 * </ul>
 *
 * <p><b>Use Cases:</b>
 *
 * <ul>
 *   <li>Triggering side effects in other aggregates or contexts
 *   <li>Enabling eventual consistency between bounded contexts
 *   <li>Audit trail and event sourcing
 *   <li>Decoupling bounded contexts
 * </ul>
 *
 * <p><b>Required Methods:</b>
 *
 * <ul>
 *   <li>{@code eventId()} - Unique identifier for this event instance
 *   <li>{@code occurredOn()} - When the event occurred
 *   <li>{@code version()} - Event schema version for evolution
 * </ul>
 *
 * <p><b>Example:</b>
 *
 * <pre>
 * public record ProductCreated(
 *     UUID eventId,
 *     ProductId productId,
 *     Instant occurredOn,
 *     int version) implements DomainEvent {
 *
 *   public ProductCreated(ProductId productId) {
 *     this(UUID.randomUUID(), productId, Instant.now(), 1);
 *   }
 * }
 * </pre>
 *
 * <p><b>References:</b>
 *
 * <ul>
 *   <li>Eric Evans' Domain-Driven Design (2003)
 *   <li>Vaughn Vernon's Implementing Domain-Driven Design (2013), Chapter 8: "Domain Events"
 * </ul>
 *
 * @see <a href="https://www.domainlanguage.com/ddd/">Domain-Driven Design Reference</a>
 */
public interface DomainEvent {

  /**
   * Unique identifier for this event instance.
   *
   * @return the event ID
   */
  UUID eventId();

  /**
   * Timestamp when the event occurred in the domain.
   *
   * @return the occurrence timestamp
   */
  Instant occurredOn();

  /**
   * Event schema version to support event evolution.
   *
   * <p>Increment this version when the event structure changes to maintain backward compatibility.
   *
   * @return the event version
   */
  int version();
}
