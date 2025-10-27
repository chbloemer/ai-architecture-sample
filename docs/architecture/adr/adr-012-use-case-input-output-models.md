# ADR-012: Use Case Input/Output Models (Command/Query Pattern)

**Date**: October 24, 2025
**Status**: 🟡 Proposed - **NOT YET IMPLEMENTED**
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

Currently, application services use primitive parameters:

```java
// application/ProductApplicationService.java
public Product createProduct(
    SKU sku,
    ProductName name,
    ProductDescription description,
    Price price,
    Category category,
    ProductStock stock) {

  Product product = productFactory.createProduct(sku, name, description, price, category, stock);
  productRepository.save(product);
  eventPublisher.publishAndClearEvents(product);
  return product;
}
```

### Problems with Current Approach

1. **Long Parameter Lists**: 6+ parameters make methods hard to use
2. **No Validation**: Validation scattered across layers
3. **Implicit Contracts**: What parameters are required? Optional?
4. **Difficult Versioning**: Adding parameter = breaking change
5. **Hard to Test**: Creating test data requires many parameters

**Example of Problems**:
```java
// Which parameters are required? Which are optional?
productService.createProduct(sku, name, null, price, category, stock);  // null allowed?

// Easy to swap parameters (same type)
productService.createProduct(name, sku, ...);  // Compiles but wrong!

// Adding new parameter breaks all callers
productService.createProduct(sku, name, description, price, category, stock, supplier);  // Breaking change
```

---

## Decision

**Application services SHOULD use explicit Command/Query objects for input, and Result/DTO objects for output.**

### Proposed Pattern

**Command Model (Input)**:
```java
// application/command/CreateProductCommand.java
public record CreateProductCommand(
    @NonNull SKU sku,
    @NonNull ProductName name,
    @NonNull ProductDescription description,
    @NonNull Price price,
    @NonNull Category category,
    @NonNull ProductStock stock
) {
  // Compact constructor for validation
  public CreateProductCommand {
    if (sku == null) throw new IllegalArgumentException("SKU required");
    if (name == null) throw new IllegalArgumentException("Name required");
    if (price == null) throw new IllegalArgumentException("Price required");
    // All validation in one place
  }

  // Convenience factory for common case
  public static CreateProductCommand withDefaults(SKU sku, ProductName name, Price price) {
    return new CreateProductCommand(
        sku, name,
        new ProductDescription(""),
        price,
        Category.UNCATEGORIZED,
        ProductStock.outOfStock()
    );
  }
}
```

**Application Service (Refactored)**:
```java
// application/ProductApplicationService.java
public class ProductApplicationService {

  // Clear contract: one command object
  public Product createProduct(CreateProductCommand command) {
    // All validation already done in command
    Product product = productFactory.createProduct(
        command.sku(),
        command.name(),
        command.description(),
        command.price(),
        command.category(),
        command.stock()
    );

    productRepository.save(product);
    eventPublisher.publishAndClearEvents(product);

    return product;
  }
}
```

**Query Model**:
```java
// application/query/FindProductsQuery.java
public record FindProductsQuery(
    Optional<Category> category,
    Optional<PriceRange> priceRange,
    Optional<Boolean> availableOnly,
    int page,
    int pageSize
) {
  public FindProductsQuery {
    if (page < 0) throw new IllegalArgumentException("Page must be non-negative");
    if (pageSize <= 0 || pageSize > 100) {
      throw new IllegalArgumentException("Page size must be between 1 and 100");
    }
  }

  // Convenience factory for simple case
  public static FindProductsQuery all(int page, int pageSize) {
    return new FindProductsQuery(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        page,
        pageSize
    );
  }

  public static FindProductsQuery byCategory(Category category, int page, int pageSize) {
    return new FindProductsQuery(
        Optional.of(category),
        Optional.empty(),
        Optional.empty(),
        page,
        pageSize
    );
  }
}
```

---

## Rationale

### 1. **Explicit Contracts**

```java
// ✅ Clear contract - what's required is obvious
CreateProductCommand command = new CreateProductCommand(sku, name, description, price, category, stock);
productService.createProduct(command);

// ❌ Current - unclear contract
productService.createProduct(sku, name, description, price, category, stock);
```

### 2. **Validation in One Place**

```java
// Command validates itself
public record CreateProductCommand(...) {
  public CreateProductCommand {
    if (sku == null) throw new IllegalArgumentException("SKU required");
    if (name == null) throw new IllegalArgumentException("Name required");
    if (price == null) throw new IllegalArgumentException("Price required");
    // All validation here
  }
}

// Application service receives valid command
public Product createProduct(CreateProductCommand command) {
  // No validation needed - command is valid
}
```

### 3. **Easier Versioning**

```java
// Adding optional field = no breaking change
public record CreateProductCommand(
    @NonNull SKU sku,
    @NonNull ProductName name,
    @NonNull ProductDescription description,
    @NonNull Price price,
    @NonNull Category category,
    @NonNull ProductStock stock,
    Optional<Supplier> supplier  // ← Added, but optional
) { ... }

// Old code still works (uses factory method)
CreateProductCommand.withDefaults(sku, name, price);  // ✓ Still works
```

### 4. **Better Testing**

