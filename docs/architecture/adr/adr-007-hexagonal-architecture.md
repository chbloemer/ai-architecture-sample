# ADR-007: Hexagonal Architecture with Explicit Port/Adapter Separation

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐⭐

---

## Context

Traditional layered architectures often create tight coupling between business logic and infrastructure:

```
┌──────────────────────┐
│   Presentation       │  ← Web framework
├──────────────────────┤
│   Business Logic     │  ← Domain + JPA/Spring
├──────────────────────┤
│   Data Access        │  ← Database specific
└──────────────────────┘
```

**Problems**:
- Business logic depends on frameworks (Spring, JPA)
- Hard to test (need full stack)
- Hard to change infrastructure (tightly coupled)
- Business logic scattered across layers

Alistair Cockburn's **Hexagonal Architecture** (Ports and Adapters) solves this:

```
         ┌──────────────────┐
    REST │   Primary        │ Web
  ───────►   Adapter        ├──────
         └────────┬─────────┘
                  │
         ┌────────▼─────────┐
         │    Application   │  ← Use Cases
         │    (Hexagon)     │
         │    + Domain      │  ← Business Logic
         └────────┬─────────┘
                  │
         ┌────────▼─────────┐
   DB    │   Secondary      │ Events
  ───────┤   Adapter        ◄──────
         └──────────────────┘
```

---

## Decision

**We will implement strict Hexagonal Architecture with explicit separation between:**

1. **Hexagon (Core)**: Domain + Application (business logic)
2. **Primary Adapters (Driving)**: Incoming requests (REST, Web)
3. **Secondary Adapters (Driven)**: Outgoing integrations (Database, Events)
4. **Ports**: Interfaces defining communication contracts

### Package Structure

```
src/main/java/de/sample/aiarchitecture/
├── sharedkernel/              # Shared Kernel (cross-context)
│   ├── domain/
│   │   ├── marker/            # DDD building blocks
│   │   └── common/            # Shared value objects (Money, ProductId, Price)
│   └── application/
│       └── marker/            # Use case patterns
│
├── product/                   # Product Bounded Context
│   ├── domain/                # Domain Model (Hexagon Core)
│   │   ├── model/             # Aggregates, entities, value objects
│   │   ├── service/           # Domain services
│   │   └── event/             # Domain events
│   ├── application/           # Application Services (Use Cases)
│   │   ├── ProductApplicationService
│   │   ├── port/
│   │   │   └── out/           # Output Ports
│   │   │       └── ProductRepository
│   │   └── usecase/           # Use cases (input ports)
│   └── adapter/
│       ├── incoming/          # Primary Adapters (Driving/Inbound)
│       │   ├── api/           # REST API (ProductResource)
│       │   ├── web/           # Web MVC (ProductPageController)
│       │   ├── mcp/           # MCP Server (ProductCatalogMcpTools)
│       │   └── event/         # Event Listeners
│       └── outgoing/          # Secondary Adapters (Driven/Outbound)
│           └── persistence/   # InMemoryProductRepository
│
├── cart/                      # Cart Bounded Context
│   ├── domain/                # Domain Model (Hexagon Core)
│   ├── application/           # Application Services
│   │   ├── ShoppingCartApplicationService
│   │   └── port/out/          # Output Ports
│   └── adapter/
│       ├── incoming/          # Primary Adapters
│       │   ├── api/           # REST API
│       │   └── event/         # Event Listeners
│       └── outgoing/          # Secondary Adapters
│           └── persistence/   # InMemoryShoppingCartRepository
│
├── portal/                    # Portal Bounded Context
│   └── adapter/
│       └── incoming/
│           └── web/           # HomePageController
│
└── infrastructure/            # Infrastructure (cross-cutting)
    ├── api/                   # Public SPI
    │   └── DomainEventPublisher
    └── config/                # Spring configuration
        └── SpringConfiguration
```

### Flow Example

```
HTTP Request
    │
    ▼
┌─────────────────────────────────┐
│ Primary Adapter                 │
│ ProductController (REST)        │  ← Driving Adapter
│ @RestController                 │
└─────────────┬───────────────────┘
              │
              ▼
┌─────────────────────────────────┐
│ Application Service (Input Port)│
│ ProductApplicationService       │  ← Use Case
│ (No framework annotations)      │
└─────────────┬───────────────────┘
              │
              ▼
┌─────────────────────────────────┐
│ Domain Model (Hexagon)          │
│ Product, Price, Money           │  ← Business Logic
│ (Pure Java, no framework)       │
└─────────────┬───────────────────┘
              │
              ▼
┌─────────────────────────────────┐
│ Output Port (Interface)         │
│ ProductRepository (interface)   │  ← Defined by domain
│ (in domain layer)               │
└─────────────┬───────────────────┘
              │
              ▼
┌─────────────────────────────────┐
│ Secondary Adapter               │
│ InMemoryProductRepository       │  ← Driven Adapter
│ @Repository                     │
└─────────────────────────────────┘
              │
              ▼
          Database
```

