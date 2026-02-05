package de.sample.aiarchitecture.sharedkernel.marker.tactical;

/**
 * Marker interface for Read Model Builders.
 *
 * <p>A ReadModelBuilder constructs and maintains Read Models by processing Domain Events. It
 * typically implements one or more {@link StateInterest} interfaces to declare which events it
 * handles.
 *
 * <p><b>Role in the Interest Interface Pattern:</b>
 *
 * <ul>
 *   <li>Implements {@link StateInterest} interface(s) to receive relevant events
 *   <li>Builds and updates queryable Read Model representations
 *   <li>Maintains eventual consistency with the write model
 *   <li>Optimizes data for specific query use cases
 * </ul>
 *
 * <p><b>Characteristics:</b>
 *
 * <ul>
 *   <li>Event-driven - updates based on Domain Events
 *   <li>Idempotent - applying the same event twice produces the same result
 *   <li>Query-optimized - structures data for efficient reads
 *   <li>May be rebuilt from event history (event sourcing scenarios)
 * </ul>
 *
 * <p><b>Example:</b>
 *
 * <pre>
 * public class OrderSummaryProjection
 *         implements OrderInterest, ReadModelBuilder {
 *
 *     private final OrderSummaryRepository repository;
 *
 *     &#64;Override
 *     public void on(OrderPlaced event) {
 *         var summary = OrderSummary.from(event);
 *         repository.save(summary);
 *     }
 *
 *     &#64;Override
 *     public void on(OrderShipped event) {
 *         repository.findById(event.orderId())
 *             .ifPresent(summary -> {
 *                 summary.markShipped(event.shippedAt());
 *                 repository.save(summary);
 *             });
 *     }
 * }
 * </pre>
 *
 * <p><b>Placement:</b> ReadModelBuilder implementations typically reside in the adapter layer
 * (incoming adapters) as they handle infrastructure concerns like persistence of read models.
 *
 * <p><b>Reference:</b> Vaughn Vernon's Implementing Domain-Driven Design (2013), Chapter 14:
 * "Application" - State Mediator Pattern (Interest Interface)
 *
 * @see StateInterest
 * @see DomainEvent
 * @see <a href="https://www.domainlanguage.com/ddd/">Domain-Driven Design Reference</a>
 */
public interface ReadModelBuilder {}
