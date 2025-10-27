# ADR-004: Persistence-Oriented Repository Pattern

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

Vaughn Vernon in "Implementing Domain-Driven Design" describes two repository styles:

### 1. Collection-Oriented Repository

Simulates an in-memory collection (Set):

```java
// Collection-oriented style
productRepository.add(product);      // Like Set.add()
productRepository.remove(product);   // Like Set.remove()

// Changes are automatically tracked and persisted
product.changePrice(newPrice);  // Persistence happens automatically (JPA)
```

**Characteristics**:
- Repositories act like in-memory collections
- Implicit persistence (e.g., JPA dirty checking)
- No explicit save operation
- Changes tracked automatically

### 2. Persistence-Oriented Repository

Explicit persistence operations:

```java
// Persistence-oriented style
productRepository.save(product);     // Explicit save
productRepository.delete(product);   // Explicit delete

// Changes must be explicitly saved
product.changePrice(newPrice);
productRepository.save(product);  // Must explicitly save
```

**Characteristics**:
- Explicit `save()` method
- Clear persistence points
- No automatic change tracking
- Explicit control over transactions

### The Decision Point

We must choose which style to use for this project.

---

## Decision

**We will use the Persistence-Oriented repository style with explicit `save()` operations.**

**Repository Base Interface**:
```java
// domain/model/ddd/Repository.java
public interface Repository<T extends AggregateRoot<T, ID>, ID extends Id> {

  void save(@NonNull T aggregate);

  Optional<T> findById(@NonNull ID id);

  void deleteById(@NonNull ID id);

  List<T> findAll();
}
```

**Usage Pattern**:
```java
// Application Service
public Product createProduct(...) {
  Product product = productFactory.createProduct(...);

  productRepository.save(product);  // ✅ Explicit save

  eventPublisher.publishAndClearEvents(product);

  return product;
}

public void changeProductPrice(ProductId productId, Price newPrice) {
  Product product = productRepository.findById(productId)
      .orElseThrow(() -> new ProductNotFoundException(productId));

  product.changePrice(newPrice);

  productRepository.save(product);  // ✅ Explicit save

  eventPublisher.publishAndClearEvents(product);
}
```

---

## Rationale

### 1. **Explicit Control Over Persistence**

Clear visibility of when persistence happens:

```java
// ✅ Clear persistence point
product.changePrice(newPrice);  // Domain logic
productRepository.save(product);  // Persistence (transaction boundary)
eventPublisher.publishAndClearEvents(product);  // Events (after persistence)
```

Benefits:
- Know exactly when database writes occur
- Control transaction boundaries explicitly
- Publish events after successful persistence
- No hidden JPA magic

### 2. **Framework Independence**

Collection-oriented style requires framework support (JPA dirty checking):
```java
// Requires JPA/Hibernate for automatic tracking
@Transactional
public void changePrice(ProductId id, Price newPrice) {
  Product product = productRepository.findById(id);  // JPA tracks this
  product.changePrice(newPrice);  // JPA detects change
  // Save happens automatically on transaction commit
}
```

Persistence-oriented style is framework-agnostic:
```java
// Works with any persistence mechanism
public void changePrice(ProductId id, Price newPrice) {
  Product product = productRepository.findById(id);
  product.changePrice(newPrice);
  productRepository.save(product);  // Explicit, no framework magic
}
```

### 3. **Better Fit for Event Publishing**

Events should be published AFTER successful persistence:

```java
// ✅ Persistence-oriented: Clear sequence
product.changePrice(newPrice);          // 1. Domain logic (registers event)
productRepository.save(product);        // 2. Persist (transaction)
eventPublisher.publishAndClearEvents(product);  // 3. Publish (after success)
```

```java
// ❌ Collection-oriented: When to publish?
@Transactional
public void changePrice(ProductId id, Price newPrice) {
  Product product = productRepository.findById(id);
  product.changePrice(newPrice);
  // When does persistence happen? End of transaction?
  // When should events be published?
}
```

### 4. **Simpler Implementation**

Persistence-oriented repositories are simpler to implement:

```java
@Repository
public class InMemoryProductRepository implements ProductRepository {

  private final Map<ProductId, Product> products = new ConcurrentHashMap<>();

  @Override
  public void save(@NonNull final Product product) {
    products.put(product.id(), product);  // Simple!
  }

  @Override
  public Optional<Product> findById(@NonNull final ProductId id) {
    return Optional.ofNullable(products.get(id));
  }
}
```

No change tracking, no proxy objects, no complexity.

### 5. **Clear Intent**

```java
// Intent is crystal clear
product.changePrice(newPrice);
productRepository.save(product);  // "I want this persisted NOW"
```

vs.

```java
// Intent is implicit
product.changePrice(newPrice);
// Will this be saved? When? How do I know?
```

---

## Consequences

### Positive

✅ **Explicit Transactions**: Clear transaction boundaries
✅ **Framework Agnostic**: Not tied to JPA dirty checking
✅ **Event Publishing**: Natural fit for event-driven architecture
✅ **Testability**: Easy to verify save() is called
✅ **Clarity**: Obvious when persistence occurs
✅ **Simplicity**: Simpler repository implementations
✅ **Control**: Full control over when/how entities are saved

### Neutral

⚠️ **Manual Save Calls**: Must remember to call save() explicitly
⚠️ **More Verbose**: Extra line of code for save()

### Negative

❌ **Not True Collection Illusion**: Not a perfect in-memory collection metaphor

---

## Implementation

### Repository Interface

