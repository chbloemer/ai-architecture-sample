# ADR-006: Domain Events as Immutable Records

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

Domain Events represent **facts about things that happened in the domain**. Once something has happened, it cannot be changed - history is immutable.

### Design Decisions for Domain Events

1. **How to ensure immutability?** (classes vs records vs final fields)
2. **What data structure?** (class, record, interface)
3. **How to ensure value equality?** (equals/hashCode)
4. **How to name events?** (present vs past tense)

### Problem with Mutable Events

```java
// ❌ BAD: Mutable event
public class ProductPriceChanged implements DomainEvent {
  private UUID eventId;
  private Instant occurredOn;
  private ProductId productId;
  private Price oldPrice;
  private Price newPrice;  // Can be changed!

  public void setNewPrice(Price price) {
    this.newPrice = price;  // ❌ Event modified after creation
  }
}
```

**Problems**:
- Event can be modified after creation (history rewritten)
- Not thread-safe
- Defensive copying needed
- Intent unclear (is this mutable or not?)

---

## Decision

**All Domain Events MUST be implemented as Java records with immutable data.**

### Event Definition

```java
// ✅ GOOD: Immutable record
public record ProductPriceChanged(
    @NonNull UUID eventId,
    @NonNull Instant occurredOn,
    int version,
    @NonNull ProductId productId,
    @NonNull Price oldPrice,
    @NonNull Price newPrice
) implements DomainEvent {

  // Compact constructor for validation
  public ProductPriceChanged {
    if (productId == null) throw new IllegalArgumentException("Product ID required");
    if (oldPrice == null) throw new IllegalArgumentException("Old price required");
    if (newPrice == null) throw new IllegalArgumentException("New price required");
  }

  // Static factory method
  public static ProductPriceChanged now(
      final ProductId productId,
      final Price oldPrice,
      final Price newPrice) {
    return new ProductPriceChanged(
        UUID.randomUUID(),
        Instant.now(),
        1,
        productId,
        oldPrice,
        newPrice
    );
  }
}
```

### Required Fields

Every domain event MUST have:

1. **eventId** (`UUID`) - Unique event identifier
2. **occurredOn** (`Instant`) - When the event happened
3. **version** (`int`) - Event schema version
4. **Domain-specific fields** - What changed, why, context

### Naming Convention

Events MUST be named in **past tense** (something already happened):

✅ **Good Names**:
- `ProductCreated`
- `ProductPriceChanged`
- `ProductOutOfStock`
- `CartItemAddedToCart`
- `CartCheckedOut`

❌ **Bad Names**:
- `CreateProduct` (command, not event)
- `ChangePrice` (command)
- `ProductUpdate` (vague, not past tense)

---

## Rationale

### 1. **Immutability by Default**

Java records are immutable by default:

```java
// Record = final class + private final fields + accessors
public record ProductPriceChanged(...) {
  // All fields automatically final
  // No setters generated
  // Cannot be modified after creation
}
```

Equivalent verbose class:
```java
public final class ProductPriceChanged implements DomainEvent {
  private final UUID eventId;
  private final Instant occurredOn;
  private final int version;
  private final ProductId productId;
  private final Price oldPrice;
  private final Price newPrice;

  // Constructor, getters, equals, hashCode...
  // 50+ lines vs 10 lines with record
}
```

### 2. **Value Equality Automatic**

Records automatically implement `equals()` and `hashCode()` based on all fields:

```java
ProductPriceChanged event1 = new ProductPriceChanged(id, time, 1, productId, old, new);
ProductPriceChanged event2 = new ProductPriceChanged(id, time, 1, productId, old, new);

event1.equals(event2);  // true - same values
```

Critical for:
- Event deduplication
- Testing
- Event replay
- Event stores

### 3. **Thread Safety**

Immutable objects are inherently thread-safe:

```java
// Can be safely shared across threads
ProductPriceChanged event = ProductPriceChanged.now(...);

executor.submit(() -> listener1.handle(event));  // Thread 1
executor.submit(() -> listener2.handle(event));  // Thread 2

// No synchronization needed - event cannot change
```

### 4. **Clear Intent**

Record signals "this is immutable data":

```java
public record ProductPriceChanged(...) {
  // Developer sees "record" → knows it's immutable
  // No need to check for setters
  // Clear intent: this is a fact, not mutable state
}
```

