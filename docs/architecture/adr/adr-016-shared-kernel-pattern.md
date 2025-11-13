# ADR-016: Shared Kernel Pattern for Cross-Context Value Objects

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

In our e-commerce application, we have two bounded contexts (Product Catalog and Shopping Cart) that need to share certain universal domain concepts. The question arises: **How should we manage value objects that are used across multiple bounded contexts?**

### The Problem

Without a clear strategy for sharing domain concepts, we face several issues:

1. **Duplication**: Reimplementing `Money` in each context risks inconsistency
2. **False Ownership**: `Money` was in `product` package, but Cart also uses it
3. **Coupling**: Cart imported from Product package, violating bounded context isolation
4. **Ambiguity**: Unclear which concepts are shared vs. context-specific

**Example of the Problem**:
```java
// ❌ BEFORE: Money in product package
package de.sample.aiarchitecture.product.domain.model;
public record Money(...) { }

// Cart had to import from product package (wrong!)
package de.sample.aiarchitecture.cart.domain.model;
import de.sample.aiarchitecture.product.domain.model.Money;  // ❌ Cross-context coupling!

public class ShoppingCart {
    public Money calculateTotal() { ... }
}
```

**Alternatives Considered**:

1. **Separate Ways** - Duplicate `Money` in each context
   - Risk: Currency handling bugs, inconsistency between contexts
   - Rejected: Too high risk for financial calculations

2. **Published Language** - Share via events/DTOs
   - Risk: Too complex for simple value objects
   - Rejected: Overkill for universal primitives

3. **Customer/Supplier** - One context owns, other imports
   - Risk: Creates false ownership (why does Product "own" Money?)
   - Rejected: Violates bounded context independence

---

## Decision

**Implement the Shared Kernel pattern with a dedicated `sharedkernel` package for universal value objects and DDD marker interfaces used across multiple bounded contexts.**

### Package Structure

```
de.sample.aiarchitecture/
├── sharedkernel/                    ← SHARED KERNEL
│   ├── domain/
│   │   ├── marker/                  (DDD marker interfaces)
│   │   │   ├── AggregateRoot.java
│   │   │   ├── Entity.java
│   │   │   ├── Value.java
│   │   │   ├── Repository.java
│   │   │   └── DomainEvent.java
│   │   ├── common/                  (Cross-context value objects)
│   │   │   ├── Money.java          (universal monetary concept)
│   │   │   ├── ProductId.java      (cross-context identifier)
│   │   │   └── Price.java          (wraps Money with validation)
│   │   └── spec/
│   │       └── Specification.java
│   └── application/
│       └── marker/                  (Use case patterns)
│           ├── InputPort.java
│           └── OutputPort.java
│
├── product/                         ← Product Context (isolated)
│   ├── domain/model/
│   │   ├── Product
│   │   ├── SKU
│   │   └── ProductName
│   ├── application/
│   └── adapter/
│
└── cart/                            ← Cart Context (isolated)
    ├── domain/model/
    │   ├── ShoppingCart
    │   ├── CartItem
    │   └── Quantity
    ├── application/
    └── adapter/
```

### Integration Rules

```java
// ✅ ALLOWED: Both contexts access Shared Kernel
package de.sample.aiarchitecture.cart.domain.model;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;     // ✅
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId; // ✅
import de.sample.aiarchitecture.sharedkernel.domain.marker.Entity;    // ✅

// ❌ FORBIDDEN: Direct access between contexts
import de.sample.aiarchitecture.product.domain.model.Product;  // ❌
```

### ArchUnit Enforcement

```groovy
// Architecture tests enforce isolation:

def "Shared Kernel must not depend on bounded contexts"() {
  expect:
  noClasses()
    .that().resideInAPackage("..sharedkernel..")
    .should().dependOnClassesThat().resideInAPackage("..product..")
    .orShould().dependOnClassesThat().resideInAPackage("..cart..")
    .check(allClasses)
}

def "Product Context must not access Cart Context directly"() {
  expect:
  noClasses()
    .that().resideInAPackage("..product..")
    .should().dependOnClassesThat().resideInAPackage("..cart..")
    .check(allClasses)
}

def "Cart Context must not access Product Context directly"() {
  expect:
  noClasses()
    .that().resideInAPackage("..cart..")
    .should().dependOnClassesThat().resideInAPackage("..product..")
    .check(allClasses)
}
```