---

## Rationale

### 1. **Business Logic at the Center**

The hexagon contains only business logic:

```java
// product/domain/model/Product.java
// Pure business logic - no infrastructure
public final class Product extends BaseAggregateRoot<Product, ProductId> {

  public void changePrice(@NonNull final Price newPrice) {
    // Business rule: price must be positive
    if (newPrice.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }

    final Price oldPrice = this.price;
    this.price = newPrice;

    registerEvent(ProductPriceChanged.now(this.id, oldPrice, newPrice));
  }

  public boolean isAvailable() {
    return stock.isAvailable();
  }
}
```

No Spring, no JPA, no JSON - just business logic.

### 2. **Ports Define Contracts**

Domain defines what it needs (interfaces = ports):

```java
// product/application/port/out/ProductRepository.java (Output Port)
public interface ProductRepository extends Repository<Product, ProductId> {

  Optional<Product> findBySku(@NonNull SKU sku);

  List<Product> findByCategory(@NonNull Category category);

  boolean existsBySku(@NonNull SKU sku);
}
```

Infrastructure implements what domain needs.

### 3. **Adapters Are Swappable**

Can swap implementations without changing domain:

```java
// In-memory for testing
@Repository
public class InMemoryProductRepository implements ProductRepository { ... }

// JPA for production (future)
@Repository
public class JpaProductRepository implements ProductRepository { ... }

// MongoDB (future)
@Repository
public class MongoProductRepository implements ProductRepository { ... }
```

Domain code unchanged!

### 4. **Primary vs Secondary Adapters**

**Primary (Driving)**: External systems drive the application
```java
// REST API (primary adapter)
@RestController
public class ProductResource {
  private final ProductApplicationService productService;

  @PostMapping
  public ResponseEntity<ProductDto> createProduct(@RequestBody CreateProductRequest request) {
    Product product = productService.createProduct(...);  // Calls into hexagon
    return ResponseEntity.ok(converter.toDto(product));
  }
}
```

**Secondary (Driven)**: Application drives external systems
```java
// Repository (secondary adapter)
@Repository
public class InMemoryProductRepository implements ProductRepository {
  @Override
  public void save(Product product) {
    products.put(product.id(), product);  // Called BY application
  }
}
```

### 5. **Testability**

Test hexagon without adapters:

```java
@Test
void shouldChangePriceAndRaiseEvent() {
  // No HTTP, no database, no Spring - just business logic
  Product product = createTestProduct();

  product.changePrice(Price.of(Money.euro(new BigDecimal("199.99"))));

  assertThat(product.price()).isEqualTo(Price.of(Money.euro(new BigDecimal("199.99"))));
  assertThat(product.domainEvents()).hasSize(1);
}
```

---

## Consequences

### Positive

✅ **Framework Independence**: Business logic not coupled to frameworks
✅ **Testability**: Test business logic in isolation
✅ **Swappable Adapters**: Change infrastructure without changing domain
✅ **Clear Boundaries**: Obvious separation of concerns
✅ **Maintainability**: Business logic easy to find and change
✅ **Flexibility**: Can support multiple interfaces (REST + GraphQL + CLI)
✅ **Team Organization**: Clear ownership boundaries

### Neutral

⚠️ **More Packages**: More directory structure
⚠️ **Indirection**: Interfaces between layers
⚠️ **Learning Curve**: Team must understand hexagonal architecture

### Negative

❌ **None identified** - Benefits far outweigh any complexity

---

## Implementation

### Domain (Hexagon Core)

```java
// product/domain/model/Product.java
public final class Product extends BaseAggregateRoot<Product, ProductId> {
  // Pure business logic
}
```

### Application (Use Cases - Input Ports)

```java
// product/application/port/out/ProductRepository.java (Output Port)
public interface ProductRepository extends Repository<Product, ProductId> {
  Optional<Product> findBySku(@NonNull SKU sku);
}

// product/application/ProductApplicationService.java
public class ProductApplicationService {

  private final ProductRepository productRepository;
  private final ProductFactory productFactory;
  private final DomainEventPublisher eventPublisher;

  public Product createProduct(...) {
    Product product = productFactory.createProduct(...);
    productRepository.save(product);
    eventPublisher.publishAndClearEvents(product);
    return product;
  }
}
```

