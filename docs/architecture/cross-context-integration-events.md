# Cross-Context Integration Through Domain Events

This document demonstrates how bounded contexts communicate through domain events while maintaining proper isolation according to DDD principles.

## Use Case: Reducing Product Stock on Cart Checkout

### Business Flow

1. Customer checks out their shopping cart (Cart context)
2. Product inventory is automatically reduced (Product context)
3. Contexts remain decoupled - no direct dependencies

### Why Events Instead of Direct Calls?

**❌ Anti-Pattern: Direct Context Coupling**
```java
// BAD: Cart context directly calling Product context
public class CheckoutCartUseCase {
    private final ProductRepository productRepository; // Cross-context dependency!

    public void execute(CheckoutCartCommand cmd) {
        cart.checkout();
        // Direct call to Product context - tightly coupled!
        cart.items().forEach(item -> {
            Product product = productRepository.findById(item.productId());
            product.decreaseStock(item.quantity());
        });
    }
}
```

**Problems:**
- Tight coupling between bounded contexts
- Violates Single Responsibility Principle
- Changes in Product context break Cart context
- Difficult to scale independently
- No audit trail of what triggered stock changes

**✅ Proper Pattern: Event-Driven Integration**
```java
// GOOD: Cart context publishes event
public class ShoppingCart {
    public void checkout() {
        this.status = CartStatus.CHECKED_OUT;
        registerEvent(CartCheckedOut.now(id, customerId, total, itemCount, items));
    }
}

// GOOD: Product context listens and reacts
@Component
public class CartEventListener {
    @EventListener
    public void onCartCheckedOut(CartCheckedOut event) {
        event.items().forEach(item ->
            reduceProductStockUseCase.execute(
                new ReduceProductStockCommand(item.productId(), item.quantity())
            )
        );
    }
}
```

**Benefits:**
- ✅ **Loose Coupling**: Cart doesn't know Product context exists
- ✅ **Eventual Consistency**: Stock reduction happens asynchronously
- ✅ **Extensibility**: Other contexts can listen to the same event
- ✅ **Auditability**: Events create audit trail
- ✅ **Scalability**: Each context can scale independently
- ✅ **Fault Tolerance**: Cart checkout succeeds even if stock reduction temporarily fails

## Implementation Details

### 1. Domain Event with DTOs (Cart Context)

**File:** `cart/domain/event/CartCheckedOut.java`

```java
public record CartCheckedOut(
    UUID eventId,
    CartId cartId,
    CustomerId customerId,
    Money totalAmount,
    int itemCount,
    List<ItemInfo> items,  // DTO, not domain object!
    Instant occurredOn,
    int version
) implements DomainEvent {

    // Nested DTO to avoid exposing Cart domain objects to other contexts
    public record ItemInfo(
        ProductId productId,  // From Shared Kernel
        int quantity          // Primitive type
    ) {}
}
```

**Key Points:**
- Event uses **ItemInfo DTO** instead of CartItem domain object
- Only contains **Shared Kernel types** (ProductId) and primitives
- **Immutable** (Java record ensures this)
- **Self-contained** - all data needed by listeners

### 2. Event Listener (Product Context)

**File:** `product/adapter/incoming/event/CartEventListener.java`

```java
@Component
public class CartEventListener {

    private final ReduceProductStockUseCase reduceProductStockUseCase;

    @EventListener
    public void onCartCheckedOut(final CartCheckedOut event) {
        logger.info("Received CartCheckedOut event for cart: {}", event.cartId());

        event.items().forEach(itemInfo -> {
            try {
                ReduceProductStockCommand cmd = new ReduceProductStockCommand(
                    itemInfo.productId().value().toString(),
                    itemInfo.quantity()
                );

                reduceProductStockUseCase.execute(cmd);

                logger.info("Reduced stock for product {} by {} units",
                    itemInfo.productId(), itemInfo.quantity());

            } catch (Exception e) {
                logger.error("Failed to reduce stock: {}", e.getMessage());
                // In production: publish CompensationEvent, trigger workflow,
                // or alert admins
            }
        });
    }
}
```