---

## Rationale

### 1. **Shared Kernel is an Established DDD Pattern**

**Eric Evans (DDD Chapter 14: Maintaining Model Integrity)**:
> "Designate some subset of the domain model that the two teams agree to share. This explicitly shared stuff has special status, and shouldn't be changed without consultation with the other team."

**Vaughn Vernon (Implementing DDD Chapter 2)**:
> "Two or more teams share a subset of the domain model, including code and perhaps the database. This is the SHARED KERNEL pattern."

### 2. **Clear Criteria for Shared Kernel Membership**

**What Belongs:**
- ✅ **Universal value objects**: `Money`, `Currency` (same meaning everywhere)
- ✅ **Cross-context identifiers**: `ProductId` (used by both Product and Cart)
- ✅ **Common primitives**: Concepts with consistent meaning across all contexts

**What Does NOT Belong:**
- ❌ **Aggregates**: Each aggregate belongs to exactly one context
- ❌ **Context-specific logic**: Business rules tied to one context
- ❌ **Infrastructure**: Technical concerns don't belong in domain

### 3. **Benefits Outweigh Trade-offs**

**Money Example**:
```java
// ✅ Single definition ensures consistency
public record Money(@NonNull BigDecimal amount, @NonNull Currency currency) implements Value {
    public Money {
        // Normalize scale to 2 decimal places - CONSISTENT everywhere
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money add(final Money other) {
        // Currency validation - CONSISTENT everywhere
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(amount.add(other.amount), currency);
    }
}
```

**Without Shared Kernel** (if we duplicated):
- Risk: Product uses 2 decimals, Cart uses 4 → rounding errors
- Risk: Different currency validation logic → runtime errors
- Risk: Inconsistent Money arithmetic → financial bugs

### 4. **Explicit Boundaries Reduce Ambiguity**

```
BEFORE (ambiguous):
- Money in product → "Why does Product own Money?"
- Cart imports from product → "Are these contexts coupled?"

AFTER (explicit):
- Money in shared → "Universal concept"
- Both contexts import shared → "Clear shared kernel"
- Contexts cannot import each other → "Enforced isolation"
```

### 5. **Enables Future Distribution**

If we later split into microservices:
```
Product Service         Cart Service
     ↓                      ↓
     ↓                      ↓
  ┌──────────────────────────┐
  │   Shared Kernel Library   │
  │   (Money, ProductId)      │
  └──────────────────────────┘
```

Shared Kernel becomes a versioned library.

---

## Consequences

### Positive

✅ **Consistency**: Single definition of `Money` prevents currency handling bugs
✅ **Reduced Duplication**: No need to reimplement universal concepts
✅ **Clear Boundaries**: Explicit separation of shared vs. context-specific
✅ **Enforced Isolation**: ArchUnit ensures contexts don't cross boundaries
✅ **Better Communication**: Team knows what's shared vs. context-owned
✅ **Scalability**: Shared Kernel can become shared library in microservices

### Neutral

⚠️ **Coordination Required**: Changes to Shared Kernel affect all contexts
⚠️ **Small Surface Area**: Must keep Shared Kernel minimal (only 3 classes currently)
⚠️ **Governance Needed**: Team must agree on what belongs in Shared Kernel

### Negative

❌ **Coupling**: Shared Kernel creates coupling between contexts
- **Mitigation**: Keep kernel small (3 classes), stable, and change rarely
- **Trade-off Accepted**: Coupling is explicit and controlled

❌ **Breaking Changes**: Changing Money affects both contexts
- **Mitigation**: Shared Kernel should be stable primitives that rarely change
- **Current Reality**: Money hasn't changed since initial implementation

---

## Implementation

### Shared Kernel Contents

