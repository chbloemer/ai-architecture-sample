# ADR-013: Specification Pattern for Business Rules

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐

---

## Context

Complex business rules need to be:
- **Explicit**: Clear and visible in code
- **Reusable**: Used across multiple use cases
- **Testable**: Verified in isolation
- **Composable**: Combined with AND/OR/NOT logic

### Traditional Approach

```java
// ❌ Business rules scattered in code
public List<Product> findAvailableProducts() {
  return products.stream()
      .filter(p -> p.stock() > 0)  // Rule hidden in lambda
      .filter(p -> p.price().amount().compareTo(BigDecimal.ZERO) > 0)
      .collect(Collectors.toList());
}
```

**Problems**:
- Rules embedded in code
- Not reusable
- Hard to test
- Not composable

---

## Decision

**Use Specification Pattern to encapsulate business rules as objects.**

### Specification Interface

```java
// domain/model/ddd/Specification.java
public interface Specification<T> {

  boolean isSatisfiedBy(@NonNull T candidate);

  default Specification<T> and(@NonNull final Specification<T> other) {
    return candidate -> isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
  }

  default Specification<T> or(@NonNull final Specification<T> other) {
    return candidate -> isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
  }

  default Specification<T> not() {
    return candidate -> !isSatisfiedBy(candidate);
  }
}
```

### Example Specification

```java
public class ProductAvailabilitySpecification implements Specification<Product> {

  private final int minimumStock;

  public ProductAvailabilitySpecification(final int minimumStock) {
    if (minimumStock < 0) {
      throw new IllegalArgumentException("Minimum stock cannot be negative");
    }
    this.minimumStock = minimumStock;
  }

  @Override
  public boolean isSatisfiedBy(@NonNull final Product product) {
    return product.isAvailable() && product.hasStockFor(minimumStock);
  }

  public static ProductAvailabilitySpecification available() {
    return new ProductAvailabilitySpecification(1);
  }

  public static ProductAvailabilitySpecification inStock(final int quantity) {
    return new ProductAvailabilitySpecification(quantity);
  }
}
```

---

## Rationale

### 1. **Explicit Business Rules**

```java
// ✅ Rule is explicit and named
Specification<Product> availableProducts = ProductAvailabilitySpecification.available();

List<Product> products = repository.findAll().stream()
    .filter(availableProducts::isSatisfiedBy)
    .collect(Collectors.toList());
```

### 2. **Composability**

```java
// Compose complex rules from simple ones
Specification<Product> affordableElectronics =
    new ProductCategorySpecification(Category.ELECTRONICS)
        .and(ProductAvailabilitySpecification.available())
        .and(new ProductPriceRangeSpecification(
            Money.euro(new BigDecimal("100")),
            Money.euro(new BigDecimal("500"))
        ));

List<Product> products = repository.findAll().stream()
    .filter(affordableElectronics::isSatisfiedBy)
    .collect(Collectors.toList());
```

### 3. **Testability**

```java
@Test
void shouldSatisfyWhenProductIsAvailable() {
  // Given
  Product product = createProductWithStock(10);
  Specification<Product> spec = ProductAvailabilitySpecification.available();

  // When
  boolean satisfied = spec.isSatisfiedBy(product);

  // Then
  assertThat(satisfied).isTrue();
}
```

### 4. **Reusability**

Same specification used in multiple places:
- Filtering collections
- Validation rules
- Repository queries
- Business rule enforcement

---

## Consequences

### Positive

✅ **Explicit Rules**: Business rules are visible objects
✅ **Composable**: Combine with AND/OR/NOT
✅ **Testable**: Test rules in isolation
✅ **Reusable**: Use across multiple use cases
✅ **Domain Language**: Specifications have business names

### Neutral

⚠️ **More Classes**: Each specification is a class
⚠️ **Learning Curve**: Pattern requires understanding

### Negative

❌ **None identified**

---

## Implementation

### Current Specifications

```java
public class ProductAvailabilitySpecification implements Specification<Product> {
  public boolean isSatisfiedBy(Product product) {
    return product.isAvailable() && product.hasStockFor(minimumStock);
  }
}

public class ProductCategorySpecification implements Specification<Product> {
  public boolean isSatisfiedBy(Product product) {
    return product.category().equals(this.category);
  }
}

public class ProductPriceRangeSpecification implements Specification<Product> {
  public boolean isSatisfiedBy(Product product) {
    return product.price().isBetween(minPrice, maxPrice);
  }
}
```

### Usage Example

```java
// Application Service
public List<Product> findAffordableElectronics() {
  Specification<Product> spec =
      new ProductCategorySpecification(Category.ELECTRONICS)
          .and(ProductAvailabilitySpecification.available())
          .and(new ProductPriceRangeSpecification(
              Money.euro(new BigDecimal("100")),
              Money.euro(new BigDecimal("500"))
          ));

  return productRepository.findAll().stream()
      .filter(spec::isSatisfiedBy)
      .collect(Collectors.toList());
}
```

---

## References

- **Evans - DDD**: "Specification encapsulates business rules for validation and selection"
- **Fowler - Specification Pattern**: https://martinfowler.com/apsupp/spec.pdf

### Related ADRs

- [ADR-010: Domain Services](adr-010-domain-services-multi-aggregate.md)

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
