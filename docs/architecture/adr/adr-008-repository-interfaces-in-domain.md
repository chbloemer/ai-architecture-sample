# ADR-008: Repository Interfaces in Domain Layer

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

In traditional layered architectures, repositories are often defined in the data access layer:

```
┌──────────────────────┐
│   Business Logic     │
│   (uses)             │
├──────────────────────┤      ↓ Dependency flows down
│   Data Access        │
│   Repository         │      Business logic depends on infrastructure
└──────────────────────┘
```

**Problems**:
- Business logic depends on infrastructure layer
- Cannot test business logic without infrastructure
- Infrastructure details leak into business logic
- Violates Dependency Inversion Principle

**Dependency Inversion Principle (SOLID)**:
> "High-level modules should not depend on low-level modules. Both should depend on abstractions."

---

## Decision

**Repository interfaces MUST be defined in the domain layer, implementations in secondary adapters.**

### Package Structure

```
domain/model/product/
├── Product.java                    # Aggregate
├── ProductRepository.java          # ← Interface in domain
└── ...

portadapter/outgoing/product/
└── InMemoryProductRepository.java  # ← Implementation in adapter
```

### Dependency Flow

```
┌─────────────────────────────────┐
│   Domain Layer                   │
│   Product (aggregate)            │
│   ProductRepository (interface)  │  ← Defines what it needs
└─────────────┬───────────────────┘
              │
              │ implements
              ▼
┌─────────────────────────────────┐
│   Secondary Adapter              │
│   InMemoryProductRepository      │  ← Provides what domain needs
└─────────────────────────────────┘
```

**Dependency points FROM infrastructure TO domain** (inverted!)

---

## Rationale

### 1. **Dependency Inversion Principle**

Domain defines the contract, infrastructure implements it:

```java
// Domain defines WHAT it needs
// domain/model/product/ProductRepository.java
public interface ProductRepository extends Repository<Product, ProductId> {

  Optional<Product> findBySku(@NonNull SKU sku);

  List<Product> findByCategory(@NonNull Category category);

  boolean existsBySku(@NonNull SKU sku);
}
```

```java
// Infrastructure provides HOW it works
// portadapter/outgoing/product/InMemoryProductRepository.java
@Repository
public class InMemoryProductRepository implements ProductRepository {
  // Implementation details
}
```

Domain doesn't care HOW repository works (in-memory, JPA, MongoDB).
Infrastructure adapts to domain's needs.

### 2. **Domain Owns Its Contracts**

Repository interface uses domain language:

```java
// ✅ Domain language in interface
public interface ProductRepository {
  Optional<Product> findBySku(SKU sku);          // Business concept: SKU
  List<Product> findByCategory(Category category); // Business concept: Category
  boolean existsBySku(SKU sku);                   // Business question
}

// ❌ Infrastructure language would be:
// List<ProductEntity> findBySkuColumn(String sku);  // Database thinking
```

Interface speaks domain language, not infrastructure language.

### 3. **Testability**

Can test domain with mock implementations:

```java
@Test
void shouldFindProductBySku() {
  // Given - mock repository (no real database needed)
  ProductRepository mockRepo = mock(ProductRepository.class);
  when(mockRepo.findBySku(sku)).thenReturn(Optional.of(product));

  // When
  ProductApplicationService service = new ProductApplicationService(mockRepo, ...);
  Product found = service.findProductBySku(sku);

  // Then
  assertThat(found).isEqualTo(product);
}
```

No database, no Spring context - just domain logic.

### 4. **Framework Independence**

Domain doesn't depend on persistence framework:

```java
// domain/model/product/ProductRepository.java
// NO JPA annotations
// NO Spring Data annotations
// Pure interface

public interface ProductRepository extends Repository<Product, ProductId> {
  Optional<Product> findBySku(@NonNull SKU sku);
}
```

Can swap from in-memory → JPA → MongoDB without changing domain.

### 5. **Hexagonal Architecture Alignment**

Repository interface is an **Output Port** (defined by hexagon):

```
┌──────────────────────────┐
│   Hexagon (Domain)        │
│   ProductRepository       │  ← Port (interface)
│   (interface)             │
└──────────┬────────────────┘
           │
           │ implements
           ▼
┌──────────────────────────┐
│   Secondary Adapter       │
│   InMemoryProductRepo     │  ← Adapter (implementation)
└──────────────────────────┘
```

Ports belong to hexagon, adapters belong to infrastructure.

---

## Consequences

### Positive

✅ **Dependency Inversion**: Infrastructure depends on domain
✅ **Domain Ownership**: Domain defines its own contracts
✅ **Framework Independence**: No infrastructure coupling
✅ **Testability**: Easy to mock repositories
✅ **Swappable Implementations**: Change persistence without changing domain
✅ **Ubiquitous Language**: Repository methods use domain terms
✅ **Clean Architecture**: Follows hexagonal/onion/clean architecture

### Neutral

⚠️ **More Packages**: Interface and implementation in different packages
⚠️ **Indirection**: Abstract interface between domain and persistence

### Negative

❌ **None identified** - This is a fundamental DDD/Clean Architecture principle

---

## Implementation

### Repository Interface (Domain Layer)

```java
// domain/model/ddd/Repository.java (Base interface)
public interface Repository<T extends AggregateRoot<T, ID>, ID extends Id> {

  void save(@NonNull T aggregate);

  Optional<T> findById(@NonNull ID id);

  void deleteById(@NonNull ID id);

  List<T> findAll();
}
```