### Primary Adapters (Driving)

```java
// product/adapter/incoming/api/ProductResource.java
@RestController
@RequestMapping("/api/products")
public class ProductResource {

  private final ProductApplicationService productService;

  @PostMapping
  public ResponseEntity<ProductDto> createProduct(@RequestBody CreateProductRequest request) {
    Product product = productService.createProduct(...);
    return ResponseEntity.ok(converter.toDto(product));
  }
}
```

### Secondary Adapters (Driven)

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
}
```

### ArchUnit Enforcement

```groovy
// HexagonalArchitectureArchUnitTest.groovy

def "Classes from the domain should not access adapters"() {
  expect:
  noClasses()
    .that().resideInAPackage("..domain..")
    .should().dependOnClassesThat().resideInAPackage("..adapter..")
    .check(allClasses)
}

def "Application Services should not access adapters"() {
  expect:
  noClasses()
    .that().resideInAPackage("..application..")
    .should().dependOnClassesThat().resideInAPackage("..adapter..")
    .check(allClasses)
}

def "Adapters (incoming and outgoing) must not communicate directly with each other"() {
  expect:
  noClasses()
    .that().resideInAPackage("..adapter.incoming..")
    .should().dependOnClassesThat().resideInAPackage("..adapter.outgoing..")
  and:
  noClasses()
    .that().resideInAPackage("..adapter.outgoing..")
    .should().dependOnClassesThat().resideInAPackage("..adapter.incoming..")
    .check(allClasses)
}
```

---

## Alternatives Considered

### Alternative 1: Traditional Layered Architecture

```
Presentation → Business Logic → Data Access
```

**Rejected**:
- Business logic depends on frameworks
- Hard to test
- Tight coupling
- Infrastructure details leak into business logic

### Alternative 2: Clean Architecture (Uncle Bob)

**Considered**: Very similar to Hexagonal
- Different terminology (same concept)
- Hexagonal is more explicit about ports/adapters
- We chose Hexagonal for clarity

### Alternative 3: No Architecture (Framework-First)

**Rejected**:
- Business logic mixed with framework code
- Hard to maintain
- Hard to test
- Framework lock-in

---

## References

### Architecture Patterns

1. **Alistair Cockburn - Hexagonal Architecture (2005)**:
   > "Create your application to work without either a UI or a database so you can run automated regression-tests against the application, work when the database becomes unavailable, and link applications together without any user involvement."

2. **Robert C. Martin - Clean Architecture (2012)**:
   > "The overriding rule that makes this architecture work is The Dependency Rule: Source code dependencies can only point inwards."

3. **Vaughn Vernon - Implementing Domain-Driven Design (2013)**:
   > "Hexagonal Architecture isolates the domain model from the infrastructure and application concerns."

### Related Patterns

- **Ports and Adapters** (same as Hexagonal)
- **Onion Architecture** (Jeffrey Palermo)
- **Clean Architecture** (Robert C. Martin)
- **Dependency Inversion Principle** (SOLID)

### Related ADRs

- [ADR-002: Framework-Independent Domain Layer](adr-002-framework-independent-domain.md)
- [ADR-008: Repository Interfaces as Output Ports](adr-008-repository-interfaces-as-output-ports.md)

### External References

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

## Validation

### ArchUnit Tests

```bash
./gradlew test-architecture

# Expected:
# HexagonalArchitectureArchUnitTest > Classes from the domain should not access port adapters PASSED ✅
# HexagonalArchitectureArchUnitTest > Application Services should not access port adapters PASSED ✅
# HexagonalArchitectureArchUnitTest > Port adapters (incoming and outgoing) must not communicate directly with each other PASSED ✅
```

### Manual Verification

Check dependency flow:
```bash
# Domain should have no outward dependencies
grep -r "import.*adapter" product/domain/  # Should return nothing
grep -r "import.*adapter" cart/domain/  # Should return nothing

# Application should only use infrastructure.api (not adapter implementations)
grep -r "import.*adapter" product/application/  # Should return nothing
grep -r "import.*adapter" cart/application/  # Should return nothing
```

---

## Review and Update

**Next Review**: January 24, 2026
**Review Frequency**: Annually or when major architectural decisions needed

**Update Criteria**:
- New adapter types (GraphQL, gRPC)
- Microservices migration
- Event sourcing adoption
- Team feedback

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
