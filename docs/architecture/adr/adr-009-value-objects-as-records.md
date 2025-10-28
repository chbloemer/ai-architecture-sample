# ADR-009: Value Objects as Java Records

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

Value Objects are fundamental DDD building blocks that represent descriptive aspects of the domain with no conceptual identity.

**Vernon's Value Object Characteristics**:
- **Immutable**: Cannot be changed after creation
- **Value Equality**: Equality based on all attributes, not identity
- **Self-Validating**: Validate invariants in constructor
- **Side-Effect Free**: Methods don't modify state

### Implementation Options

**Option 1: Traditional Class**
```java
public final class Money {
  private final BigDecimal amount;
  private final Currency currency;

  public Money(BigDecimal amount, Currency currency) {
    this.amount = amount;
    this.currency = currency;
  }

  public BigDecimal amount() { return amount; }
  public Currency currency() { return currency; }

  @Override
  public boolean equals(Object o) {
    // ... 10 lines of equals implementation
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency);
  }

  // 40+ lines total
}
```

**Option 2: Java Record**
```java
public record Money(
    @NonNull BigDecimal amount,
    @NonNull Currency currency
) implements Value {
  // 10 lines total - immutable, equals/hashCode automatic!
}
```

---

## Decision

**Value Objects SHOULD be implemented as Java records whenever possible.**

### Implementation Pattern

```java
public record Money(
    @NonNull BigDecimal amount,
    @NonNull Currency currency
) implements Value {

  // Compact constructor for validation
  public Money {
    if (amount == null) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    if (currency == null) {
      throw new IllegalArgumentException("Currency cannot be null");
    }
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }
    // Normalize scale
    amount = amount.setScale(2, RoundingMode.HALF_UP);
  }

  // Factory methods
  public static Money euro(final BigDecimal amount) {
    return new Money(amount, Currency.getInstance("EUR"));
  }

  // Business operations (side-effect free)
  public Money add(final Money other) {
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException("Cannot add money with different currencies");
    }
    return new Money(this.amount.add(other.amount), this.currency);
  }

  public Money multiply(final int factor) {
    return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
  }
}
```

---

## Rationale

### 1. **Immutability by Default**

Records are immutable automatically:
- All fields are `private final`
- No setters generated
- Cannot be modified after creation

### 2. **Concise Code**

```java
// Record: 10 lines
public record ProductId(@NonNull UUID value) implements Id {
  public static ProductId generate() {
    return new ProductId(UUID.randomUUID());
  }
}

// Class equivalent: 30+ lines
public final class ProductId implements Id {
  private final UUID value;

  public ProductId(UUID value) {
    this.value = value;
  }

  public UUID value() { return value; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProductId)) return false;
    ProductId that = (ProductId) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  public static ProductId generate() {
    return new ProductId(UUID.randomUUID());
  }
}
```

### 3. **Value Equality Automatic**

Records automatically implement `equals()` and `hashCode()` based on all fields:

```java
Money m1 = new Money(new BigDecimal("10.00"), Currency.getInstance("EUR"));
Money m2 = new Money(new BigDecimal("10.00"), Currency.getInstance("EUR"));

m1.equals(m2);  // true - value equality
```

### 4. **Compact Constructor Validation**

```java
public record Price(@NonNull Money amount) implements Value {
  public Price {  // Compact constructor
    if (amount == null) {
      throw new IllegalArgumentException("Amount required");
    }
    if (amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
  }
}
```

---

## Consequences

### Positive

✅ **Less Boilerplate**: 70% less code than traditional classes
✅ **Immutability Guaranteed**: Final fields, no setters
✅ **Value Equality**: Automatic equals/hashCode
✅ **Clear Intent**: "record" signals value object
✅ **Thread-Safe**: Immutable = thread-safe
✅ **Modern Java**: Uses Java 14+ features

### Neutral

⚠️ **Java 14+ Required**: Project uses Java 21 ✓
⚠️ **All Fields Public**: Acceptable for value objects

### Negative

❌ **None for value objects**

---

## Implementation

### Current Value Objects (All Records)

**Identity Value Objects**:
```java
public record ProductId(@NonNull UUID value) implements Id { ... }
public record CartId(@NonNull UUID value) implements Id { ... }
public record CartItemId(@NonNull UUID value) implements Id { ... }
public record CustomerId(@NonNull UUID value) implements Id { ... }
```

**Domain Value Objects**:
```java
public record Money(@NonNull BigDecimal amount, @NonNull Currency currency) implements Value { ... }
public record Price(@NonNull Money amount) implements Value { ... }
public record Quantity(int value) implements Value { ... }
public record ProductName(@NonNull String value) implements Value { ... }
public record SKU(@NonNull String value) implements Value { ... }
public record Category(@NonNull String name) implements Value { ... }
```

---

## References

### Books

- **Vernon - Implementing DDD**: "Value Objects should be immutable"
- **Evans - DDD**: "Value Objects should be lightweight and have value equality"

### Related ADRs

- [ADR-006: Domain Events as Immutable Records](adr-006-domain-events-immutable-records.md)

---

## Validation

```bash
./gradlew test-architecture
# "Value Object classes should be final (immutability)" PASSED ✅
```

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
