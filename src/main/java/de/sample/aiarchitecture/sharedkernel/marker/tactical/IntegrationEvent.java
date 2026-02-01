package de.sample.aiarchitecture.sharedkernel.marker.tactical;

/**
 * Marker interface for Integration Events - events published across bounded contexts.
 *
 * <p>Integration Events represent a <b>public contract</b> between bounded contexts and must be
 * treated differently from internal domain events. They enable cross-context communication while
 * maintaining bounded context autonomy.
 *
 * <p><b>Key Differences from Internal Domain Events:</b>
 *
 * <ul>
 *   <li><b>Scope:</b> Cross bounded context boundaries (internal events stay within one context)
 *   <li><b>Versioning:</b> Strict backward compatibility required (internal events can change
 *       freely)
 *   <li><b>Documentation:</b> Must be formally documented with schema (internal events are
 *       informal)
 *   <li><b>Consumption:</b> Should be consumed via Anti-Corruption Layer (internal events are
 *       direct)
 *   <li><b>Evolution:</b> Breaking changes require new versions (internal events can be refactored)
 * </ul>
 *
 * <p><b>When to Use Integration Events:</b>
 *
 * <ul>
 *   <li>Communication between different bounded contexts (e.g., Cart → Product)
 *   <li>Enabling eventual consistency across contexts
 *   <li>Decoupling autonomous services/modules
 *   <li>Events that external consumers depend on
 * </ul>
 *
 * <p><b>When NOT to Use (Use {@link DomainEvent} instead):</b>
 *
 * <ul>
 *   <li>Communication within the same bounded context
 *   <li>Triggering side effects in the same aggregate
 *   <li>Internal notifications that don't cross context boundaries
 * </ul>
 *
 * <p><b>Best Practices:</b>
 *
 * <ul>
 *   <li>Use immutable records for implementation
 *   <li>Include only essential data (minimize coupling)
 *   <li>Use Shared Kernel types (e.g., ProductId) or primitives
 *   <li>Never include full domain objects from other contexts
 *   <li>Increment version on schema changes
 *   <li>Document schema in event catalog (YAML/AsyncAPI)
 *   <li>Consume via Anti-Corruption Layer to isolate contexts
 * </ul>
 *
 * <p><b>Example - Integration Event:</b>
 *
 * <pre>
 * // Cart context publishes this event for other contexts
 * public record CartCheckedOut(
 *     UUID eventId,
 *     CartId cartId,
 *     CustomerId customerId,
 *     Money totalAmount,
 *     int itemCount,
 *     List&lt;ItemInfo&gt; items,  // DTO, not full domain objects
 *     Instant occurredOn,
 *     int version) implements IntegrationEvent {
 *
 *   // Lightweight DTO using Shared Kernel types
 *   public record ItemInfo(ProductId productId, int quantity) {}
 *
 *   public static CartCheckedOut now(...) {
 *     return new CartCheckedOut(..., Instant.now(), 1);
 *   }
 * }
 * </pre>
 *
 * <p><b>Example - Consuming via Anti-Corruption Layer:</b>
 *
 * <pre>
 * // Product context translates Cart's event into Product's language
 * &#64;Component
 * public class CartEventTranslator {
 *   public List&lt;ReduceStockCommand&gt; translate(CartCheckedOut event) {
 *     return event.items().stream()
 *         .map(item -&gt; new ReduceStockCommand(
 *             item.productId(),
 *             item.quantity()))
 *         .toList();
 *   }
 * }
 * </pre>
 *
 * <p><b>Architecture Alignment:</b>
 *
 * <ul>
 *   <li><b>DDD Strategic Design:</b> Enables bounded context integration
 *   <li><b>Hexagonal Architecture:</b> Events flow through ports (inbound/outbound)
 *   <li><b>Event-Driven Architecture:</b> Async communication between services
 *   <li><b>Deployment Patterns:</b> Supports modular monolith → microservices evolution
 * </ul>
 *
 * <p><b>References:</b>
 *
 * <ul>
 *   <li>Vaughn Vernon's "Implementing Domain-Driven Design" (2013), Chapter 13: "Integrating
 *       Bounded Contexts"
 *   <li>Martin Fowler's "Event-Driven Architecture" patterns
 *   <li>Sam Newman's "Building Microservices" (2021), Chapter 4: "Microservice Communication
 *       Styles"
 * </ul>
 *
 * @see DomainEvent
 * @see <a href="https://www.domainlanguage.com/ddd/">Domain-Driven Design Reference</a>
 */
public interface IntegrationEvent extends DomainEvent {
  // Marker interface - inherits all DomainEvent methods
  // No additional methods needed - the semantic distinction is the key
}
