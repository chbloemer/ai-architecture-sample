# ADR-011: Bounded Context Isolation via Package Structure

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

Strategic DDD emphasizes **Bounded Contexts** - explicit boundaries within which a domain model exists and terms have specific meaning.

Without clear boundaries:
- Terms have ambiguous meanings
- Models become entangled
- Changes ripple uncontrollably
- Team ownership is unclear

---

## Decision

**Each Bounded Context resides in its own package under `domain.model` with strict isolation rules:**

1. **No direct object references across contexts**
2. **Reference other contexts only by ID**
3. **Integration via Domain Events**
4. **Package-per-context structure**

### Package Structure

```
domain/model/
├── product/              ← Product Catalog Context
│   ├── Product           (aggregate)
│   ├── ProductId         (value object)
│   ├── SKU, Price, Money
│   ├── ProductRepository
│   ├── ProductFactory
│   └── ProductCreated, ProductPriceChanged  (events)
│
└── cart/                 ← Shopping Cart Context
    ├── ShoppingCart      (aggregate)
    ├── CartItem          (entity)
    ├── CartId            (value object)
    ├── ProductId         (references Product Context by ID)
    └── CartItemAddedToCart  (event)
```

### Context Map

```
┌─────────────────────┐         ┌──────────────────────┐
│  Shopping Cart      │ ────────>│  Product Catalog     │
│  Context            │  U/D     │  Context             │
└─────────────────────┘         └──────────────────────┘
       (uses ProductId reference only)

U/D = Upstream/Downstream (Customer-Supplier relationship)
```

---

## Rationale

### 1. **Clear Boundaries**

Each context has clear responsibility:

**Product Catalog Context**:
- Purpose: Manage product catalog with pricing and inventory
- Ubiquitous Language: Product, SKU, Price, Category, Stock
- Core Aggregate: Product

**Shopping Cart Context**:
- Purpose: Manage customer shopping carts
- Ubiquitous Language: Cart, CartItem, Customer, Quantity
- Core Aggregate: ShoppingCart

### 2. **Reference by ID Only**

```java
// cart/CartItem.java
public final class CartItem implements Entity<CartItem, CartItemId> {

  private final ProductId productId;  // ✅ ID reference only
  // NOT: private final Product product; ❌

  public Money totalPrice(final Price productPrice) {
    // Price passed as parameter from Product Context
    return productPrice.multiply(quantity.value());
  }
}
```

### 3. **Integration via Events**

```java
// Product Context raises event
@EventListener
public void on(ProductPriceChanged event) {
  // Cart Context can react
  // Invalidate cart totals for this product
  // Notify customers with product in cart
}
```

---

## Consequences

### Positive

✅ **Clear Ownership**: Each context has clear boundaries
✅ **Independent Evolution**: Contexts can evolve separately
✅ **Team Organization**: Teams can own specific contexts
✅ **Reduced Coupling**: No direct dependencies between contexts
✅ **Scalability**: Contexts can become microservices later

### Neutral

⚠️ **Coordination Needed**: Application services coordinate contexts
⚠️ **Eventual Consistency**: Events provide eventual consistency

### Negative

❌ **None identified**

---

## Implementation

### Current Bounded Contexts

**Product Catalog Context** (`product/`):
- 15 classes
- Aggregate: Product
- Repository: ProductRepository
- Events: ProductCreated, ProductPriceChanged

**Shopping Cart Context** (`cart/`):
- 11 classes
- Aggregate: ShoppingCart
- Entity: CartItem
- Events: CartItemAddedToCart, CartCheckedOut

### ArchUnit Enforcement

```groovy
def "Bounded Contexts dürfen nicht direkt aufeinander zugreifen"() {
  expect:
  noClasses()
    .that().resideInAPackage("..cart..")
    .should().dependOnClassesThat().resideInAPackage("..product..")
    .check(allClasses)
}

def "Contexts sollten nur über IDs referenzieren"() {
  // Enforced by aggregate reference rules
}
```

---

## References

- **Evans - DDD**: "Bounded Contexts are explicit boundaries"
- **Vernon - Implementing DDD**: "Context mapping shows relationships"

### Related ADRs

- [ADR-003: Aggregate Reference by Identity Only](adr-003-aggregate-reference-by-id.md)
- [ADR-005: Domain Events Publishing Strategy](adr-005-domain-events-publishing.md)

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