```java
// domain/model/ddd/Repository.java
public interface Repository<T extends AggregateRoot<T, ID>, ID extends Id> {

  void save(@NonNull T aggregate);

  Optional<T> findById(@NonNull ID id);

  void deleteById(@NonNull ID id);

  List<T> findAll();
}
```

### Domain-Specific Repository

```java
// domain/model/product/ProductRepository.java
public interface ProductRepository extends Repository<Product, ProductId> {

  Optional<Product> findBySku(@NonNull SKU sku);

  List<Product> findByCategory(@NonNull Category category);

  boolean existsBySku(@NonNull SKU sku);
}
```

### In-Memory Implementation

```java
// portadapter/outgoing/product/InMemoryProductRepository.java
@Repository
public class InMemoryProductRepository implements ProductRepository {

  private final Map<ProductId, Product> products = new ConcurrentHashMap<>();

  @Override
  public void save(@NonNull final Product product) {
    products.put(product.id(), product);
  }

  @Override
  public Optional<Product> findById(@NonNull final ProductId id) {
    return Optional.ofNullable(products.get(id));
  }

  @Override
  public void deleteById(@NonNull final ProductId id) {
    products.remove(id);
  }

  @Override
  public List<Product> findAll() {
    return new ArrayList<>(products.values());
  }

  @Override
  public Optional<Product> findBySku(@NonNull final SKU sku) {
    return products.values().stream()
        .filter(p -> p.sku().equals(sku))
        .findFirst();
  }

  @Override
  public List<Product> findByCategory(@NonNull final Category category) {
    return products.values().stream()
        .filter(p -> p.category().equals(category))
        .toList();
  }

  @Override
  public boolean existsBySku(@NonNull final SKU sku) {
    return products.values().stream()
        .anyMatch(p -> p.sku().equals(sku));
  }
}
```

### Application Service Usage

```java
// application/ProductApplicationService.java
public class ProductApplicationService {

  private final ProductRepository productRepository;
  private final ProductFactory productFactory;
  private final DomainEventPublisher eventPublisher;

  public Product createProduct(...) {
    // Create aggregate
    final Product product = productFactory.createProduct(...);

    // Explicit save
    productRepository.save(product);

    // Publish events after successful save
    eventPublisher.publishAndClearEvents(product);

    return product;
  }

  public void changeProductPrice(ProductId productId, Price newPrice) {
    // Load aggregate
    final Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));

    // Execute domain logic
    product.changePrice(newPrice);

    // Explicit save
    productRepository.save(product);

    // Publish events
    eventPublisher.publishAndClearEvents(product);
  }
}
```

---

## Alternatives Considered

### Alternative 1: Collection-Oriented with JPA

```java
// Collection-oriented
public interface ProductRepository extends JpaRepository<Product, UUID> {
  // No save() method needed, JPA handles it
}

@Transactional
public void changePrice(ProductId id, Price newPrice) {
  Product product = productRepository.findById(id);
  product.changePrice(newPrice);
  // Automatic persistence via JPA dirty checking
}
```

**Rejected**:
- Requires JPA/Hibernate (framework coupling)
- Implicit persistence (less clear)
- Harder to control when saves happen
- Domain becomes aware of transactions
- Event publishing timing unclear

### Alternative 2: Dual Interface (add + save)

```java
public interface Repository<T, ID> {
  void add(T aggregate);     // For new aggregates
  void save(T aggregate);    // For updates
  void remove(T aggregate);
}
```

**Rejected**:
- More complex API
- Need to track "new vs existing"
- `save()` handles both create and update (simpler)

---

## References

### Domain-Driven Design Books

1. **Vaughn Vernon - Implementing Domain-Driven Design (2013)**, Chapter 12:
   > "There are two basic types of repositories: Collection-Oriented and Persistence-Oriented. The Collection-Oriented style better reflects the DDD ideal, but the Persistence-Oriented style is more explicit about transactions."

2. **Eric Evans - Domain-Driven Design (2003)**:
   > "Repositories should encapsulate all storage and retrieval logic."

### Related Patterns

- **Unit of Work**: Tracks changes (collection-oriented)
- **Data Mapper**: Maps domain to persistence (persistence-oriented)
- **Repository Pattern**: Abstraction over data access

### Related ADRs

- [ADR-008: Repository Interfaces in Domain Layer](adr-008-repository-interfaces-in-domain.md)
- [ADR-005: Domain Events Publishing Strategy](adr-005-domain-events-publishing.md)

---

## Validation

### Tests

```java
@Test
void shouldSaveProductExplicitly() {
  // Given
  Product product = productFactory.createProduct(...);

  // When
  productRepository.save(product);

  // Then
  Optional<Product> saved = productRepository.findById(product.id());
  assertThat(saved).isPresent();
}

@Test
void shouldRequireExplicitSaveForUpdates() {
  // Given
  Product product = productFactory.createProduct(...);
  productRepository.save(product);

  // When
  product.changePrice(Price.of(Money.euro(new BigDecimal("299.99"))));
  productRepository.save(product);  // Must explicitly save

  // Then
  Product loaded = productRepository.findById(product.id()).get();
  assertThat(loaded.price()).isEqualTo(Price.of(Money.euro(new BigDecimal("299.99"))));
}
```

---

## Review and Update

**Next Review**: January 24, 2026
**Review Frequency**: Annually or when persistence technology changes

**Update Criteria**:
- JPA adoption (might reconsider collection-oriented)
- Event sourcing adoption (would require changes)
- Team feedback on explicit saves

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
