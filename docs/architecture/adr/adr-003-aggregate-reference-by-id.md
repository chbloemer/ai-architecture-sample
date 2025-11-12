# ADR-003: Aggregate Reference by Identity Only

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐⭐

---

## Context

In Domain-Driven Design, Aggregates are consistency boundaries that encapsulate related entities and value objects. A critical question arises: **How should one Aggregate reference another Aggregate?**

### Problems with Direct Object References

When Aggregates hold direct references to other Aggregates, several problems emerge:

1. **Transaction Boundary Violations**: Loading one aggregate loads entire object graph
2. **Memory Issues**: Cascading loads can bring entire database into memory
3. **Unclear Ownership**: Who is responsible for lifecycle of referenced aggregate?
4. **Consistency Confusion**: Which aggregate's invariants are we protecting?
5. **Lazy Loading Hell**: JPA lazy loading exceptions and proxy objects
6. **Tight Coupling**: Aggregates become interdependent

**Example of Problem**:
```java
// ❌ BAD: Direct aggregate reference
public class ShoppingCart {
  private final CartId id;
  private final List<CartItem> items;

  public void addItem(Product product, int quantity) {  // Direct reference!
    items.add(new CartItem(product, quantity));  // Holds entire Product aggregate
  }

  public Money calculateTotal() {
    return items.stream()
        .map(item -> item.getProduct().getPrice().multiply(item.getQuantity()))  // Navigating object graph
        .reduce(Money.zero(), Money::add);
  }
}

public class CartItem {
  private Product product;  // ❌ Direct reference to another aggregate
  private int quantity;
}
```

**Problems**:
- Loading ShoppingCart loads all Products
- Product lifecycle managed by both ProductCatalog AND ShoppingCart
- Consistency boundary is unclear (Cart? Product? Both?)
- Cannot change Product without affecting ShoppingCart
- JPA lazy loading issues

---

## Decision

**Aggregate Roots MUST reference other Aggregate Roots by ID only, NEVER by direct object reference.**

**Rule**:
```java
// ✅ ALLOWED: Reference by ID
private final ProductId productId;

// ❌ FORBIDDEN: Direct object reference
private final Product product;
```

This implements **Vaughn Vernon's Rule #3** from "Implementing Domain-Driven Design":
> "Reference other Aggregates by Identity only"

### Correct Implementation

```java
// ✅ GOOD: Reference by ID
public final class CartItem implements Entity<CartItem, CartItemId> {

  private final CartItemId id;
  private final ProductId productId;  // ✅ Reference by ID only
  private Quantity quantity;

  public Money totalPrice(final Price productPrice) {
    // Price passed as parameter, not navigated via object graph
    return productPrice.multiply(quantity.value());
  }
}

public class ShoppingCart extends BaseAggregateRoot<ShoppingCart, CartId> {

  private final CartId id;
  private final List<CartItem> items;

  public void addItem(@NonNull final ProductId productId, @NonNull final Quantity quantity) {
    // ✅ Only stores ProductId, not entire Product
    items.add(new CartItem(CartItemId.generate(), productId, quantity));
    registerEvent(CartItemAddedToCart.now(this.id, productId, quantity));
  }
}
```

---

## Rationale

### 1. **Clear Transaction Boundaries**

Each aggregate is a separate transaction boundary:

```java
// Product transaction
productRepository.save(product);  // Transaction 1

// Cart transaction (independent)
shoppingCart.addItem(product.id(), quantity);  // Only needs ID
cartRepository.save(shoppingCart);  // Transaction 2
```

No cascading saves, no distributed transactions, clean boundaries.

### 2. **Independent Lifecycle Management**

```java
// Product can be deleted without affecting Cart
productRepository.delete(productId);

// Cart still works (has ProductId)
// Application layer handles missing products:
Optional<Product> product = productRepository.findById(cartItem.productId());
if (product.isEmpty()) {
  // Handle: product no longer available
}
```

Aggregates have independent lifecycles.