### 5. **Compact Constructor Validation**

Records support compact constructor for validation:

```java
public record ProductPriceChanged(
    @NonNull UUID eventId,
    @NonNull Instant occurredOn,
    int version,
    @NonNull ProductId productId,
    @NonNull Price oldPrice,
    @NonNull Price newPrice
) implements DomainEvent {

  public ProductPriceChanged {  // Compact constructor
    if (productId == null) throw new IllegalArgumentException("Product ID required");
    if (oldPrice == null) throw new IllegalArgumentException("Old price required");
    if (newPrice == null) throw new IllegalArgumentException("New price required");
    // Validation runs before field assignment
  }
}
```

### 6. **Pattern Matching Ready**

Records work well with Java pattern matching:

```java
// Future: Pattern matching (Java 21+)
switch (event) {
  case ProductPriceChanged(var id, var time, _, var productId, var oldPrice, var newPrice) ->
      log.info("Price changed: {} -> {}", oldPrice, newPrice);
  case ProductCreated(var id, var time, _, var productId, var name, _) ->
      log.info("Product created: {}", name);
}
```

---

## Consequences

### Positive

✅ **Immutability Guaranteed**: Cannot be modified after creation
✅ **Thread-Safe**: Can be shared across threads safely
✅ **Concise**: 10 lines vs 50+ for equivalent class
✅ **Value Equality**: Automatic equals/hashCode
✅ **Clear Intent**: "record" signals immutability
✅ **Validation Support**: Compact constructor
✅ **Modern Java**: Leverages Java 14+ features
✅ **Pattern Matching**: Ready for future Java features

### Neutral

⚠️ **Java 14+ Required**: Needs modern Java version (we use Java 21)
⚠️ **All Fields Exposed**: All fields are public (acceptable for events)

### Negative

❌ **None identified** - Records are perfect for events

---

## Implementation

### Domain Event Interface

```java
// domain/model/ddd/DomainEvent.java
public interface DomainEvent {
  UUID eventId();
  Instant occurredOn();
  int version();
}
```

### Event Examples

**Product Created**:
```java
public record ProductCreated(
    @NonNull UUID eventId,
    @NonNull Instant occurredOn,
    int version,
    @NonNull ProductId productId,
    @NonNull SKU sku,
    @NonNull ProductName name,
    @NonNull Price price
) implements DomainEvent {

  public ProductCreated {
    if (productId == null) throw new IllegalArgumentException("Product ID required");
    if (sku == null) throw new IllegalArgumentException("SKU required");
    if (name == null) throw new IllegalArgumentException("Name required");
    if (price == null) throw new IllegalArgumentException("Price required");
  }

  public static ProductCreated now(
      final ProductId productId,
      final SKU sku,
      final ProductName name,
      final Price price) {
    return new ProductCreated(
        UUID.randomUUID(),
        Instant.now(),
        1,
        productId,
        sku,
        name,
        price
    );
  }
}
```

**Cart Item Added**:
```java
public record CartItemAddedToCart(
    @NonNull UUID eventId,
    @NonNull Instant occurredOn,
    int version,
    @NonNull CartId cartId,
    @NonNull ProductId productId,
    @NonNull Quantity quantity
) implements DomainEvent {

  public CartItemAddedToCart {
    if (cartId == null) throw new IllegalArgumentException("Cart ID required");
    if (productId == null) throw new IllegalArgumentException("Product ID required");
    if (quantity == null) throw new IllegalArgumentException("Quantity required");
  }

  public static CartItemAddedToCart now(
      final CartId cartId,
      final ProductId productId,
      final Quantity quantity) {
    return new CartItemAddedToCart(
        UUID.randomUUID(),
        Instant.now(),
        1,
        cartId,
        productId,
        quantity
    );
  }
}
```

### Usage in Aggregate

```java
public void changePrice(@NonNull final Price newPrice) {
  final Price oldPrice = this.price;
  this.price = newPrice;

  // Register immutable event
  registerEvent(ProductPriceChanged.now(this.id, oldPrice, newPrice));
  // Event cannot be modified after creation
}
```

### ArchUnit Enforcement

