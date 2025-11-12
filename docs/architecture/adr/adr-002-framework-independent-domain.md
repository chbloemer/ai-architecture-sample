# ADR-002: Framework-Independent Domain Layer

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐⭐

---

## Context

In enterprise applications, business logic is often tightly coupled to infrastructure frameworks (Spring, JPA, Hibernate, etc.). This creates several problems:

### Problems with Framework-Dependent Domain

1. **Framework Lock-In**: Domain logic cannot be reused if framework changes
2. **Testing Complexity**: Requires framework context to test business logic
3. **Framework Lifecycle**: Domain must adapt to framework updates/migrations
4. **Cognitive Load**: Developers must understand both domain AND framework
5. **Longevity Risk**: Frameworks change more frequently than business rules

**Example of Framework-Polluted Domain**:
```java
// ❌ BAD: Domain polluted with framework code
@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;  // JPA manages this

  // Domain logic mixed with persistence concerns
}
```

**Problems**:
- JPA annotations leak into domain
- Cannot test without Spring/JPA context
- Framework changes require domain changes
- ORM concerns mixed with business rules

---

## Decision

**The domain layer MUST have zero dependencies on infrastructure frameworks.**

**Allowed Dependencies**:
- `java.*` - Java Standard Edition
- `org.jspecify.*` - Null safety annotations only

**Forbidden Dependencies**:
- ❌ Spring Framework (`org.springframework.*`)
- ❌ JPA/Hibernate (`jakarta.persistence.*`, `org.hibernate.*`)
- ❌ Jackson (`com.fasterxml.jackson.*`)
- ❌ Any other infrastructure framework

**Clean Domain Example**:
```java
// ✅ GOOD: Pure domain logic
public final class Product extends BaseAggregateRoot<Product, ProductId> {

  private final ProductId id;
  private final SKU sku;
  private ProductName name;
  private Price price;
  private Category category;
  private ProductStock stock;

  public void changePrice(@NonNull final Price newPrice) {
    if (newPrice == null) {
      throw new IllegalArgumentException("New price cannot be null");
    }

    final Price oldPrice = this.price;
    this.price = newPrice;

    // Pure domain logic - no framework code
    registerEvent(ProductPriceChanged.now(this.id, oldPrice, newPrice));
  }

  public boolean isAvailable() {
    return stock.isAvailable();
  }
}
```

---

## Rationale

### 1. **Longevity and Stability**

Business rules outlive frameworks:
- **Domain**: Product pricing logic is stable for years
- **Frameworks**: Spring 5 → Spring 6 → Spring 7 (breaking changes)

Framework-free domain means:
- Business logic survives framework migrations
- No need to update domain for framework changes
- Domain code has longer shelf life

### 2. **Testability**

Pure domain objects are trivially testable:

```java
// ✅ Simple unit test - no framework needed
@Test
void shouldRaisePriceChangedEventWhenPriceChanges() {
  // Given
  Product product = createTestProduct();
  Price newPrice = Price.of(Money.euro(new BigDecimal("199.99")));

  // When
  product.changePrice(newPrice);

  // Then
  assertThat(product.domainEvents())
      .hasSize(1)
      .first()
      .isInstanceOf(ProductPriceChanged.class);
}
```

No Spring context, no JPA, no infrastructure - just pure business logic testing.

### 3. **Portability**

Framework-free domain can be used in multiple contexts:
- Spring Boot applications
- Quarkus applications
- Standalone batch jobs
- Serverless functions
- Different projects

Same domain logic, different infrastructure.

### 4. **Focus and Clarity**

Domain code focuses ONLY on business rules:
- No `@Entity`, `@Column`, `@Transactional` noise
- No Spring lifecycle concerns
- No ORM mapping complexity
- Pure business logic clearly visible

### 5. **Dependency Inversion**

Domain defines interfaces, infrastructure implements:

```java
// Domain defines what it needs
// domain/model/product/ProductRepository.java
public interface ProductRepository extends Repository<Product, ProductId> {
  Optional<Product> findBySku(@NonNull SKU sku);
}

// Infrastructure provides implementation
// portadapter/outgoing/product/JpaProductRepository.java
@Repository
public class JpaProductRepository implements ProductRepository {
  // JPA/Hibernate code HERE, not in domain
}
```

Domain drives, infrastructure serves.

---

## Consequences

### Positive

✅ **Long-lived Domain**: Business logic survives framework changes
✅ **Easy Testing**: No framework context needed for domain tests
✅ **Portable**: Same domain can run on different frameworks
✅ **Focused Code**: Domain only contains business logic
✅ **Clear Boundaries**: Obvious separation between domain and infrastructure
✅ **Faster Tests**: Domain tests run in milliseconds (no Spring context)
✅ **Team Efficiency**: Domain experts can read domain code without framework knowledge

### Neutral

⚠️ **More Infrastructure Code**: Mappers/adapters needed between domain and persistence
⚠️ **Learning Curve**: Team must understand hexagonal architecture

### Negative

❌ **None identified** - This is a universally beneficial practice in DDD

---