**Key Points:**
- Located in `adapter.incoming.event` package (incoming adapter for events)
- Uses **use case** to reduce stock (follows Clean Architecture)
- Handles **failures gracefully** (logs error, continues processing)
- In production: implement **compensation logic** or **workflow orchestration**

### 3. Use Case (Product Context)

**File:** `product/application/usecase/reduceproductstock/ReduceProductStockUseCase.java`

```java
@Service
@Transactional
public class ReduceProductStockUseCase implements ReduceProductStockInputPort {

    private final ProductRepository productRepository;

    @Override
    public ReduceProductStockResponse execute(ReduceProductStockCommand input) {
        ProductId productId = ProductId.of(input.productId());

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        int previousStock = product.stock().quantity();

        // Domain logic for reducing stock (with invariants)
        product.decreaseStock(input.quantity());

        productRepository.save(product);

        return new ReduceProductStockResponse(
            product.id().value().toString(),
            previousStock,
            product.stock().quantity()
        );
    }
}
```

## Architectural Patterns Applied

### 1. Event-Driven Architecture

```
┌─────────────────┐       Domain Event        ┌──────────────────┐
│  Cart Context   │──────────────────────────▶│ Product Context  │
│                 │    (CartCheckedOut)        │                  │
│ ┌─────────────┐ │                            │ ┌──────────────┐ │
│ │ ShoppingCart│ │  Publishes event           │ │CartEventListn│ │
│ │ .checkout() │ │  (fire and forget)         │ │ @EventListnr │ │
│ └─────────────┘ │                            │ └──────┬───────┘ │
│                 │                            │        │         │
│                 │                            │        ▼         │
│                 │                            │ ┌──────────────┐ │
│                 │                            │ │ReduceProduct │ │
│                 │                            │ │  StockUseCase│ │
│                 │                            │ └──────┬───────┘ │
│                 │                            │        │         │
│                 │                            │        ▼         │
│                 │                            │ ┌──────────────┐ │
│                 │                            │ │   Product    │ │
│                 │                            │ │.decreaseStock│ │
│                 │                            │ └──────────────┘ │
└─────────────────┘                            └──────────────────┘
     Publishes                                      Listens
```

### 2. Eventual Consistency

- Cart checkout completes **immediately**
- Stock reduction happens **asynchronously**
- System is **eventually consistent** (not immediately consistent)
- Trade-off: Accepts temporary inconsistency for better availability and scalability

### 3. Bounded Context Isolation

```
Product Context
├── domain
│   └── model
│       └── Product.java ✅ No dependencies on Cart
├── application
│   └── usecase
│       └── reduceproductstock/
│           └── ReduceProductStockUseCase.java ✅ No dependencies on Cart
└── adapter
    └── incoming
        └── event
            └── CartEventListener.java ⚠️  Listens to Cart events (allowed exception)

Cart Context
├── domain
│   ├── model
│   │   └── ShoppingCart.java ✅ No dependencies on Product
│   └── event
│       └── CartCheckedOut.java ⚠️  Uses Shared Kernel (ProductId) only
```

**Architecture Rule:**
- Contexts must **not** directly depend on each other
- **Exception:** Event listeners may access domain events from other contexts
- Events must use **Shared Kernel types** or **primitives** only (no cross-context domain objects)

## Testing the Integration

### Manual Test

1. Start the application:
   ```bash
   ./gradlew bootRun
   ```

2. Create a cart and add items:
   ```bash
   curl -u user:password -X POST "http://localhost:8080/api/carts?customerId=test-123"
   ```

3. Add product to cart:
   ```bash
   curl -u user:password -X POST "http://localhost:8080/api/carts/{cartId}/items" \
     -H "Content-Type: application/json" \
     -d '{"productId":"PROD-001", "quantity":2}'
   ```

