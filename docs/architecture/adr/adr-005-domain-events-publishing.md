# ADR-005: Domain Events Publishing Strategy

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐⭐

---

## Context

Domain Events represent something significant that happened in the domain. They are critical for:
- Eventual consistency between aggregates
- Integration between bounded contexts
- Audit trails and event sourcing
- Decoupling and reactive architectures

A critical design decision: **When should domain events be published?**

### Option 1: Publish Immediately

```java
public void changePrice(Price newPrice) {
  this.price = newPrice;
  eventPublisher.publish(ProductPriceChanged.now(this.id, oldPrice, newPrice));  // Immediate
}
```

### Option 2: Register Now, Publish Later

```java
public void changePrice(Price newPrice) {
  this.price = newPrice;
  registerEvent(ProductPriceChanged.now(this.id, oldPrice, newPrice));  // Register only
  // Published later by application service
}
```

### Problems with Immediate Publishing

1. **Ghost Events**: Events published for failed transactions
   ```java
   product.changePrice(newPrice);  // Publishes event immediately
   repository.save(product);  // Transaction fails! ❌
   // Event already published, but change not persisted
   // Listeners react to event that didn't actually happen
   ```

2. **Framework Coupling**: Domain needs access to event publisher
   ```java
   public class Product {
     private final EventPublisher publisher;  // ❌ Infrastructure in domain
   }
   ```

3. **Testing Complexity**: Must mock event publisher in domain tests
4. **Ordering Issues**: Events may be published before persistence completes

---

## Decision

**Domain aggregates REGISTER events during mutations, but events are PUBLISHED only after successful persistence.**

### Two-Phase Process

**Phase 1: Domain - Register Events**
```java
// domain/model/product/Product.java
public void changePrice(@NonNull final Price newPrice) {
  final Price oldPrice = this.price;
  this.price = newPrice;

  // Register event for later publication
  registerEvent(ProductPriceChanged.now(this.id, oldPrice, newPrice));
}
```

**Phase 2: Application Service - Publish After Save**
```java
// application/ProductApplicationService.java
public void changeProductPrice(ProductId productId, Price newPrice) {
  Product product = productRepository.findById(productId)
      .orElseThrow();

  product.changePrice(newPrice);  // Registers event

  productRepository.save(product);  // Persist first

  eventPublisher.publishAndClearEvents(product);  // Publish after successful save
}
```

### Base Aggregate Implementation

```java
// domain/model/ddd/BaseAggregateRoot.java
public abstract class BaseAggregateRoot<T extends AggregateRoot<T, ID>, ID extends Id>
    implements AggregateRoot<T, ID> {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

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
```

### Event Publisher (Infrastructure SPI)

```java
// infrastructure/api/DomainEventPublisher.java
public interface DomainEventPublisher {

  void publish(@NonNull DomainEvent event);

  void publishAndClearEvents(@NonNull AggregateRoot<?, ?> aggregate);
}
```

```java
// infrastructure/config/SpringDomainEventPublisher.java
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void publish(@NonNull final DomainEvent event) {
    eventPublisher.publishEvent(event);
  }

  @Override
  public void publishAndClearEvents(@NonNull final AggregateRoot<?, ?> aggregate) {
    aggregate.domainEvents().forEach(this::publish);
    aggregate.clearDomainEvents();
  }
}
```

---

## Rationale

### 1. **Reliability - No Ghost Events**

Events are only published if persistence succeeds:

```java
try {
  product.changePrice(newPrice);     // Registers event
  productRepository.save(product);   // Might fail
  eventPublisher.publishAndClearEvents(product);  // Only if save succeeded
} catch (PersistenceException e) {
  // Event NOT published - correct!
  // No ghost events
}
```

### 2. **Domain Purity - Framework Independence**

Domain has no dependency on event publisher:

```java
// ✅ Pure domain - no infrastructure
public class Product extends BaseAggregateRoot<Product, ProductId> {
  // No EventPublisher dependency
  // No Spring annotations
  // Just business logic
}
```

### 3. **Transactional Consistency**

Events published within same transaction as persistence:

```java
@Transactional
public void changeProductPrice(ProductId productId, Price newPrice) {
  Product product = productRepository.findById(productId).orElseThrow();

  product.changePrice(newPrice);          // Register
  productRepository.save(product);        // Persist
  eventPublisher.publishAndClearEvents(product);  // Publish

  // All in same transaction
  // Rollback discards events too
}
```

### 4. **Testability**

Domain tests don't need event publisher:

```java
@Test
void shouldRegisterPriceChangedEvent() {
  // Given
  Product product = createTestProduct();

  // When
  product.changePrice(newPrice);

  // Then
  assertThat(product.domainEvents())
      .hasSize(1)
      .first()
      .isInstanceOf(ProductPriceChanged.class);

  // No event publisher needed!
}
```

### 5. **Clear Separation of Concerns**

- **Domain**: Knows WHAT happened (registers events)
- **Application**: Knows WHEN to publish (after persistence)
- **Infrastructure**: Knows HOW to publish (Spring events, message queue, etc.)

### 6. **Correct Ordering**

```
1. Domain Logic    → Product.changePrice()
2. Register Event  → registerEvent(ProductPriceChanged)
3. Persist State   → productRepository.save()
4. Publish Events  → eventPublisher.publishAndClearEvents()
```

This ordering guarantees:
- Events represent persisted state
- Listeners see consistent data
- No race conditions

---

## Consequences

### Positive

✅ **No Ghost Events**: Events only published for persisted changes
✅ **Framework Independence**: Domain doesn't know about publishers
✅ **Transactional Consistency**: Events and persistence in same transaction
✅ **Easy Testing**: Domain tests don't need event infrastructure
✅ **Clear Responsibility**: Domain registers, application publishes
✅ **Reliable**: Guaranteed that event = persisted fact
✅ **Flexible**: Can change publishing mechanism without changing domain

### Neutral

⚠️ **Application Service Responsibility**: Must remember to publish events
⚠️ **Two-Step Process**: Register → Publish (not immediate)

### Negative

❌ **None identified** - This is the recommended pattern from DDD literature

---

## Implementation

### Aggregate Root Interface

```java
// domain/model/ddd/AggregateRoot.java
public interface AggregateRoot<T extends AggregateRoot<T, ID>, ID extends Id>
    extends Entity<T, ID> {

  List<DomainEvent> domainEvents();

  void clearDomainEvents();
}
```

### Usage in Domain

```java
// domain/model/product/Product.java
public void changePrice(@NonNull final Price newPrice) {
  if (newPrice == null) {
    throw new IllegalArgumentException("New price cannot be null");
  }

  final Price oldPrice = this.price;
  this.price = newPrice;

  registerEvent(ProductPriceChanged.now(this.id, oldPrice, newPrice));
}

public void updateStock(@NonNull final ProductStock newStock) {
  if (newStock == null) {
    throw new IllegalArgumentException("New stock cannot be null");
  }

  final ProductStock oldStock = this.stock;
  this.stock = newStock;

  if (oldStock.isAvailable() && !newStock.isAvailable()) {
    registerEvent(ProductOutOfStock.now(this.id));
  }
}
```

### Usage in Application Service

```java
// application/ProductApplicationService.java
@Service
public class ProductApplicationService {

  private final ProductRepository productRepository;
  private final ProductFactory productFactory;
  private final DomainEventPublisher eventPublisher;

  public Product createProduct(...) {
    // Domain creates and registers creation event
    final Product product = productFactory.createProduct(...);

    // Persist
    productRepository.save(product);

    // Publish events AFTER successful persistence
    eventPublisher.publishAndClearEvents(product);

    return product;
  }

  public void changeProductPrice(ProductId productId, Price newPrice) {
    // Load
    final Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));

    // Execute domain logic (registers event)
    product.changePrice(newPrice);

    // Persist
    productRepository.save(product);

    // Publish AFTER successful persistence
    eventPublisher.publishAndClearEvents(product);
  }
}
```

### Event Listeners