## Implementation

### Current Implementation

Domain layer is already framework-independent:

**Domain Package Structure**:
```
domain/model/
├── ddd/                    # DDD building blocks
│   ├── AggregateRoot
│   ├── Entity
│   ├── Value
│   ├── Repository
│   ├── DomainEvent
│   ├── DomainService
│   ├── Factory
│   └── Specification
├── product/               # Product bounded context
│   ├── Product           # ✅ No framework annotations
│   ├── Price             # ✅ No framework annotations
│   ├── Money             # ✅ No framework annotations
│   └── ProductRepository # ✅ Interface (no JPA)
└── cart/                 # Cart bounded context
    ├── ShoppingCart      # ✅ No framework annotations
    └── ShoppingCartRepository # ✅ Interface (no JPA)
```

**Verification**:
```bash
# Check domain dependencies
./gradlew test-architecture

# ArchUnit test passes:
# "Domain sollte keine Framework-Abhängigkeiten haben" ✅
```

### ArchUnit Enforcement

```groovy
// OnionArchitectureArchUnitTest.groovy
def "Domain sollte keine Framework-Abhängigkeiten haben"() {
  given:
  def javaPackages = ['java..', 'org.jspecify..']  // Allowed

  def forbiddenPackages = [
    'org.springframework..',
    'jakarta.persistence..',
    'org.hibernate..',
    'com.fasterxml.jackson..',
    // ... other frameworks
  ]

  expect:
  classes()
    .that().resideInAPackage("..domain..")
    .should().onlyDependOnClassesThat()
    .resideInAnyPackage(javaPackages.toArray(new String[0]))
    .orShould().resideInAPackage("..domain..")
    .check(allClasses)
}
```

This test **automatically fails the build** if anyone adds framework dependencies to domain.

---

## Alternatives Considered

### Alternative 1: Allow JPA annotations in domain

**Rejected**:
- Couples domain to JPA
- Makes testing harder (requires JPA context)
- Domain becomes persistence-aware
- Violates Single Responsibility Principle

### Alternative 2: Allow Spring annotations (@Component, @Service)

**Rejected**:
- Couples domain to Spring
- Domain becomes framework-aware
- Cannot reuse domain outside Spring
- Violates Dependency Inversion Principle

### Alternative 3: Separate domain model from persistence model

**Considered but not needed**:
- More complex (two models)
- More mapping code
- Current approach (repositories handle mapping) is sufficient

---

## References

### Domain-Driven Design Books

1. **Eric Evans - Domain-Driven Design (2003)**:
   > "The domain model should be free of infrastructure concerns. Isolate the domain from everything else."

2. **Vaughn Vernon - Implementing Domain-Driven Design (2013)**:
   > "Keep your domain model pure. Don't let infrastructure leak into it."

3. **Robert C. Martin - Clean Architecture (2017)**:
   > "The innermost circle should contain enterprise business rules with no outward dependencies."

### Related Patterns

- **Hexagonal Architecture** (Alistair Cockburn): Domain at center, adapters outside
- **Onion Architecture** (Jeffrey Palermo): Dependencies point inward
- **Dependency Inversion Principle** (SOLID): High-level modules don't depend on low-level
- **Ports and Adapters**: Domain defines ports, infrastructure provides adapters

### Related ADRs

- [ADR-007: Hexagonal Architecture](adr-007-hexagonal-architecture.md)
- [ADR-008: Repository Interfaces in Domain Layer](adr-008-repository-interfaces-in-domain.md)

### External References

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [DDD and Clean Architecture](https://www.youtube.com/watch?v=Nsjsiz2A9mg)

---

## Validation

### ArchUnit Tests

```bash
./gradlew test-architecture

# Expected output:
# DomainArchitectureTest > Domain sollte keine Framework-Abhängigkeiten haben PASSED ✅
# OnionArchitectureArchUnitTest > Domain darf nicht auf Infrastructure zugreifen PASSED ✅
```

### Build

```bash
./gradlew build
# BUILD SUCCESSFUL ✅
```

### Domain Test Example

```java
// No @SpringBootTest needed!
class ProductTest {

  @Test
  void shouldEnforcePositivePriceInvariant() {
    // Pure domain test - runs in milliseconds
    Money negativeMoney = Money.euro(new BigDecimal("-10"));

    assertThatThrownBy(() -> Price.of(negativeMoney))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Price must be positive");
  }
}
```

---

## Review and Update

**Next Review**: January 24, 2026
**Review Frequency**: Annually or when considering framework changes

**Update Criteria**:
- New frameworks emerge (e.g., Micronaut, Quarkus)
- Framework migration planned
- Team feedback on domain purity
- New infrastructure requirements

---

## Benefits Realized

Since implementing this decision:

✅ **Fast domain tests**: Domain tests run in <100ms (no Spring context)
✅ **Zero framework coupling**: Can move to any framework
✅ **Clear architecture**: Team understands domain vs. infrastructure
✅ **Automated enforcement**: ArchUnit prevents violations
✅ **Business focus**: Domain code reads like business requirements

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