### 3. **Eventual Consistency via Domain Events**

Aggregates coordinate through events, not direct references:

```java
// Product aggregate raises event
public void changePrice(Price newPrice) {
  this.price = newPrice;
  registerEvent(ProductPriceChanged.now(this.id, oldPrice, newPrice));
}

// Event listener updates dependent data (if needed)
@EventListener
public void on(ProductPriceChanged event) {
  // Could invalidate shopping cart cache
  // Or notify users with product in cart
  // Eventual consistency, not immediate
}
```

### 4. **Scalability and Performance**

```java
// Only loads what's needed
ShoppingCart cart = cartRepository.findById(cartId);  // Light weight
// cart.items contains ProductIds only

// Fetch products separately if needed
List<ProductId> productIds = cart.items().stream()
    .map(CartItem::productId)
    .toList();

List<Product> products = productRepository.findAllById(productIds);  // Batch query
```

Explicit loading, no N+1 queries, no cascading fetches.

### 5. **Testability**

```java
// Easy to test with just IDs
@Test
void shouldAddItemToCart() {
  // Given
  ShoppingCart cart = new ShoppingCart(CartId.generate(), CustomerId.generate());
  ProductId productId = ProductId.generate();  // Just an ID, no Product needed!

  // When
  cart.addItem(productId, Quantity.of(2));

  // Then
  assertThat(cart.items()).hasSize(1);
  assertThat(cart.items().get(0).productId()).isEqualTo(productId);
}
```

No need to create entire Product aggregate for testing CartItem.

---

## Consequences

### Positive

✅ **Clear Boundaries**: Each aggregate is independent
✅ **Performance**: No cascading loads or N+1 queries
✅ **Scalability**: Aggregates can be distributed across services
✅ **Flexibility**: Aggregates evolve independently
✅ **Simple Transactions**: One aggregate per transaction
✅ **Easy Testing**: Test aggregates in isolation
✅ **Eventual Consistency**: Natural fit for event-driven architecture

### Neutral

⚠️ **Coordination Required**: Application services coordinate aggregates
⚠️ **Explicit Loading**: Must explicitly load referenced aggregates when needed

### Negative

❌ **None identified** - This is a fundamental DDD best practice

---

## Implementation

### Current Implementation

All aggregates reference each other by ID only:

**CartItem references Product by ID**:
```java
// domain/model/cart/CartItem.java
public final class CartItem implements Entity<CartItem, CartItemId> {

  private final CartItemId id;
  private final ProductId productId;  // ✅ ID reference only
  private Quantity quantity;

  // No direct Product reference
  // No navigation to Product aggregate
}
```

**ShoppingCart manages items**:
```java
// domain/model/cart/ShoppingCart.java
public final class ShoppingCart extends BaseAggregateRoot<ShoppingCart, CartId> {

  private final CartId id;
  private final CustomerId customerId;  // ✅ ID reference only
  private final List<CartItem> items;  // CartItems contain ProductIds
  private CartStatus status;

  public void addItem(@NonNull final ProductId productId, @NonNull final Quantity quantity) {
    // Only stores ProductId, not Product
    items.add(new CartItem(CartItemId.generate(), productId, quantity));
    registerEvent(CartItemAddedToCart.now(this.id, productId, quantity));
  }
}
```

**Use Case Coordinates**:
```java
// application/usecase/additemtocart/AddItemToCartUseCase.java
@Service
public class AddItemToCartUseCase implements AddItemToCartInputPort {

  public AddItemToCartResponse execute(AddItemToCartCommand command) {
    CartId cartId = CartId.of(command.cartId());
    ProductId productId = ProductId.of(command.productId());

    // Load cart aggregate
    ShoppingCart cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new CartNotFoundException(cartId));

    // Verify product exists (separate query)
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));

    // Verify product is available
    if (!product.isAvailable()) {
      throw new ProductNotAvailableException(productId);
    }

    // Add item (stores ID only)
    cart.addItem(productId, new Quantity(command.quantity()), product.price());

    // Save cart
    cartRepository.save(cart);

  // Publish events
  eventPublisher.publishAndClearEvents(cart);
}
```

