package de.sample.aiarchitecture.sharedkernel.marker.tactical;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker interface for Integration Events — adapter-layer DTOs published across bounded contexts.
 *
 * <p>Integration Events are <b>not</b> domain events. They are adapter-layer data transfer objects
 * created when an outgoing event adapter consumes an internal {@link DomainEvent} and publishes a
 * cross-context representation. This separation acts as an Anti-Corruption Layer (ACL) between the
 * publishing context's domain model and external consumers.
 *
 * <p><b>Key Differences from Domain Events:</b>
 *
 * <ul>
 *   <li><b>Layer:</b> Adapter layer ({@code adapter.outgoing.event}), not domain layer
 *   <li><b>Purpose:</b> Cross bounded context communication (domain events are internal)
 *   <li><b>Versioning:</b> Strict backward compatibility required (domain events can change freely)
 *   <li><b>Naming:</b> Suffixed with {@code Event} (e.g., {@code CartCheckedOutEvent}), while
 *       domain events have no suffix (e.g., {@code CartCheckedOut})
 *   <li><b>Creation:</b> Created by outgoing event adapters via {@code from(DomainEvent)} factory
 *       methods
 *   <li><b>Consumption:</b> Consumed by incoming event adapters in other contexts
 * </ul>
 *
 * <p><b>Event Flow:</b>
 *
 * <pre>
 * Aggregate raises DomainEvent
 *   → Outgoing EventPublisher adapter listens
 *     → Creates IntegrationEvent via from() factory
 *       → Publishes IntegrationEvent
 *         → Incoming EventConsumer in other context receives it
 * </pre>
 *
 * <p><b>Example — Integration Event (adapter layer):</b>
 *
 * <pre>
 * // In cart/adapter/outgoing/event/
 * public record CartCheckedOutEvent(
 *     UUID eventId,
 *     CartId cartId,
 *     CustomerId customerId,
 *     Money totalAmount,
 *     int itemCount,
 *     List&lt;ItemInfo&gt; items,
 *     Instant occurredOn,
 *     int version) implements IntegrationEvent {
 *
 *   public record ItemInfo(ProductId productId, int quantity) {}
 *
 *   public static CartCheckedOutEvent from(CartCheckedOut domainEvent) {
 *     List&lt;ItemInfo&gt; items = domainEvent.items().stream()
 *         .map(i -&gt; new ItemInfo(i.productId(), i.quantity()))
 *         .toList();
 *     return new CartCheckedOutEvent(
 *         domainEvent.eventId(), domainEvent.cartId(), ...items, domainEvent.occurredOn(), 1);
 *   }
 * }
 * </pre>
 *
 * <p><b>Example — Outgoing Event Publisher (adapter):</b>
 *
 * <pre>
 * &#64;Component
 * public class CartCheckedOutEventPublisher {
 *   private final ApplicationEventPublisher publisher;
 *
 *   &#64;EventListener
 *   public void on(CartCheckedOut domainEvent) {
 *     publisher.publishEvent(CartCheckedOutEvent.from(domainEvent));
 *   }
 * }
 * </pre>
 *
 * @see DomainEvent
 */
public interface IntegrationEvent {

  /** Unique identifier for this event instance. */
  UUID eventId();

  /** Timestamp when the event occurred. */
  Instant occurredOn();

  /**
   * Event schema version to support event evolution and backward compatibility.
   *
   * <p>Consumers may depend on the event schema, so changes require version increments to maintain
   * backward compatibility.
   *
   * @return the event version
   */
  int version();
}