```java
// infrastructure/event/ProductEventListener.java
@Component
public class ProductEventListener {

  private static final Logger log = LoggerFactory.getLogger(ProductEventListener.class);

  @EventListener
  public void on(final ProductCreated event) {
    log.info("Product created: {} - {}", event.productId(), event.name());
    // Update search index
    // Clear cache
    // Notify subscribers
  }

  @EventListener
  public void on(final ProductPriceChanged event) {
    log.info("Price changed for product {}: {} -> {}",
        event.productId(), event.oldPrice(), event.newPrice());
    // Update price index
    // Notify price watchers
    // Invalidate cart calculations
  }

  @EventListener
  @Async
  public void on(final ProductOutOfStock event) {
    log.warn("Product out of stock: {}", event.productId());
    // Send notifications
    // Update availability index
  }
}
```

---

## Alternatives Considered

### Alternative 1: Publish Immediately in Domain

```java
public void changePrice(Price newPrice, EventPublisher publisher) {
  this.price = newPrice;
  publisher.publish(ProductPriceChanged.now(...));  // Immediate
}
```

**Rejected**:
- Ghost events if transaction fails
- Domain coupled to infrastructure
- Hard to test
- No transaction safety

### Alternative 2: Outbox Pattern with Database Table

```java
// Save events to database table in same transaction
productRepository.save(product);
eventOutboxRepository.save(product.domainEvents());

// Separate process publishes from outbox table
```

**Considered for Future**:
- Good for distributed systems
- Guaranteed delivery
- More complex
- Consider for microservices migration

### Alternative 3: Event Sourcing

```java
// Store events as source of truth
eventStore.append(product.id(), product.domainEvents());
```

**Not Needed Now**:
- Significant complexity
- Different persistence model
- May consider in future

---

## References

### Domain-Driven Design Books

1. **Vaughn Vernon - Implementing Domain-Driven Design (2013)**, Chapter 8:
   > "Domain Events should be published by the Application Service after the Aggregate has been successfully saved."

2. **Vaughn Vernon - Domain-Driven Design Distilled (2016)**:
   > "Publish Events after persistence succeeds, to prevent ghost events."

3. **Eric Evans - Domain-Driven Design (2003)**:
   > "Events capture facts about things that happened in the domain."

### Related Patterns

- **Transactional Outbox Pattern**: Reliable event publishing
- **Event Sourcing**: Events as source of truth
- **CQRS**: Separate read/write models via events
- **Saga Pattern**: Long-running transactions via events

### Related ADRs

- [ADR-003: Aggregate Reference by Identity Only](adr-003-aggregate-reference-by-id.md)
- [ADR-006: Domain Events as Immutable Records](adr-006-domain-events-immutable-records.md)

### External References

- [Domain Events Pattern](https://martinfowler.com/eaaDev/DomainEvent.html)
- [Reliable Event Publishing](https://microservices.io/patterns/data/transactional-outbox.html)

---

## Validation

### Tests

```java
@Test
void shouldRegisterEventButNotPublishImmediately() {
  // Given
  Product product = createTestProduct();

  // When
  product.changePrice(newPrice);

  // Then - event registered but not published
  assertThat(product.domainEvents()).hasSize(1);
}

@Test
void shouldPublishEventsAfterSuccessfulSave() {
  // Given
  Product product = createTestProduct();
  product.changePrice(newPrice);

  // When
  productRepository.save(product);
  eventPublisher.publishAndClearEvents(product);

  // Then - events published
  verify(eventPublisher).publishAndClearEvents(product);
  assertThat(product.domainEvents()).isEmpty();  // Cleared
}

@Test
void shouldNotPublishEventsIfSaveFails() {
  // Given
  Product product = createTestProduct();
  product.changePrice(newPrice);

  when(productRepository.save(any())).thenThrow(new PersistenceException());

  // When/Then
  assertThatThrownBy(() -> {
    productRepository.save(product);
    eventPublisher.publishAndClearEvents(product);  // Never reached
  }).isInstanceOf(PersistenceException.class);

  // Events NOT published - correct!
  verify(eventPublisher, never()).publishAndClearEvents(any());
}
```

---

## Review and Update

**Next Review**: January 24, 2026
**Review Frequency**: Annually or when considering event sourcing

**Update Criteria**:
- Move to microservices (consider Outbox pattern)
- Event sourcing adoption
- Message queue integration
- Distributed transactions needed

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