### ArchUnit Enforcement

```groovy
// DddTacticalPatternsArchUnitTest.groovy

def "Aggregate Roots must not have fields with other Aggregate Root types"() {
  given:
  def aggregateRootClasses = getAggregateRootClasses()

  expect:
  "Aggregate Roots should only reference other Aggregate Roots by ID, not by direct reference"

  noFields()
    .that()
    .areDeclaredInClassesThat(implementAggregateRoot())
    .should()
    .haveRawType(anyAggregateRootType())
    .check(allClasses)
}

def "Entities must not have fields with Aggregate Root types"() {
  expect:
  "Entities should only reference Aggregates by ID"

  noFields()
    .that()
    .areDeclaredInClassesThat(implementEntity())
    .should()
    .haveRawType(anyAggregateRootType())
    .check(allClasses)
}
```

**Tests automatically fail** if any code violates this rule.

---

## Alternatives Considered

### Alternative 1: Allow direct references with lazy loading

**Rejected**:
- Creates complex object graphs
- JPA proxy issues
- Performance problems (N+1 queries)
- Unclear transaction boundaries
- Difficult to test

### Alternative 2: Shared aggregate roots

**Rejected**:
- Violates aggregate autonomy
- Complex lifecycle management
- Coupling between aggregates
- Concurrency issues

### Alternative 3: Store entire aggregate as value object

**Rejected**:
- Duplication of data
- Synchronization problems
- Stale data issues
- Violates single source of truth

---

## References

### Domain-Driven Design Books

1. **Vaughn Vernon - Implementing Domain-Driven Design (2013)**:

   **Vernon's 4 Rules of Aggregate Design**:
   1. Model true invariants in consistency boundaries
   2. Design small aggregates
   3. **Reference other aggregates by identity only** ⭐
   4. Update other aggregates using eventual consistency

2. **Eric Evans - Domain-Driven Design (2003)**:
   > "Prefer references to external Aggregates only by their identifier, not by holding direct object references."

3. **Vaughn Vernon - Effective Aggregate Design (Paper)**:
   > "Rule 3: Reference Other Aggregates By Identity. One aggregate may hold references to the Root of other aggregates, but only by the unique identity rather than holding a direct object reference."

### Related Patterns

- **Eventual Consistency**: Aggregates coordinate via events
- **Repository Pattern**: Fetch aggregates by ID
- **Domain Events**: Communicate changes between aggregates
- **Saga Pattern**: Long-running processes across aggregates

### Related ADRs

- [ADR-005: Domain Events Publishing Strategy](adr-005-domain-events-publishing.md)
- [ADR-011: Bounded Context Isolation](adr-011-bounded-context-isolation.md)

### External References

- [Vernon's Aggregate Design Rules](https://www.dddcommunity.org/library/vernon_2011/)
- [Effective Aggregate Design](https://www.slideshare.net/VaughnVernon/effective-aggregate-design)

---

## Validation

### ArchUnit Tests

```bash
./gradlew test-architecture

# Expected output:
# DddTacticalPatternsArchUnitTest > Aggregate Roots must not have fields with other Aggregate Root types PASSED ✅
# DddTacticalPatternsArchUnitTest > Entities must not have fields with Aggregate Root types PASSED ✅
```

### Code Review Checklist

When reviewing code, verify:
- [ ] Aggregate fields only contain IDs of other aggregates
- [ ] No direct object references across aggregate boundaries
- [ ] Application services coordinate aggregates
- [ ] Domain events used for aggregate coordination
- [ ] ArchUnit tests pass

---

## Review and Update

**Next Review**: January 24, 2026
**Review Frequency**: Annually

**Update Criteria**:
- New aggregate relationships discovered
- Performance issues arise
- Team feedback on coordination complexity

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