```java
// Test data builder pattern
class CreateProductCommandBuilder {
  public static CreateProductCommand aValidCommand() {
    return new CreateProductCommand(
        SKU.of("TEST-001"),
        ProductName.of("Test Product"),
        ProductDescription.of("Test"),
        Price.of(Money.euro(new BigDecimal("99.99"))),
        Category.of("ELECTRONICS"),
        ProductStock.of(10)
    );
  }
}

@Test
void shouldCreateProduct() {
  CreateProductCommand command = aValidCommand();  // Easy!
  Product product = productService.createProduct(command);
  assertThat(product).isNotNull();
}
```

---

## Consequences

### Positive

✅ **Explicit Contracts**: Clear input/output contracts
✅ **Centralized Validation**: Validation in command/query
✅ **Easier Versioning**: Add optional fields without breaking changes
✅ **Better Testing**: Test data builders easier
✅ **Type Safety**: Cannot swap parameters (different types)
✅ **Documentation**: Command/query documents use case
✅ **IDE Support**: Autocomplete shows required fields

### Neutral

⚠️ **More Classes**: One command/query class per use case
⚠️ **Refactoring Needed**: Existing code needs updates

### Negative

❌ **Breaking Change**: Existing callers need updates

---

## Implementation Plan

### Phase 1: Create Command/Query Infrastructure

Create base patterns and examples:

1. Create `application/command/` package
2. Create `application/query/` package
3. Create example: `CreateProductCommand`
4. Create example: `FindProductsQuery`

### Phase 2: Refactor Application Services

Refactor one service at a time:

1. **ProductApplicationService**:
   - `createProduct(CreateProductCommand)`
   - `updateProduct(UpdateProductCommand)`
   - `findProducts(FindProductsQuery)`

2. **ShoppingCartApplicationService**:
   - `addItem(AddItemToCartCommand)`
   - `checkout(CheckoutCartCommand)`

### Phase 3: Update Primary Adapters

Update REST controllers to use commands:

```java
@PostMapping
public ResponseEntity<ProductDto> createProduct(@RequestBody CreateProductRequest request) {
  // Map request to command
  CreateProductCommand command = new CreateProductCommand(
      SKU.of(request.sku()),
      ProductName.of(request.name()),
      ProductDescription.of(request.description()),
      Price.of(Money.euro(request.price())),
      Category.of(request.category()),
      ProductStock.of(request.stock())
  );

  Product product = productService.createProduct(command);
  return ResponseEntity.ok(converter.toDto(product));
}
```

### Phase 4: Add ArchUnit Tests

```groovy
def "Application Service methods should accept Command or Query objects"() {
  expect:
  methods()
    .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("ApplicationService")
    .and().arePublic()
    .should().haveRawParameterTypes(implementCommand().or(implementQuery()))
    .check(allClasses)
}
```

---

## Alternatives Considered

### Alternative 1: Keep Current Approach (Primitives)

**Rejected**: Long parameter lists, unclear contracts

### Alternative 2: Use Maps for Parameters

```java
public Product createProduct(Map<String, Object> params) { ... }
```

**Rejected**: No type safety, no validation, unclear contract

### Alternative 3: Builder Pattern

```java
productService.createProduct()
    .withSku(sku)
    .withName(name)
    .withPrice(price)
    .build();
```

**Rejected**: More complex than command pattern, mutation during building

---

## References

### Patterns

- **Command Pattern** (GoF): Encapsulate request as object
- **CQRS** (Command Query Responsibility Segregation): Separate commands and queries
- **Parameter Object** (Refactoring): Replace parameter list with object

### Books

- **Martin Fowler - Refactoring**: Introduce Parameter Object
- **Vernon - Implementing DDD**: Application Services with commands
- **Greg Young - CQRS**: Command/Query separation

### Related ADRs

- None yet (this is a proposed pattern)

---

## Validation

### Success Criteria

- [ ] All application service methods use command/query objects
- [ ] Validation logic centralized in commands/queries
- [ ] ArchUnit tests enforce pattern
- [ ] Test data builders created
- [ ] Documentation updated

### Tests

```java
@Test
void commandShouldValidateRequiredFields() {
  assertThatThrownBy(() ->
      new CreateProductCommand(null, name, description, price, category, stock)
  ).isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("SKU required");
}

@Test
void shouldCreateProductWithValidCommand() {
  CreateProductCommand command = aValidCommand();

  Product product = productService.createProduct(command);

  assertThat(product).isNotNull();
  assertThat(product.sku()).isEqualTo(command.sku());
}
```

---

## Migration Strategy

### Step 1: Add Pattern Alongside Current Code

Keep existing methods, add new ones:

```java
// Old (deprecated)
@Deprecated
public Product createProduct(SKU sku, ProductName name, ...) {
  return createProduct(new CreateProductCommand(sku, name, ...));
}

// New
public Product createProduct(CreateProductCommand command) {
  // Implementation
}
```

### Step 2: Update Callers Gradually

Update one caller at a time to use new API.

### Step 3: Remove Old Methods

Once all callers updated, remove deprecated methods.

---

## Review and Update

**Next Review**: After implementation
**Implementation Target**: Q1 2026

**Update Criteria**:
- Pattern implemented and tested
- Team feedback collected
- ArchUnit tests passing
- Documentation complete

---

## Decision

**Status**: 🟡 Proposed

This ADR is **proposed** and awaits:
1. Team discussion
2. Prototype implementation
3. Team approval
4. Full implementation

---

**Proposed by**: Architecture Team
**Date**: October 24, 2025
**Version**: 0.1 (Draft)