**Domain Marker Interfaces** (`sharedkernel.domain.marker`):
- `AggregateRoot`, `Entity`, `Value`, `Repository`, `DomainEvent`, `DomainService`, `Factory`, `Specification`

**Cross-Context Value Objects** (`sharedkernel.domain.common`):

**1. Money** - Universal monetary value
```java
package de.sample.aiarchitecture.sharedkernel.domain.common;

public record Money(@NonNull BigDecimal amount, @NonNull Currency currency) implements Value {
    // Universal monetary concept
    // Used by: Product (pricing), Cart (totals)
}
```

**2. ProductId** - Cross-context identifier
```java
package de.sample.aiarchitecture.sharedkernel.domain.common;

public record ProductId(@NonNull String value) implements Id, Value {
    // Product identifier
    // Used by: Product (identity), Cart (references products by ID)
}
```

**3. Price** - Domain-specific monetary wrapper
```java
package de.sample.aiarchitecture.sharedkernel.domain.common;

public record Price(@NonNull Money value) implements Value {
    // Wraps Money with "price must be > 0" validation
    // Used by: Product (product price), Cart (price snapshot)
}
```

**Application Marker Interfaces** (`sharedkernel.application.port`):
- `InputPort`, `OutputPort`

### Migration Performed

**Changes Made**:
1. Created `sharedkernel` package with `domain.marker`, `domain.common`, `application.marker` sub-packages
2. Moved DDD marker interfaces to `sharedkernel.domain.marker`
3. Moved `Money`, `ProductId`, `Price` to `sharedkernel.domain.common`
4. Moved use case patterns to `sharedkernel.application.port`
5. Updated all files with correct imports
6. Added ArchUnit tests for isolation
7. Updated architecture documentation

**Before/After Comparison**:

```diff
// BEFORE: Cart imported from Product (coupling!)
- import de.sample.aiarchitecture.product.domain.model.Money;
- import de.sample.aiarchitecture.product.domain.model.ProductId;

// AFTER: Cart imports from Shared Kernel (explicit)
+ import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
+ import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
```

### Verification

```bash
$ ./gradlew test-architecture

✅ Shared Kernel must not depend on bounded contexts: PASSED
✅ Product Context must not access Cart Context directly: PASSED
✅ Cart Context must not access Product Context directly: PASSED

BUILD SUCCESSFUL
```

---

## Alternatives Considered

### Alternative 1: Separate Ways (Duplicate Money)

**Description**: Each context implements its own `Money` class

**Rejected Because**:
- **High Risk**: Currency handling bugs, rounding inconsistencies
- **Maintenance**: Must keep two implementations in sync
- **No Benefit**: Money is truly universal, no context-specific behavior
- **Real-World Risk**: Financial calculations must be consistent