```java
// domain/model/product/ProductRepository.java
public interface ProductRepository extends Repository<Product, ProductId> {

  Optional<Product> findBySku(@NonNull SKU sku);

  List<Product> findByCategory(@NonNull Category category);

  boolean existsBySku(@NonNull SKU sku);
}
```

### Repository Implementation (Secondary Adapter)

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

  // Depends on interface from domain layer
  private final ProductRepository productRepository;

  public Optional<Product> findProductBySku(SKU sku) {
    return productRepository.findBySku(sku);
  }

  public List<Product> findProductsByCategory(Category category) {
    return productRepository.findByCategory(category);
  }
}
```

### Spring Configuration

```java
// infrastructure/config/SpringConfiguration.java
@Configuration
public class SpringConfiguration {

  // Spring wires implementation to interface
  // Application service receives interface, not implementation

  @Bean
  public ProductApplicationService productApplicationService(
      ProductRepository productRepository,  // ← Interface
      ProductFactory productFactory,
      DomainEventPublisher eventPublisher) {
    return new ProductApplicationService(productRepository, productFactory, eventPublisher);
  }
}
```

### ArchUnit Enforcement

```groovy
// DddTacticalPatternsArchUnitTest.groovy

def "Repository Interfaces must reside in domain package"() {
  expect:
  classes()
    .that().haveSimpleNameEndingWith("Repository")
    .and().areInterfaces()
    .should().resideInAPackage("..domain.model..")
    .check(allClasses)
}

def "Repository Implementations must reside in portadapter.outgoing package"() {
  expect:
  classes()
    .that().implement(Repository.class)
    .and().areNotInterfaces()
    .should().resideInAPackage("..portadapter.outgoing..")
    .check(allClasses)
}

def "Repositories must only exist for Aggregate Roots"() {
  expect:
  classes()
    .that().haveSimpleNameEndingWith("Repository")
    .and().areInterfaces()
    .should().haveNameMatching(".*AggregateRootName.*Repository")
    .check(allClasses)
}
```

---

## Alternatives Considered

### Alternative 1: Repository Interface in Infrastructure

```java
// infrastructure/repository/ProductRepository.java
public interface ProductRepository extends JpaRepository<Product, UUID> {
  // Spring Data JPA interface
}

// Domain depends on infrastructure ❌
```

**Rejected**:
- Domain depends on infrastructure (wrong direction)
- Couples domain to JPA
- Cannot test domain without JPA
- Violates Dependency Inversion Principle

### Alternative 2: Repository Implementation in Domain

```java
// domain/model/product/InMemoryProductRepository.java
public class InMemoryProductRepository implements ProductRepository {
  // Implementation in domain
}
```

**Rejected**:
- Implementation details in domain
- Domain knows about storage mechanism
- Not swappable
- Violates Single Responsibility Principle

### Alternative 3: Separate Repository Package

```java
// repository/product/ProductRepository.java (separate package)
```

**Rejected**:
- Unclear ownership
- Not aligned with hexagonal architecture
- Ambiguous dependency direction

---

## References

### Domain-Driven Design Books

1. **Eric Evans - Domain-Driven Design (2003)**:
   > "Repositories belong to the domain layer. Infrastructure provides implementations."

2. **Vaughn Vernon - Implementing Domain-Driven Design (2013)**:
   > "Place Repository interfaces in the domain layer, implementations in infrastructure."

3. **Robert C. Martin - Clean Architecture (2017)**:
   > "The Dependency Inversion Principle: Depend on abstractions, not concretions."

### SOLID Principles

- **Dependency Inversion Principle**: High-level modules define interfaces, low-level modules implement them

### Related Patterns

- **Hexagonal Architecture**: Ports (interfaces) in hexagon, adapters outside
- **Repository Pattern**: Abstraction over data access
- **Adapter Pattern**: Adapt infrastructure to domain's needs

### Related ADRs

- [ADR-002: Framework-Independent Domain Layer](adr-002-framework-independent-domain.md)
- [ADR-004: Persistence-Oriented Repository Pattern](adr-004-persistence-oriented-repository.md)
- [ADR-007: Hexagonal Architecture](adr-007-hexagonal-architecture.md)

### External References

- [DDD Repository Pattern](https://martinfowler.com/eaaCatalog/repository.html)
- [Dependency Inversion Principle](https://en.wikipedia.org/wiki/Dependency_inversion_principle)

---

## Validation

### ArchUnit Tests

```bash
./gradlew test-architecture

# Expected:
# Repository Interfaces must reside in domain package PASSED ✅
# Repository Implementations must reside in portadapter.outgoing package PASSED ✅
```

### Dependency Check

```bash
# Check that domain doesn't depend on adapters
grep -r "import.*portadapter" domain/model/
# Should return nothing

# Check that implementations are in secondary adapters
find portadapter/outgoing -name "*Repository.java" -type f
# Should find implementations
```

### Test Example

```java
@Test
void canTestWithMockRepository() {
  // Given - no real repository needed
  ProductRepository mockRepo = mock(ProductRepository.class);
  when(mockRepo.findBySku(any())).thenReturn(Optional.of(testProduct));

  // When
  ProductApplicationService service = new ProductApplicationService(mockRepo, factory, publisher);

  // Then - domain logic tested without infrastructure
  assertThat(service.findProductBySku(sku)).isPresent();
}
```

---

## Review and Update

**Next Review**: January 24, 2026
**Review Frequency**: Annually

**Update Criteria**:
- New persistence technologies
- Repository pattern evolution
- Team feedback on structure

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
