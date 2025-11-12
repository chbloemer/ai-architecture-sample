# ADR-008: Repository Interfaces as Output Ports in Application Layer

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

**Repository interfaces MUST be defined as output ports in the application layer (`application/port/out`), with implementations in secondary adapters (`adapter/outgoing`).**

**Key Principles**:

1. **Repositories are Output Ports** - Interfaces defined by the application layer describing what it needs from the outside world (persistence)
2. **Not Part of Domain Model** - Domain contains only business logic (aggregates, entities, value objects, domain services)
3. **Application Owns Ports** - The application layer defines both input ports (use cases) and output ports (repository interfaces)
4. **Infrastructure Implements Ports** - Adapters in the infrastructure layer implement what the application needs

**Layering**:
```
Domain Layer:        Pure business logic (Product, SKU, Price, etc.)
                              ↑ uses
Application Layer:   Use cases + Ports (ProductApplicationService, ProductRepository interface)
                              ↑ implements
Adapter Layer:       Implementations (InMemoryProductRepository)
```

### Package Structure

```
product/
├── domain/model/
│   ├── Product.java                # Aggregate
│   └── ...
├── application/port/out/
│   └── ProductRepository.java      # ← Interface (Output Port)
└── adapter/outgoing/persistence/
    └── InMemoryProductRepository.java  # ← Implementation
```

### Dependency Flow

```
┌─────────────────────────────────┐
│   Domain Layer                   │
│   Product (aggregate)            │  ← Pure business logic
│   No infrastructure dependencies │
└─────────────┬───────────────────┘
              │
              │ used by
              ▼
┌─────────────────────────────────┐
│   Application Layer              │
│   ProductApplicationService      │
│   ProductRepository (interface)  │  ← Output Port (defines what app needs)
└─────────────┬───────────────────┘
              │
              │ implements
              ▼
┌─────────────────────────────────┐
│   Secondary Adapter              │
│   InMemoryProductRepository      │  ← Provides what application needs
└─────────────────────────────────┘
```

**Dependency points FROM infrastructure TO application** (inverted!)

---

## Rationale

### 1. **Dependency Inversion Principle**

Application defines the contract (output port), infrastructure implements it:

```java
// Application defines WHAT it needs (output port)
// product/application/port/out/ProductRepository.java
public interface ProductRepository extends Repository<Product, ProductId> {

  Optional<Product> findBySku(@NonNull SKU sku);

  List<Product> findByCategory(@NonNull Category category);

  boolean existsBySku(@NonNull SKU sku);
}
```

```java
// Infrastructure provides HOW it works
// product/adapter/outgoing/persistence/InMemoryProductRepository.java
@Repository
public class InMemoryProductRepository implements ProductRepository {
  // Implementation details
}
```

Application doesn't care HOW repository works (in-memory, JPA, MongoDB).
Infrastructure adapts to application's needs.

### 2. **Output Ports Use Domain Language**

Repository interface (output port) uses domain language:

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

Can test application services with mock implementations:

```java
@Test
void shouldFindProductBySku() {
  // Given - mock repository (no real database needed)
  ProductRepository mockRepo = mock(ProductRepository.class);
  when(mockRepo.findBySku(sku)).thenReturn(Optional.of(product));

  // When
  ProductApplicationService service = new ProductApplicationService(mockRepo, factory, publisher);
  Product found = service.findProductBySku(sku);

  // Then
  assertThat(found).isEqualTo(product);
}
```

No database, no Spring context - just application and domain logic.

### 4. **Framework Independence**

Application layer doesn't depend on persistence framework:

```java
// product/application/port/out/ProductRepository.java
// NO JPA annotations
// NO Spring Data annotations
// Pure interface

public interface ProductRepository extends Repository<Product, ProductId> {
  Optional<Product> findBySku(@NonNull SKU sku);
}
```

Can swap from in-memory → JPA → MongoDB without changing application or domain.

### 5. **Hexagonal Architecture Alignment**

Repository interface is an **Output Port** (secondary port, defined by application layer):

```
┌──────────────────────────┐
│   Hexagon Core            │
│   Domain + Application    │
│   ┌────────────────────┐ │
│   │ Application Layer  │ │
│   │ ProductRepository  │ │  ← Output Port (interface)
│   └────────────────────┘ │
└──────────┬────────────────┘
           │
           │ implements
           ▼
┌──────────────────────────┐
│   Secondary Adapter       │
│   InMemoryProductRepo     │  ← Adapter (implementation)
└──────────────────────────┘
```

