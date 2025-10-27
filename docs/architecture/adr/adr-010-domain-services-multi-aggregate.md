# ADR-010: Domain Services Only for Multi-Aggregate Operations

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐

---

## Context

In DDD, most business logic belongs in Aggregates, Entities, or Value Objects. However, some operations don't naturally fit into any single domain object.

**The Question**: When should we use Domain Services?

### The Anemic Domain Anti-Pattern

```java
// ❌ BAD: Anemic domain (all logic in services)
public class Product {
  private Price price;
  // Just getters and setters - no behavior
  public Price getPrice() { return price; }
  public void setPrice(Price price) { this.price = price; }
}

public class PricingService {
  public Price calculateDiscountedPrice(Product product, BigDecimal discount) {
    // Business logic in service, not domain
  }
}
```

**Problem**: Domain objects become data containers, all logic in services.

---

## Decision

**Domain Services should be RARE and only used when:**

1. **Operation spans multiple aggregates**
2. **Operation doesn't naturally belong to any single entity or value object**
3. **Operation represents a domain concept that is naturally stateless**

**When NOT to use**:
- Logic that belongs to an aggregate
- CRUD operations (use repositories)
- Application orchestration (use application services)

---

## Rationale

### 1. **Most Logic Belongs in Domain Objects**

```java
// ✅ GOOD: Logic in domain object
public class Product {
  public void changePrice(Price newPrice) {
    if (newPrice.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
    this.price = newPrice;
    registerEvent(ProductPriceChanged.now(this.id, oldPrice, newPrice));
  }
}
```

### 2. **Use Domain Services for Multi-Aggregate Operations**

```java
// ✅ GOOD: Service for operation spanning multiple concepts
public class PricingService implements DomainService {

  public Price calculateDiscountedPrice(
      final Price originalPrice,
      final BigDecimal discountPercentage) {

    if (discountPercentage.compareTo(BigDecimal.ZERO) < 0 ||
        discountPercentage.compareTo(new BigDecimal("100")) > 0) {
      throw new IllegalArgumentException("Discount must be between 0 and 100");
    }

    final BigDecimal multiplier = BigDecimal.ONE
        .subtract(discountPercentage.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));

    return originalPrice.multiply(multiplier);
  }

  // Involves both Price AND Category - doesn't belong to either alone
  public boolean isPriceValid(final Price price, final Category category) {
    return price.amount().compareTo(category.minimumPrice()) >= 0;
  }
}
```

---

## Consequences

### Positive

✅ **Rich Domain Models**: Logic stays in domain objects
✅ **Clear Separation**: Easy to identify when services are needed
✅ **Avoid Anemic Domain**: Prevents over-use of services

### Neutral

⚠️ **Judgment Required**: Team must decide where logic belongs

### Negative

❌ **None identified**

---

## Implementation

### Domain Service Marker

```java
// domain/model/ddd/DomainService.java
public interface DomainService {}
```

### Current Domain Services

**PricingService**:
```java
public class PricingService implements DomainService {
  // Discount calculation spans Price and discount rules
  public Price calculateDiscountedPrice(Price price, BigDecimal discount) { ... }
  public boolean isPriceValid(Price price, Category category) { ... }
}
```

**CartTotalCalculator**:
```java
public class CartTotalCalculator implements DomainService {
  // Calculation requires cart items AND product prices
  public Money calculateTotal(List<CartItem> items, Map<ProductId, Price> prices) { ... }
}
```

### ArchUnit Enforcement

```groovy
def "Domain Services müssen DomainService Interface implementieren"() { ... }
def "Domain Services müssen im domain.model Package liegen"() { ... }
def "Domain Services sollten zustandslos sein"() { ... }
```

---

## References

- **Evans - DDD**: "Use Domain Service when operation doesn't belong to any Entity or Value Object"
- **Vernon - Implementing DDD**: "Domain Services should be rare"

### Related ADRs

- [ADR-002: Framework-Independent Domain Layer](adr-002-framework-independent-domain.md)

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