```groovy
// DddAdvancedPatternsArchUnitTest.groovy

def "Domain Events müssen DomainEvent Interface implementieren"() {
  expect:
  classes()
    .that().implement(DomainEvent.class)
    .should().beRecords()  // Must be records
    .check(allClasses)
}

def "Domain Events sollten Records sein (Immutabilität)"() {
  expect:
  classes()
    .that().resideInAPackage("..domain.model..")
    .and().haveSimpleNameEndingWith("Event")
    .or().haveSimpleNameMatching(".*Created|.*Changed|.*Deleted|.*Added|.*Removed")
    .should().beRecords()
    .check(allClasses)
}

def "Domain Event Namen sollten in Vergangenheit sein"() {
  expect:
  classes()
    .that().implement(DomainEvent.class)
    .should().haveSimpleNameMatching(".*Created|.*Changed|.*Deleted|.*Added|.*Removed|.*Checked.*|.*Updated")
    .check(allClasses)
}
```

---

## Alternatives Considered

### Alternative 1: Final Classes with Final Fields

```java
public final class ProductPriceChanged implements DomainEvent {
  private final UUID eventId;
  private final Instant occurredOn;
  // ... more boilerplate
}
```

**Rejected**:
- Much more verbose (50+ lines vs 10)
- Manual equals/hashCode implementation
- More error-prone
- Records are better for this use case

### Alternative 2: Lombok @Value

```java
@Value
public class ProductPriceChanged implements DomainEvent {
  UUID eventId;
  Instant occurredOn;
  // ...
}
```

**Rejected**:
- Dependency on Lombok
- Records are native Java
- Lombok adds complexity
- Records are the modern standard

### Alternative 3: Interfaces Only (No Implementation)

```java
public interface ProductPriceChanged extends DomainEvent {
  ProductId productId();
  Price oldPrice();
  Price newPrice();
}
```

**Rejected**:
- Need concrete implementation anyway
- More complexity
- Records provide implementation + interface

---

## References

### Domain-Driven Design Books

1. **Eric Evans - Domain-Driven Design (2003)**:
   > "Domain Events are immutable. They represent something that happened in the past."

2. **Vaughn Vernon - Implementing Domain-Driven Design (2013)**:
   > "Domain Events should be immutable... Use Value Objects to model Events."

3. **Vaughn Vernon - Domain-Driven Design Distilled (2016)**:
   > "Events are facts. Facts don't change."

### Java Language

- **JEP 395: Records** (Java 16)
- **JEP 409: Sealed Classes** (Java 17)
- **Pattern Matching** (Java 21+)

### Related Patterns

- **Value Object**: Events are value objects
- **Event Sourcing**: Immutable events as source of truth
- **Immutable Object**: Thread-safe, simple reasoning

### Related ADRs

- [ADR-005: Domain Events Publishing Strategy](adr-005-domain-events-publishing.md)
- [ADR-009: Value Objects as Java Records](adr-009-value-objects-as-records.md)

---

## Validation

### ArchUnit Tests

```bash
./gradlew test-architecture

# Expected:
# Domain Events sollten Records sein PASSED ✅
# Domain Event Namen sollten in Vergangenheit sein PASSED ✅
```

### Unit Tests

```java
@Test
void eventsShouldBeImmutable() {
  ProductPriceChanged event = ProductPriceChanged.now(
      productId,
      Price.of(Money.euro(new BigDecimal("99.99"))),
      Price.of(Money.euro(new BigDecimal("79.99")))
  );

  // Cannot modify - no setters exist
  // All fields are final
  assertThat(event.productId()).isNotNull();
  assertThat(event.oldPrice()).isNotNull();
  assertThat(event.newPrice()).isNotNull();
}

@Test
void eventsShouldHaveValueEquality() {
  UUID id = UUID.randomUUID();
  Instant time = Instant.now();

  ProductPriceChanged event1 = new ProductPriceChanged(id, time, 1, productId, oldPrice, newPrice);
  ProductPriceChanged event2 = new ProductPriceChanged(id, time, 1, productId, oldPrice, newPrice);

  assertThat(event1).isEqualTo(event2);
  assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
}
```

---

## Review and Update

**Next Review**: January 24, 2026
**Review Frequency**: Annually or when Java version changes

**Update Criteria**:
- New Java features for records
- Pattern matching adoption
- Serialization requirements change
- Event sourcing adoption

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