**When It WOULD Make Sense**:
- If Product and Cart had different monetary requirements
- If Money was context-specific (it's not)

### Alternative 2: Published Language (Events/DTOs)

**Description**: Share Money as a DTO/event payload

**Rejected Because**:
- **Too Complex**: Overkill for simple value objects
- **Not Domain**: DTOs are infrastructure, not domain
- **Duplication**: Still need Money in domain layer
- **Indirection**: Unnecessary translation layers

**When It WOULD Make Sense**:
- For complex aggregates (not value objects)
- For external system integration
- For versioned APIs

### Alternative 3: Customer/Supplier (Product owns Money)

**Description**: Keep Money in Product, Cart imports from Product

**Rejected Because**:
- **False Ownership**: Product doesn't "own" Money
- **Coupling**: Cart becomes downstream of Product
- **Violation**: Breaks bounded context isolation
- **Ambiguity**: Unclear why Product owns universal concept

**When It WOULD Make Sense**:
- If Money was product-specific (it's not)
- If one context was clearly upstream (neither is)

---

## Governance

### What Can Be Added to Shared Kernel?

**Criteria** (all must be true):
1. ✅ Used by 2+ bounded contexts
2. ✅ Has universal meaning (same concept everywhere)
3. ✅ Stable (unlikely to change frequently)
4. ✅ Value object (not aggregate, not entity)
5. ✅ No context-specific behavior

**Examples**:

| Concept | Belongs in Shared Kernel? | Reason |
|---------|--------------------------|---------|
| `Money` | ✅ YES | Universal, used by all contexts, stable |
| `ProductId` | ✅ YES | Cross-context identifier, stable |
| `Address` | ✅ MAYBE | If used by multiple contexts AND has universal meaning |
| `Product` | ❌ NO | Aggregate, belongs to Product context only |
| `CartStatus` | ❌ NO | Cart-specific, not used elsewhere |
| `SKU` | ❌ NO | Product-specific identifier |

### Change Process

**To modify Shared Kernel**:
1. Discuss with all affected teams
2. Ensure backward compatibility (or coordinate migration)
3. Update all contexts simultaneously
4. Run full test suite
5. Document in ADR

**Version Management** (if we become microservices):
- Semantic versioning for Shared Kernel library
- Major version for breaking changes
- Coordinated deployment across services

---

## References

### Domain-Driven Design Literature

**1. Eric Evans - Domain-Driven Design (2003)**:
- **Chapter 14: Maintaining Model Integrity**
- **Section: Shared Kernel**
- Quote: "Designate some subset of the domain model that the two teams agree to share."

**2. Vaughn Vernon - Implementing Domain-Driven Design (2013)**:
- **Chapter 2: Domains, Subdomains, and Bounded Contexts**
- **Section: Context Mapping - Shared Kernel**
- Quote: "Two or more teams share a subset of the domain model, including code and perhaps the database."

**3. Martin Fowler - Patterns of Enterprise Application Architecture**:
- **Pattern: Money**
- Discusses Money as a universal pattern across domains

### Related ADRs

- [ADR-003: Aggregate Reference by Identity Only](adr-003-aggregate-reference-by-id.md) - ProductId enables ID references
- [ADR-009: Value Objects as Java Records](adr-009-value-objects-as-records.md) - Money, Price, ProductId are records
- [ADR-011: Bounded Context Isolation](adr-011-bounded-context-isolation.md) - Superseded by this ADR for shared concepts

### External Resources

- [Context Mapping Patterns](https://www.domainlanguage.com/ddd/patterns/)
- [Shared Kernel Pattern](https://martinfowler.com/bliki/BoundedContext.html)
- [DDD Reference - Eric Evans](http://domainlanguage.com/wp-content/uploads/2016/05/DDD_Reference_2015-03.pdf)

---

## Metrics and Success Criteria

### Success Metrics

**Achieved**:
- ✅ 3 classes in Shared Kernel (minimal surface area)
- ✅ 100% ArchUnit test coverage (3 new tests)
- ✅ Zero violations in bounded context isolation
- ✅ All builds passing
- ✅ Documentation updated

**Ongoing**:
- Monitor Shared Kernel size (should stay small, ~3-5 classes)
- Track change frequency (should be low, stable)
- Measure coupling (contexts should only couple to shared, not each other)

### Review Criteria

**Quarterly Review Questions**:
1. Has Shared Kernel grown? If yes, why?
2. Are classes in Shared Kernel truly universal?
3. Have we had issues with coupling?
4. Should any classes be moved out of Shared Kernel?
5. Should any classes be moved into Shared Kernel?

---

## Decision Review

**Next Review**: January 24, 2026
**Review Frequency**: Quarterly

**Review Triggers**:
- Shared Kernel grows beyond 5 classes
- Frequent breaking changes to Shared Kernel
- Team reports coordination difficulties
- New bounded context added
- Migration to microservices planned

**Update Criteria**:
- New universal concepts discovered
- Context relationships change
- Team structure changes
- Architectural patterns evolve

---

## Validation Checklist

For code reviews and ongoing validation:

- [ ] Shared Kernel contains only universal value objects
- [ ] Shared Kernel has no dependencies on bounded contexts
- [ ] Product and Cart contexts don't import from each other
- [ ] All ArchUnit tests pass
- [ ] New shared concepts meet all governance criteria
- [ ] Documentation updated for any changes

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
**Relates to**: ADR-011 (Bounded Context Isolation)
**Supersedes**: Implicit sharing pattern (Money in product package)