4. Checkout cart:
   ```bash
   curl -u user:password -X POST "http://localhost:8080/api/carts/{cartId}/checkout"
   ```

5. Verify stock was reduced:
   ```bash
   curl -u user:password "http://localhost:8080/api/products/PROD-001"
   ```

### Expected Log Output

```
INFO  CartCheckedOut event raised for cart: abc-123
INFO  Received CartCheckedOut event for cart: abc-123
INFO  Reduced stock for product PROD-001 by 2 units
```

## Production Considerations

### 1. Idempotency

Events may be delivered multiple times. Ensure handlers are idempotent:

```java
@EventListener
public void onCartCheckedOut(CartCheckedOut event) {
    // Check if already processed
    if (processedEvents.contains(event.eventId())) {
        logger.warn("Event {} already processed, skipping", event.eventId());
        return;
    }

    // Process event
    // ...

    // Mark as processed
    processedEvents.add(event.eventId());
}
```

### 2. Error Handling & Compensation

```java
@EventListener
public void onCartCheckedOut(CartCheckedOut event) {
    event.items().forEach(item -> {
        try {
            reduceProductStockUseCase.execute(cmd);
        } catch (InsufficientStockException e) {
            // Publish compensation event
            eventPublisher.publish(StockReductionFailed.now(
                event.cartId(),
                item.productId(),
                "Insufficient stock"
            ));

            // Optionally: revert cart checkout
            // Optionally: notify customer
        }
    });
}
```

### 3. Event Store / Audit Trail

Consider persisting events for:
- **Debugging**: Replay events to understand what happened
- **Auditing**: Track all state changes
- **Event Sourcing**: Rebuild state from events
- **Analytics**: Analyze business metrics

### 4. Asynchronous Event Processing

For better scalability, use message queues:

```java
// Spring Integration / Spring Cloud Stream
@StreamListener(CartEvents.CHECKOUT_CHANNEL)
public void onCartCheckedOut(CartCheckedOut event) {
    // Process asynchronously
}
```

## Alternative Patterns Considered

### 1. Saga Pattern

For operations requiring multiple steps with rollback:

```
Cart Checkout → Reduce Stock → Process Payment → Ship Order
     ↓              ↓               ↓                ↓
  Success      Success         Failure          Canceled
                                  ↓
                           Compensate: Restore Stock
```

### 2. CQRS with Read Models

Product context maintains denormalized view:

```java
// Cart publishes events
// Product context updates read model
public class ProductStockReadModel {
    private Map<ProductId, Integer> stockLevels;

    @EventListener
    public void on(CartCheckedOut event) {
        event.items().forEach(item ->
            stockLevels.compute(item.productId(), (id, stock) -> stock - item.quantity())
        );
    }
}
```

### 3. Anti-Corruption Layer

Wrap external context access:

```java
// Cart context defines its own interface
public interface ProductStockService {
    void reduceStock(ProductId id, int quantity);
}

// Adapter translates to Product context
@Component
public class ProductStockAdapter implements ProductStockService {
    private final ReduceProductStockUseCase useCase;

    @Override
    public void reduceStock(ProductId id, int quantity) {
        useCase.execute(new ReduceProductStockCommand(id.toString(), quantity));
    }
}
```

## Summary

**✅ Benefits of Event-Driven Cross-Context Integration:**
- Loose coupling between bounded contexts
- Each context can evolve independently
- Easy to add new contexts that react to events
- Natural audit trail
- Supports eventual consistency

**⚠️  Trade-offs:**
- Eventual consistency (not immediate)
- More complex error handling
- Debugging can be harder (asynchronous)
- Need to handle duplicate/out-of-order events

**📚 References:**
- Eric Evans, *Domain-Driven Design* (Chapter 14: Strategic Design)
- Vaughn Vernon, *Implementing Domain-Driven Design* (Chapter 8: Domain Events)
- Martin Fowler, *Event-Driven Architecture* (martinfowler.com/articles/201701-event-driven.html)