**Key:**
- **Output Ports** (repository interfaces) are in **application layer** (`application/port/out`)
- **Domain layer** contains only pure business logic (aggregates, entities, value objects)
- **Adapters** (implementations) are outside the hexagon in infrastructure

---

## Consequences

### Positive

✅ **Dependency Inversion**: Infrastructure depends on application (not the reverse)
✅ **Clear Output Ports**: Application explicitly defines what it needs from infrastructure
✅ **Framework Independence**: No infrastructure coupling in application or domain
✅ **Testability**: Easy to mock repositories in application service tests
✅ **Swappable Implementations**: Change persistence without changing application or domain
✅ **Ubiquitous Language**: Repository methods (output ports) use domain terms
✅ **Clean Architecture**: Follows hexagonal/onion/clean architecture
✅ **Separation of Concerns**: Domain is pure business logic; ports are in application

### Neutral

⚠️ **More Packages**: Interface and implementation in different packages
⚠️ **Indirection**: Abstract interface between domain and persistence

### Negative

❌ **None identified** - This is a fundamental DDD/Clean Architecture principle

---

## Implementation

### Repository Interface (Application Layer - Output Port)

```java
// sharedkernel/domain/marker/Repository.java (Base interface)
public interface Repository<T extends AggregateRoot<T, ID>, ID extends Id> {

  void save(@NonNull T aggregate);

  Optional<T> findById(@NonNull ID id);

  void deleteById(@NonNull ID id);

  List<T> findAll();
}
```

```java
// product/application/port/out/ProductRepository.java
public interface ProductRepository extends Repository<Product, ProductId> {

  Optional<Product> findBySku(@NonNull SKU sku);

  List<Product> findByCategory(@NonNull Category category);

  boolean existsBySku(@NonNull SKU sku);
}
```

### Repository Implementation (Secondary Adapter)

```java
// product/adapter/outgoing/persistence/InMemoryProductRepository.java
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
// product/application/ProductApplicationService.java
public class ProductApplicationService {

  // Depends on output port (repository interface) from same layer
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

def "Repository Interfaces must reside in application port.out package"() {
  expect:
  classes()
    .that().haveSimpleNameEndingWith("Repository")
    .and().areInterfaces()
    .should().resideInAnyPackage(
      "..product.application.port.out..",
      "..cart.application.port.out.."
    )
    .check(allClasses)
}

def "Repository Implementations must reside in adapter.outgoing package"() {
  expect:
  classes()
    .that().implement(Repository.class)
    .and().areNotInterfaces()
    .should().resideInAnyPackage(
      "..product.adapter.outgoing..",
      "..cart.adapter.outgoing.."
    )
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

// Application depends on infrastructure ❌
```

**Rejected**:
- Application depends on infrastructure (wrong direction)
- Couples application to JPA
- Cannot test application without JPA
- Violates Dependency Inversion Principle

### Alternative 2: Repository Interface in Domain Layer

```java
// product/domain/model/ProductRepository.java
public interface ProductRepository extends Repository<Product, ProductId> {
  // Repository interface in domain
}
```

**Rejected**:
- Repositories are not part of the domain model (they're infrastructure concerns)
- Domain should only contain business logic (aggregates, entities, value objects, domain services)
- Repository is an output port - belongs in application layer
- Mixing persistence concerns with business logic

### Alternative 3: Repository Implementation in Domain

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

### Alternative 4: Separate Repository Package

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

1. **Alistair Cockburn - Hexagonal Architecture (2005)**:
   > "The application defines ports - interfaces for what it needs. Infrastructure implements these ports as adapters."

2. **Robert C. Martin - Clean Architecture (2017)**:
   > "Use case interactors depend on interfaces (ports) that are implemented by frameworks and drivers in the outer layer."

**Note on DDD Literature**: Traditional DDD books (Evans, Vernon) place repository interfaces in the domain layer. However, in Hexagonal/Clean Architecture, we recognize that:
- **Domain** = Pure business logic (aggregates, entities, value objects, domain services)
- **Application** = Use cases and ports (including repository interfaces as output ports)
- **Repositories are infrastructure concerns** - they deal with persistence, not business rules
- This separation is clearer and better aligns with dependency inversion and Hexagonal Architecture principles

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
grep -r "import.*adapter" product/domain/
grep -r "import.*adapter" cart/domain/
# Should return nothing

# Check that implementations are in secondary adapters
find product/adapter/outgoing -name "*Repository.java" -type f
find cart/adapter/outgoing -name "*Repository.java" -type f
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
