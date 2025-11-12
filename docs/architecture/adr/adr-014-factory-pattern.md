# ADR-014: Factory Pattern for Complex Aggregate Creation

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐

---

## Context

Creating domain aggregates sometimes involves:
- Complex validation rules
- ID generation
- Default values
- Domain event registration
- Multi-step initialization

### Direct Constructor Issues

```java
// ❌ Complex creation logic scattered
ProductId id = ProductId.generate();  // Where should ID generation live?
Product product = new Product(id, sku, name, description, price, category, stock);
product.registerEvent(ProductCreated.now(id, sku, name, price));  // Easy to forget!
```

**Problems**:
- Creation logic scattered
- Easy to forget steps (like event registration)
- Difficult to provide convenience methods
- Constructor gets complex

---

## Decision

**Use Factory pattern when aggregate creation is complex, involves ID generation, or raises creation events.**

### Factory Interface

```java
// domain/model/ddd/Factory.java
public interface Factory {}
```

### Factory Implementation

```java
// domain/model/product/ProductFactory.java
public class ProductFactory implements Factory {

  public Product createProduct(
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final ProductDescription description,
      @NonNull final Price price,
      @NonNull final Category category,
      @NonNull final ProductStock stock) {

    // 1. Generate ID
    final ProductId id = ProductId.generate();

    // 2. Create aggregate
    final Product product = new Product(id, sku, name, description, price, category, stock);

    // 3. Register creation event (consistent, never forgotten)
    product.registerEvent(ProductCreated.now(id, sku, name, price));

    return product;
  }

  // Convenience method for common case
  public Product createProductWithDefaults(
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final Price price) {

    return createProduct(
        sku,
        name,
        new ProductDescription(""),
        price,
        Category.UNCATEGORIZED,
        ProductStock.outOfStock()
    );
  }
}
```

---

## Rationale

### 1. **Encapsulates Complex Creation Logic**

All creation complexity in one place:
- ID generation
- Validation
- Default values
- Event registration

### 2. **Consistent Event Registration**

Events never forgotten:

```java
// ✅ Factory always registers creation event
Product product = productFactory.createProduct(...);
// ProductCreated event automatically registered

// ❌ Without factory, easy to forget
Product product = new Product(...);
// Oops, forgot to register ProductCreated event!
```

### 3. **Convenience Methods**

```java
// Simple case: use defaults
Product product = productFactory.createProductWithDefaults(sku, name, price);

// Complex case: full control
Product product = productFactory.createProduct(sku, name, description, price, category, stock);
```

### 4. **Testability**

```java
@Test
void factoryShouldRegisterCreationEvent() {
  // Given
  ProductFactory factory = new ProductFactory();

  // When
  Product product = factory.createProduct(...);

  // Then
  assertThat(product.domainEvents())
      .hasSize(1)
      .first()
      .isInstanceOf(ProductCreated.class);
}
```

---

## Consequences

### Positive

✅ **Centralized Creation Logic**: All creation in one place
✅ **Consistent Events**: Never forget to register creation event
✅ **Convenience**: Provide multiple creation methods
✅ **Testable**: Easy to verify creation logic
✅ **Framework-Free**: Pure domain logic

### Neutral

⚠️ **Extra Class**: One factory per aggregate
⚠️ **Indirection**: Call factory instead of constructor

### Negative

❌ **None identified**

---

## Implementation

### Current Factory

```java
// domain/model/product/ProductFactory.java
public class ProductFactory implements Factory {

  public Product createProduct(...) {
    final ProductId id = ProductId.generate();
    final Product product = new Product(id, sku, name, description, price, category, stock);
    product.registerEvent(ProductCreated.now(id, sku, name, price));
    return product;
  }

  public Product createProductWithDefaults(SKU sku, ProductName name, Price price) {
    return createProduct(
        sku, name,
        new ProductDescription(""),
        price,
        Category.UNCATEGORIZED,
        ProductStock.outOfStock()
    );
  }
}
```

### Usage in Use Case

```java
// application/usecase/createproduct/CreateProductUseCase.java
@Service
public class CreateProductUseCase implements CreateProductInputPort {

  public CreateProductResponse execute(CreateProductCommand command) {
    // Use factory for creation
    final Product product = productFactory.createProduct(...);

    productRepository.save(product);
    eventPublisher.publishAndClearEvents(product);

    return new CreateProductResponse(...);
  }
}
```

---

## References

- **Evans - DDD**: "Use Factory when creation is complex or exposes internal structure"
- **Vernon - Implementing DDD**: "Factories for complex creation"

### Related ADRs

- [ADR-005: Domain Events Publishing Strategy](adr-005-domain-events-publishing.md)

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
