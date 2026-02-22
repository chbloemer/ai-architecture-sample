# ADR-024: Interface Inversion Pattern for Spring Modulith Event Listeners

**Date**: February 22, 2026
**Status**: Accepted
**Deciders**: Architecture Team

---

## Context

Spring Modulith's `verify()` treats event listener imports as module dependencies. When a module's event consumer imports an event class from another module, Spring Modulith records a dependency from the consumer's module to the producer's module.

This created dependency cycles in our codebase:

1. **cart <-> checkout cycle**: The cart module consumed `CheckoutConfirmedEvent` from checkout (to clear the cart after checkout), while checkout already depended on cart (to read cart contents during checkout).
2. **inventory -> checkout cycle**: The inventory module consumed `CheckoutConfirmedEvent` from checkout (to reduce stock levels), creating an unnecessary dependency from a leaf module to a process module.

Spring Modulith's `verify()` rejects cyclic module dependencies, making the application context fail verification.

---

## Decision

**Use the Interface Inversion pattern to break cross-module event listener cycles.**

This pattern is recommended by Oliver Drotbohm (Spring Modulith discussion [#493](https://github.com/spring-projects/spring-modulith/discussions/493)). The core idea: the *consumer* module defines a trigger interface in its own published `events/` package, and the *producer* module's event class implements that interface. Consumers listen to their own interfaces instead of importing the producer's event class.

### Implementation

**Cart module** defines its trigger interface:

```java
// cart/events/CartCompletionTrigger.java (published API)
public interface CartCompletionTrigger {
    UserId userId();
}
```

**Inventory module** defines its trigger interface:

```java
// inventory/events/StockReductionTrigger.java (published API)
public interface StockReductionTrigger {
    List<OrderItem> items();
}
```

**Checkout module** implements both interfaces on its event:

```java
// checkout/domain/model/CheckoutConfirmedEvent.java
public record CheckoutConfirmedEvent(...)
    implements DomainEvent, CartCompletionTrigger, StockReductionTrigger {
    // ...
}
```

**Consumers listen to their own interface**, not the foreign event:

```java
// cart/adapter/incoming/event/CartCompletionListener.java
@ApplicationModuleListener
void on(CartCompletionTrigger trigger) {
    // clear cart for trigger.userId()
}

// inventory/adapter/incoming/event/StockReductionListener.java
@ApplicationModuleListener
void on(StockReductionTrigger trigger) {
    // reduce stock for trigger.items()
}
```

Each listener uses `@ApplicationModuleListener`, which ensures it runs in its own transaction (one aggregate per transaction).

### Dependency Direction

```
checkout ──implements──> cart/events/CartCompletionTrigger
checkout ──implements──> inventory/events/StockReductionTrigger

cart listens to CartCompletionTrigger       (own interface, no foreign import)
inventory listens to StockReductionTrigger  (own interface, no foreign import)
```

Dependencies flow from the producer (checkout) to consumer-defined interfaces. Consumers have zero dependencies on the producer module.

---

## Consequences

### Positive

- Acyclic module dependency graph verified by Spring Modulith `verify()`
- One aggregate per transaction (DDD best practice) via `@ApplicationModuleListener`
- Cart and inventory are "leaf" modules with no business context dependencies
- Each consumer defines exactly the data shape it needs via its trigger interface

### Negative

- Producer (checkout) must implement consumer-defined interfaces, coupling the event shape to consumer needs
- Each consumer processes events in separate transactions (eventual consistency rather than strong consistency)
- Adding a new consumer requires the producer to implement an additional interface

---

## Related ADRs

- [ADR-005: Domain Events Publishing Strategy](adr-005-domain-events-publishing.md) -- how domain events are published
- [ADR-006: Domain Events as Immutable Records](adr-006-domain-events-immutable-records.md) -- event record design
- [ADR-011: Bounded Context Isolation](adr-011-bounded-context-isolation.md) -- module isolation principles
- [ADR-019: Open Host Service Pattern](adr-019-open-host-service-pattern.md) -- cross-context communication
