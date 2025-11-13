# Architecture Principles

This document describes the architectural patterns and principles used in the AI Architecture Sample Project.

## Table of Contents

1. [Overview](#overview)
2. [Domain-Driven Design (DDD)](#domain-driven-design-ddd)
3. [Clean Architecture (Use Cases)](#clean-architecture-use-cases)
4. [Hexagonal Architecture](#hexagonal-architecture)
5. [Onion Architecture](#onion-architecture)
6. [Layered Architecture](#layered-architecture)
7. [Package Structure](#package-structure)
8. [Bounded Contexts](#bounded-contexts)
9. [Architectural Rules](#architectural-rules)

---

## Overview

This sample project demonstrates a modern, maintainable architecture for enterprise applications using three complementary architectural patterns:

- **Domain-Driven Design (DDD)**: Focus on the core domain and domain logic
- **Clean Architecture**: Use case driven approach with explicit input/output models
- **Hexagonal Architecture**: Separation of business logic from technical concerns (Ports & Adapters)
- **Onion Architecture**: Dependencies flow inward toward the domain core

These patterns work together to create a flexible, testable, and maintainable codebase that can evolve with changing business requirements.

---

## Domain-Driven Design (DDD)

Domain-Driven Design is an approach to software development that centers the development on programming a domain model that has a rich understanding of the processes and rules of the domain.

### Strategic Patterns

#### Bounded Contexts

A bounded context is an explicit boundary within which a domain model is defined and applicable. Our e-commerce application has two bounded contexts plus a shared kernel:

1. **Shared Kernel** (`de.sample.aiarchitecture.sharedkernel.domain.common`)
   - Small, carefully curated domain model shared across contexts
   - Value Objects: `Money`, `ProductId`, `Price`
   - **Pattern**: Shared Kernel (Eric Evans, DDD Chapter 14)
   - **Trade-off**: Creates coupling but ensures consistency for universal concepts

2. **Product Catalog Context** (`de.sample.aiarchitecture.product.domain.model`)
   - Manages products, pricing, inventory
   - Aggregate Root: `Product`
   - Value Objects: `SKU`, `ProductName`, `ProductDescription`, `ProductStock`, `Category`
   - Depends on: Shared Kernel

3. **Shopping Cart Context** (`de.sample.aiarchitecture.cart.domain.model`)
   - Manages shopping carts and checkout
   - Aggregate Root: `ShoppingCart`
   - Entity: `CartItem`
   - Value Objects: `CartId`, `CartItemId`, `Quantity`, `CartStatus`
   - Depends on: Shared Kernel

**Context Mapping Strategy:**
- **Shared Kernel Pattern**: `Money`, `ProductId`, `Price` are shared across contexts
- Cart references Product by `ProductId` only (from Shared Kernel)
- Product and Cart contexts are **isolated** - no direct dependencies
- Both contexts may access Shared Kernel
- No direct aggregate-to-aggregate references

**Why Shared Kernel?**
- **Consistency**: Single definition of `Money` prevents currency handling bugs
- **Reduces Duplication**: Avoid reimplementing universal concepts
- **Clear Boundaries**: Explicit separation of shared vs. context-specific

**What Belongs in Shared Kernel:**
- ✅ Universal value objects (`Money`, monetary primitives)
- ✅ Cross-context identifiers (`ProductId` used by both Product and Cart)
- ✅ Common domain primitives with universal meaning

**What Does NOT Belong in Shared Kernel:**
- ❌ Aggregates (each belongs to exactly one context)
- ❌ Context-specific business logic
- ❌ Infrastructure or technical concerns

### Tactical Patterns

#### Aggregate Root

An aggregate is a cluster of domain objects that can be treated as a single unit. The aggregate root is the entry point to the aggregate.

**Example: Product Aggregate**

```java
public final class Product implements AggregateRoot<Product, ProductId> {
    private final ProductId id;
    private final SKU sku;
    private ProductName name;
    private Price price;
    private ProductStock stock;

    public void changePrice(Price newPrice) {
        if (newPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        this.price = newPrice;
    }
}
```

**Example: ShoppingCart Aggregate**

```java
public final class ShoppingCart extends BaseAggregateRoot<ShoppingCart, CartId> {
    private final CartId id;
    private final CustomerId customerId;
    private final List<CartItem> items;  // CartItem is an entity within this aggregate
    private CartStatus status;

    public void addItem(ProductId productId, Quantity quantity, Price price) {
        ensureCartIsActive();

        // Check if product already in cart
        Optional<CartItem> existingItem = findItemByProductId(productId);
        if (existingItem.isPresent()) {
            // Increase quantity of existing item
            existingItem.get().updateQuantity(Quantity.of(
                existingItem.get().quantity().value() + quantity.value()));
        } else {
            // Add new item
            items.add(new CartItem(CartItemId.generate(), productId, quantity, price));
        }

        // Raise domain event
        registerEvent(CartItemAddedToCart.now(this.id, productId, quantity));
    }
}
```

**Rules:**
1. External objects can only reference the aggregate root (never entities within)
2. Aggregate boundaries ensure invariants are maintained
3. Aggregates reference other aggregates by identity only (e.g., ShoppingCart references Product via ProductId)
4. Aggregates raise domain events for important state changes

**Implementation:** See `de.sample.aiarchitecture.sharedkernel.domain.marker.AggregateRoot`

#### Entity

An entity is an object that has a distinct identity that runs through time and different states.

**Example: CartItem Entity**

```java
public final class CartItem implements Entity<CartItem, CartItemId> {
    private final CartItemId id;
    private final ProductId productId;
    private Quantity quantity;
    private final Price priceAtAddition;

    // Package-private constructor enforces aggregate boundary
    CartItem(CartItemId id, ProductId productId, Quantity quantity, Price priceAtAddition) {
        // ...
    }
}
```

**Rules:**
1. Has a unique identity
2. Identity remains constant through state changes
3. Equality based on identity, not attributes

**Implementation:** See `de.sample.aiarchitecture.sharedkernel.domain.marker.Entity`

#### Value Object

A value object is an immutable object that describes some characteristic or attribute but has no conceptual identity.

**Example: Money Value Object (from Shared Kernel)**

```java
public record Money(@NonNull BigDecimal amount, @NonNull Currency currency) implements Value {
    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        // Normalize scale to 2 decimal places
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money add(final Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(amount.add(other.amount), currency);
    }
}
```

**Rules:**
1. Immutable - use Java records
2. Equality based on attributes, not identity
3. Can be shared between aggregates and contexts (Shared Kernel)
4. Contains validation logic
5. Universal value objects belong in Shared Kernel

**Implementation:**
- Interface: `de.sample.aiarchitecture.sharedkernel.domain.marker.Value`
- Shared Value Objects: `de.sample.aiarchitecture.sharedkernel.domain.common.Money`, `ProductId`, `Price`
- Context-specific Value Objects: In their respective bounded contexts

#### Repository

A repository encapsulates the logic for accessing domain objects from a data store, presenting a collection-like interface. Repositories mediate between the domain and data mapping layers, providing the illusion of an in-memory collection of aggregates.

**Base Repository Interface**

All repositories extend a common base interface that provides essential operations:

```java
public interface Repository<T extends AggregateRoot<T, ID>, ID extends Id> {
    // Find aggregate by unique identifier
    Optional<T> findById(@NonNull ID id);

    // Save aggregate (add or update)
    T save(@NonNull T aggregate);

    // Remove aggregate from collection
    void deleteById(@NonNull ID id);
}
```

**Example: Product Repository Interface**

Specific repositories add domain-specific query methods using ubiquitous language:

```java
public interface ProductRepository extends Repository<Product, ProductId> {
    // Common methods inherited from Repository:
    // - Optional<Product> findById(ProductId id)
    // - Product save(Product product)
    // - void deleteById(ProductId id)

    // Domain-specific query methods:
    Optional<Product> findBySku(@NonNull SKU sku);
    List<Product> findByCategory(@NonNull Category category);
    List<Product> findAll();
    boolean existsBySku(@NonNull SKU sku);
}
```

**Example: Repository Implementation**

```java
@Repository
public class InMemoryProductRepository implements ProductRepository {
    private final ConcurrentHashMap<ProductId, Product> products = new ConcurrentHashMap<>();

    @Override
    public Optional<Product> findById(ProductId id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public Product save(Product product) {
        products.put(product.id(), product);
        return product;
    }

    @Override
    public void deleteById(ProductId id) {
        products.remove(id);
    }

    // Domain-specific implementations...
}
```

**Rules:**
1. Interface lives in domain layer
2. Implementation lives in infrastructure/adapter layer (secondary adapter)
3. One repository per aggregate root (not per entity)
4. Collection-oriented interface (not generic CRUD)
5. Use ubiquitous language in method names
6. Return immutable collections when appropriate
7. Base interface provides common operations (findById, save, deleteById)

**Benefits:**
- DRY principle: Common methods defined once in base interface
- Type safety: Generic interface ensures type-safe implementations
- Consistency: All repositories follow the same pattern
- Clear contracts: Base interface defines what all repositories must provide
- Fluent API: save() returning aggregate enables method chaining

**Implementation:**
- Base Interface: `de.sample.aiarchitecture.sharedkernel.application.port.Repository`
- Domain Interfaces: `ProductRepository`, `ShoppingCartRepository`
- Implementations: `InMemoryProductRepository`, `InMemoryShoppingCartRepository` (in `portadapter.outgoing`)

#### Domain Service

A domain service contains domain logic that doesn't naturally fit within an entity or value object.

**Example: Cart Total Calculator**

```java
public class CartTotalCalculator implements DomainService {
    public Money calculateTotal(final ShoppingCart cart) {
        if (cart.items().isEmpty()) {
            return Money.zero(Currency.getInstance("EUR"));
        }

        return cart.items().stream()
            .map(item -> item.priceAtAddition().multiply(item.quantity().value()))
            .reduce(Money::add)
            .orElse(Money.zero(Currency.getInstance("EUR")));
    }
}
```

**Rules:**
1. Stateless - only final fields for dependencies
2. Framework-independent (no Spring annotations)
3. Operates on domain objects
4. Named after activities, not entities

**Implementation:** See `de.sample.aiarchitecture.sharedkernel.domain.marker.DomainService`

#### Domain Event

A domain event is something that happened in the domain that domain experts care about. Events enable eventual consistency, loose coupling between aggregates and bounded contexts, and support audit trails.

**Example: Product Price Changed Event**

```java
public final record ProductPriceChanged(
    @NonNull UUID eventId,
    @NonNull ProductId productId,
    @NonNull Price oldPrice,
    @NonNull Price newPrice,
    @NonNull Instant occurredOn,
    int version
) implements DomainEvent {
    public static ProductPriceChanged now(
        ProductId productId,
        Price oldPrice,
        Price newPrice) {
        return new ProductPriceChanged(
            UUID.randomUUID(),
            productId,
            oldPrice,
            newPrice,
            Instant.now(),
            1
        );
    }
}
```

**Example: Shopping Cart Events**

```java
// Event raised when a product is added to cart
public record CartItemAddedToCart(
    @NonNull UUID eventId,
    @NonNull CartId cartId,
    @NonNull ProductId productId,
    @NonNull Quantity quantity,
    @NonNull Instant occurredOn,
    int version
) implements DomainEvent {
    public static CartItemAddedToCart now(CartId cartId, ProductId productId, Quantity quantity) {
        return new CartItemAddedToCart(UUID.randomUUID(), cartId, productId, quantity, Instant.now(), 1);
    }
}

// Event raised when a product is removed from cart
public record ProductRemovedFromCart(
    @NonNull UUID eventId,
    @NonNull CartId cartId,
    @NonNull ProductId productId,
    @NonNull Instant occurredOn,
    int version
) implements DomainEvent {
    public static ProductRemovedFromCart now(CartId cartId, ProductId productId) {
        return new ProductRemovedFromCart(UUID.randomUUID(), cartId, productId, Instant.now(), 1);
    }
}
```

**Rules:**
1. Named in past tense (something that happened)
2. Immutable (use records or final classes)
3. Contains timestamp (occurredOn), unique ID (eventId), and version
4. Framework-independent
5. Version supports event schema evolution

**Event Publishing Pattern:**

```java
// 1. Aggregate raises event during state change
public void changePrice(Price newPrice) {
    Price oldPrice = this.price;
    this.price = newPrice;
    registerEvent(ProductPriceChanged.now(this.id, oldPrice, newPrice));
}

// 2. Application Service saves and publishes events
Product product = productRepository.save(product);
eventPublisher.publishAndClearEvents(product);
```

**Event Handling:**

```java
@Component
public class ProductEventListener {
    @EventListener
    public void onProductPriceChanged(ProductPriceChanged event) {
        // Handle cross-aggregate coordination
        // Update read models
        // Send notifications
        // Trigger business processes
    }
}
```

**Benefits:**
- Enables eventual consistency across aggregates (Vernon's Rule #4)
- Loose coupling between bounded contexts
- Audit trail and event sourcing capability
- Asynchronous processing support
- Time-travel debugging

**Implementation:**
- Interface: `de.sample.aiarchitecture.sharedkernel.domain.marker.DomainEvent`
- Publisher Interface (SPI): `de.sample.aiarchitecture.sharedkernel.application.port.DomainEventPublisher`
- Publisher Implementation: `de.sample.aiarchitecture.infrastructure.config.SpringDomainEventPublisher`
- Examples: `ProductCreated`, `ProductPriceChanged`, `CartItemAddedToCart`, `CartCheckedOut`

**Event Publishing Infrastructure:**

The event publishing infrastructure follows the Dependency Inversion Principle to keep the application layer framework-independent:

```java
// Interface (outbound port) in sharedkernel.application.port - application layer depends on this
public interface DomainEventPublisher {
    void publish(DomainEvent event);
    void publishAndClearEvents(AggregateRoot<?, ?> aggregate);
}

// Implementation in infrastructure.config - uses Spring framework
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(DomainEvent event) {
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publishAndClearEvents(AggregateRoot<?, ?> aggregate) {
        aggregate.domainEvents().forEach(this::publish);
        aggregate.clearDomainEvents();
    }
}
```

**Benefits:**
- Application layer remains framework-independent (depends on interface, not Spring)
- Easy to mock for testing
- Can swap implementations (e.g., message broker, in-memory for tests)
- Follows Hexagonal Architecture (port defined in sharedkernel.application.port, adapter in infrastructure.config)

#### Integration Event

Integration Events are domain events that cross bounded context boundaries and represent public contracts between contexts. They are a special category of domain events with stricter requirements for versioning, backward compatibility, and consumption patterns.

**Key Differences from Internal Domain Events:**

| Aspect | Internal Events | Integration Events |
|--------|----------------|-------------------|
| **Scope** | Within one bounded context | Cross bounded context boundaries |
| **Versioning** | Can change freely | Strict backward compatibility required |
| **Documentation** | Informal JavaDoc | Formal schema documentation (YAML/AsyncAPI) |
| **Consumption** | Direct usage | Via Anti-Corruption Layer |
| **Evolution** | Refactor as needed | Breaking changes require new versions |

**Example: CartCheckedOut Integration Event**

```java
public record CartCheckedOut(
    @NonNull UUID eventId,
    @NonNull CartId cartId,
    @NonNull CustomerId customerId,
    @NonNull Money totalAmount,
    int itemCount,
    @NonNull List<ItemInfo> items,  // DTO, not domain objects
    @NonNull Instant occurredOn,
    int version
) implements IntegrationEvent {

    // Lightweight DTO using Shared Kernel types only
    public record ItemInfo(
        @NonNull ProductId productId,  // From Shared Kernel
        int quantity                    // Primitive
    ) {}

    public static CartCheckedOut now(
        CartId cartId,
        CustomerId customerId,
        Money totalAmount,
        int itemCount,
        List<CartItem> cartItems) {

        // Convert domain objects to DTOs for cross-context communication
        List<ItemInfo> itemInfos = cartItems.stream()
            .map(item -> new ItemInfo(item.productId(), item.quantity().value()))
            .toList();

        return new CartCheckedOut(
            UUID.randomUUID(),
            cartId,
            customerId,
            totalAmount,
            itemCount,
            itemInfos,
            Instant.now(),
            1  // Version 1
        );
    }
}
```

**Rules:**
1. Use DTOs with Shared Kernel types or primitives only (never full domain objects from other contexts)
2. Implement `IntegrationEvent` marker interface (not just `DomainEvent`)
3. Version field must be actively managed for schema evolution
4. Document schema in event catalog (see `docs/events/cart-checked-out.yaml`)
5. Consumers must use Anti-Corruption Layer to translate events
6. Maintain backward compatibility across versions

**Anti-Corruption Layer Pattern:**

When consuming integration events from other bounded contexts, use an Anti-Corruption Layer (ACL) to translate the event into your context's ubiquitous language.

```java
// ACL translates Cart events into Product commands
@Component
public class CartEventTranslator {

    public List<ReduceProductStockCommand> translate(CartCheckedOut event) {
        // Handle version-specific translation
        return switch (event.version()) {
            case 1 -> translateV1(event);
            case 2 -> translateV2(event);
            default -> throw new UnsupportedEventVersionException(
                "Unsupported CartCheckedOut version: " + event.version()
            );
        };
    }

    private List<ReduceProductStockCommand> translateV1(CartCheckedOut event) {
        return event.items().stream()
            .map(item -> new ReduceProductStockCommand(
                item.productId().value().toString(),
                item.quantity()
            ))
            .toList();
    }

    private List<ReduceProductStockCommand> translateV2(CartCheckedOut event) {
        // Handle v2 schema changes (future)
        return translateV1(event);
    }
}
```

**Event Consumer with ACL:**

```java
@Component
public class ProductStockEventListener {
    private final ReduceProductStockUseCase reduceProductStockUseCase;
    private final CartEventTranslator cartEventTranslator;  // ACL

    @EventListener
    public void onCartCheckedOut(CartCheckedOut event) {
        try {
            // Use ACL to translate Cart event → Product commands
            List<ReduceProductStockCommand> commands =
                cartEventTranslator.translate(event);

            // Execute commands in Product's ubiquitous language
            commands.forEach(reduceProductStockUseCase::execute);

        } catch (UnsupportedEventVersionException e) {
            logger.error("Unsupported event version: {}", event.version());
            // Alert DevOps, version mismatch between contexts
        }
    }
}
```

**Benefits:**
- **Context Isolation:** Product context doesn't directly depend on Cart's event structure
- **Evolution:** Cart can change event schema, only ACL needs updating
- **Versioning:** ACL handles multiple event versions transparently
- **Translation:** Converts Cart's language to Product's language
- **Testability:** ACL can be tested independently

**Event Versioning Strategy:**

1. **Version 1 (current):** Basic event structure
2. **Version 2 (future):** Add optional fields (e.g., warehouse allocation, batch numbers)
3. **ACL handles both:** Consumers remain unaffected by version changes
4. **Backward compatibility:** New fields must be optional

**Implementation:**
- Interface: `de.sample.aiarchitecture.sharedkernel.domain.marker.IntegrationEvent`
- Example Event: `de.sample.aiarchitecture.cart.domain.event.CartCheckedOut`
- ACL Example: `de.sample.aiarchitecture.product.adapter.incoming.event.acl.CartEventTranslator`
- Consumer Example: `de.sample.aiarchitecture.product.adapter.incoming.event.ProductStockEventListener`
- Schema Documentation: `docs/events/cart-checked-out.yaml`

**When to Use Integration Events:**
- ✅ Communication between different bounded contexts (e.g., Cart → Product)
- ✅ Enabling eventual consistency across contexts
- ✅ Decoupling autonomous services/modules
- ✅ Events that external consumers depend on

**When NOT to Use (use DomainEvent instead):**
- ❌ Communication within the same bounded context
- ❌ Triggering side effects in the same aggregate
- ❌ Internal notifications that don't cross context boundaries

#### Factory

A factory encapsulates complex object creation logic.

**Example: Product Factory**

```java
public class ProductFactory implements Factory {
    private final ProductIdGenerator idGenerator;

    public ProductFactory(final ProductIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Product createProduct(
        final SKU sku,
        final ProductName name,
        final ProductDescription description,
        final Price price,
        final ProductStock initialStock,
        final Category category
    ) {
        final ProductId id = idGenerator.nextId();
        return new Product(id, sku, name, description, price, initialStock, category);
    }
}
```

**Rules:**
1. Encapsulates complex creation logic
2. Ensures invariants are satisfied at creation
3. Framework-independent
4. Stateless

**Implementation:** See `de.sample.aiarchitecture.sharedkernel.domain.marker.Factory`

#### Specification

A specification encapsulates business rules and can be combined for complex queries.

**Example: Product Available Specification**

```java
public class ProductAvailableSpecification implements Specification<Product> {
    @Override
    public boolean isSatisfiedBy(final Product product) {
        return product.isAvailable();
    }
}
```

**Rules:**
1. Encapsulates business rule
2. Can be combined (AND, OR, NOT)
3. Framework-independent
4. Reusable across use cases

**Implementation:** See `de.sample.aiarchitecture.sharedkernel.domain.marker.Specification`

#### Custom Annotations (Shared Kernel Common Layer)

Custom framework-agnostic annotations belong in the Shared Kernel when they represent cross-cutting technical concerns used across multiple bounded contexts.

**Example: @AsyncInitialize Annotation**

The `@AsyncInitialize` annotation marks components for asynchronous initialization after bean construction, enabling non-blocking startup of services that require heavy initialization tasks.

**Annotation Definition (Shared Kernel):**

```java
// sharedkernel/common/annotation/AsyncInitialize.java
package de.sample.aiarchitecture.sharedkernel.common.annotation;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsyncInitialize {
  int priority() default 100;
  String description() default "";
}
```

**Processor Implementation (Infrastructure):**

```java
// infrastructure/config/AsyncInitializationProcessor.java
@Component
public class AsyncInitializationProcessor implements BeanPostProcessor, Ordered {

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    AsyncInitialize annotation =
        AnnotationUtils.findAnnotation(bean.getClass(), AsyncInitialize.class);

    if (annotation != null) {
      Method initMethod = bean.getClass().getMethod("asyncInitialize");
      initMethod.invoke(bean);
    }
    return bean;
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
```

**Usage Example (Adapter):**

```java
// product/adapter/outgoing/persistence/InMemoryProductRepository.java
@Repository
@AsyncInitialize(priority = 50, description = "Warm up product cache")
public class InMemoryProductRepository implements ProductRepository {

  @Async
  public void asyncInitialize() {
    logger.info("Starting async initialization of ProductRepository cache...");
    // Preload frequently accessed products
    // Build search indexes
    // Warm up caches
    logger.info("ProductRepository cache warmup completed.");
  }
}

// cart/adapter/outgoing/persistence/InMemoryShoppingCartRepository.java
@Repository
@AsyncInitialize(priority = 100, description = "Initialize shopping cart metrics")
public class InMemoryShoppingCartRepository implements ShoppingCartRepository {

  @Async
  public void asyncInitialize() {
    logger.info("Starting async initialization of ShoppingCartRepository...");
    // Initialize metrics collectors
    // Preload abandoned cart data
    logger.info("ShoppingCartRepository initialization completed.");
  }
}
```

**Rules:**
1. Annotation definition in `sharedkernel.common.annotation` (framework-agnostic)
2. Processor implementation in `infrastructure.config` (framework-specific)
3. Pure Java annotation with no Spring dependencies
4. Method name convention: `asyncInitialize()` annotated with `@Async`
5. Priority determines initialization order (lower values first)

**Benefits:**
- Framework-independent annotation (pure Java metadata)
- Application layer can use it without depending on infrastructure
- Easy to test and mock
- Separation of concerns: annotation vs. processing logic
- Follows Dependency Inversion Principle

**Implementation:**
- Annotation: `de.sample.aiarchitecture.sharedkernel.common.annotation.AsyncInitialize`
- Processor: `de.sample.aiarchitecture.infrastructure.config.AsyncInitializationProcessor`
- Configuration: `de.sample.aiarchitecture.infrastructure.config.AsyncConfiguration`
- Examples: `InMemoryProductRepository`, `InMemoryShoppingCartRepository`

---

## Clean Architecture (Use Cases)

Clean Architecture, as defined by Robert C. Martin, emphasizes organizing code around use cases - explicit representations of what users can do with the system. This section describes how we implement the Use Case pattern in the application layer.

### Use Case Pattern

A **Use Case** represents a single user action or system operation. In Clean Architecture, use cases are the primary organizing principle of the application layer, replacing traditional "service" classes with many methods.

#### Core Concepts

**Generic UseCase Contract:**
All use cases implement a generic `UseCase<I,O>` interface with a single `execute()` method. This provides a consistent contract while keeping implementations focused and testable.

**Input/Output Models:**
Use cases accept Input models and return Output models to decouple the application layer from presentation and infrastructure concerns.

**Single Responsibility:**
Each use case class represents one specific business operation (Command or Query), following the Single Responsibility Principle.

### Example: Create Product Use Case

**Base UseCase Interface**

```java
public interface UseCase<I, O> {
    @NonNull O execute(@NonNull I input);
}
```

**Input Model**

```java
public record CreateProductInput(
    String sku,
    String name,
    String description,
    BigDecimal priceAmount,
    String priceCurrency,
    String category,
    int stockQuantity
) {
    // Validation in compact constructor
    public CreateProductInput {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be null or blank");
        }
        // ... more validation
    }
}
```

**Output Model**

```java
public record CreateProductOutput(
    String productId,
    String sku,
    String name,
    String description,
    BigDecimal priceAmount,
    String priceCurrency,
    String category,
    int stockQuantity
) {}
```

**Use Case Implementation**

```java
@Service
@Transactional
public class CreateProductUseCase implements UseCase<CreateProductInput, CreateProductOutput> {
    private final ProductRepository productRepository;
    private final ProductFactory productFactory;
    private final DomainEventPublisher eventPublisher;

    @Override
    public @NonNull CreateProductOutput execute(@NonNull CreateProductInput input) {
        // 1. Validate business rules
        SKU sku = new SKU(input.sku());
        if (productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("Product with SKU already exists");
        }

        // 2. Convert input to domain objects
        ProductName name = new ProductName(input.name());
        Price price = new Price(new Money(input.priceAmount(), Currency.getInstance(input.priceCurrency())));
        // ...

        // 3. Execute domain logic
        Product product = productFactory.createProduct(sku, name, ...);

        // 4. Persist
        productRepository.save(product);

        // 5. Publish events
        eventPublisher.publishAndClearEvents(product);

        // 6. Map to output
        return new CreateProductOutput(
            product.id().value().toString(),
            product.sku().value(),
            ...
        );
    }
}
```

### Use Case Categories

#### Command Use Cases (Write)

Commands modify system state and publish domain events.

**Examples:**
- `CreateProductUseCase` - Creates a new product
- `UpdateProductPriceUseCase` - Changes product price
- `AddItemToCartUseCase` - Adds item to cart
- `CheckoutCartUseCase` - Completes cart checkout

**Characteristics:**
- Transactional (`@Transactional`)
- Validate business rules
- Modify aggregate state
- Publish domain events
- Return result data

#### Query Use Cases (Read)

Queries retrieve data without modifying state.

**Examples:**
- `GetProductByIdUseCase` - Retrieves a product
- `GetAllProductsUseCase` - Lists all products
- `GetCartByIdUseCase` - Retrieves a cart

**Characteristics:**
- Read-only (`@Transactional(readOnly = true)`)
- No state changes
- No domain events
- Return read models

### Input/Output Model Design

#### Input Models

**Rules:**
1. Immutable (Java records preferred)
2. Contain only primitive types and Strings (no domain objects)
3. Validate format in compact constructor
4. Reside in `application` package
5. Named with "Input" suffix

**Purpose:**
- Decouple use case from presentation layer DTOs
- Define explicit contract for use case
- Enable easy testing

**Example:**

```java
public record AddItemToCartInput(
    String cartId,
    String productId,
    int quantity
) {
    public AddItemToCartInput {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}
```

#### Output Models

**Rules:**
1. Immutable (Java records preferred)
2. Contain only data needed by presentation layer
3. Primitive types, Strings, and nested records
4. Reside in `application` package
5. Named with "Output" suffix

**Purpose:**
- Prevent leaking domain entities to outer layers
- Control what data is exposed
- Enable API versioning

**Example:**

```java
public record CreateProductOutput(
    String productId,
    String sku,
    String name,
    BigDecimal priceAmount,
    String priceCurrency
) {}
```

### Benefits

1. **Explicit Contracts**: Each use case is a clear, testable contract
2. **Interface Segregation**: Clients depend only on what they use
3. **Decoupling**: Input/Output models decouple layers
4. **Testability**: Easy to mock and test use cases
5. **Documentation**: Use cases document what the system does
6. **API Stability**: Output models provide versioning boundary

### Use Cases with Input/Output Ports Pattern

**Traditional Application Service:**
```java
@Service
public class ProductApplicationService {
    public Product createProduct(...) { }
    public void updatePrice(...) { }
    public Product findById(...) { }
    public List<Product> findAll() { }
    // Many methods in one class
}
```

**Use Case with Input Ports Approach:**
```java
// Input Port interface (marker interface)
interface InputPort<INPUT, OUTPUT> {
    OUTPUT execute(INPUT input);
}

// Specific Input Port for the use case
interface UpdateProductPriceInputPort extends InputPort<UpdateProductPriceCommand, UpdateProductPriceResponse> {
    UpdateProductPriceResponse execute(UpdateProductPriceCommand input);
}

// Use Case implementation
@Service
public class UpdateProductPriceUseCase implements UpdateProductPriceInputPort {
    private final ProductRepository productRepository;  // Output Port
    private final DomainEventPublisher eventPublisher;  // Output Port

    public UpdateProductPriceResponse execute(UpdateProductPriceCommand input) { ... }
}
```

**Advantages of Input Port / Use Case Approach:**
- Single Responsibility Principle (one use case per class)
- Interface Segregation (clients inject only the specific input ports they need)
- Easier to test (smaller, focused classes)
- Clearer naming (port and use case names reflect business operation)
- Better suited for microservices (can deploy use cases independently)
- Explicit Hexagonal Architecture ports (Input Ports = primary ports, Output Ports = secondary ports)
- Consistent contract enforces input/output models across all use cases

### Implementation

**Base Interface:** `de.sample.aiarchitecture.sharedkernel.application.port.UseCase<INPUT, OUTPUT>`

**Product Use Cases:**
- Input Port: `CreateProductInputPort extends InputPort<CreateProductCommand, CreateProductResponse>`
- Implementation: `CreateProductUseCase implements CreateProductInputPort`
- Input Port: `UpdateProductPriceInputPort extends InputPort<UpdateProductPriceCommand, UpdateProductPriceResponse>`
- Implementation: `UpdateProductPriceUseCase implements UpdateProductPriceInputPort`
- Input Port: `GetProductByIdInputPort extends InputPort<GetProductByIdQuery, GetProductByIdResponse>`
- Implementation: `GetProductByIdUseCase implements GetProductByIdInputPort`
- Input Port: `GetAllProductsInputPort extends InputPort<GetAllProductsQuery, GetAllProductsResponse>`
- Implementation: `GetAllProductsUseCase implements GetAllProductsInputPort`

**Shopping Cart Use Cases:**
- Input Port: `CreateCartInputPort extends InputPort<CreateCartCommand, CreateCartResponse>`
- Implementation: `CreateCartUseCase implements CreateCartInputPort`
- Input Port: `AddItemToCartInputPort extends InputPort<AddItemToCartCommand, AddItemToCartResponse>`
- Implementation: `AddItemToCartUseCase implements AddItemToCartInputPort`
- Input Port: `CheckoutCartInputPort extends InputPort<CheckoutCartCommand, CheckoutCartResponse>`
- Implementation: `CheckoutCartUseCase implements CheckoutCartInputPort`
- Input Port: `GetCartByIdInputPort extends InputPort<GetCartByIdQuery, GetCartByIdResponse>`
- Implementation: `GetCartByIdUseCase implements GetCartByIdInputPort`

**Naming Convention:**
- Input port interfaces end with "InputPort"
- Use case implementation classes end with "UseCase"
- Command input models end with "Command" (for write operations)
- Query input models end with "Query" (for read operations)
- Output models end with "Response"
- All input ports extend `InputPort<INPUT, OUTPUT>`
- All use cases implement their corresponding input port interface

### Organization by Bounded Context

Use cases are organized into subpackages matching the bounded contexts from the domain layer. This creates a clear alignment between the application layer and domain layer.

**Package Structure:**
```
application/
├── createproduct/                    # Use case: Create Product
│   ├── CreateProductInputPort        # Input port interface
│   ├── CreateProductUseCase          # Use case implementation
│   ├── CreateProductCommand          # Input model
│   └── CreateProductResponse         # Output model
├── updateproductprice/               # Use case: Update Product Price
│   ├── UpdateProductPriceInputPort
│   ├── UpdateProductPriceUseCase
│   ├── UpdateProductPriceCommand
│   └── UpdateProductPriceResponse
├── additemtocart/                    # Use case: Add Item to Cart
│   ├── AddItemToCartInputPort
│   ├── AddItemToCartUseCase
│   ├── AddItemToCartCommand
│   └── AddItemToCartResponse
└── shared/                           # Shared output ports
    ├── ProductRepository             # Product repository interface
    └── ShoppingCartRepository        # Cart repository interface
```

**Benefits:**

1. **Use Cases as First-Class Citizens** - Use cases are at the top level, mirroring how bounded contexts are organized
2. **High Cohesion** - Everything related to one use case (port, implementation, models) is co-located
3. **Clear Shared Concerns** - The `shared/` folder makes cross-cutting dependencies explicit
4. **Simpler Navigation** - Flatter structure with fewer nested folders
5. **Mirrors Bounded Context Pattern** - Same organizational principle at both context and use case levels
6. **Feature-Oriented** - Easy to find and work on complete features

**Rules:**

1. Each use case has its own package in the `application` layer
2. Input port interface, use case implementation, and models reside together in the use case package
3. Shared output ports (repositories, gateways) are defined in `application.shared` package
4. Use cases may only orchestrate domain objects from their own bounded context
5. Cross-context coordination happens via domain events, not direct use case calls
6. Adapters depend on input ports, not on use case implementations directly
7. The `shared` folder concept mirrors the `sharedkernel` pattern at the bounded context level

**Location:**
- Product Use Cases: `de.sample.aiarchitecture.product.application.{usecasename}`
- Cart Use Cases: `de.sample.aiarchitecture.cart.application.{usecasename}`
- Shared Output Ports: `de.sample.aiarchitecture.{context}.application.shared`

### Relationship to Hexagonal Architecture

In Hexagonal Architecture terminology:
- **UseCase Interface** = Base contract for all Input Ports (Primary Ports)
- **Use Case Classes** = Concrete Input Ports (e.g., CreateProductUseCase, GetCartByIdUseCase)
- **Input/Output Models** = Port Data Structures
- **Use Case Implementations** = Application Core
- **REST Controllers** = Primary Adapters (invoke use cases via dependency injection)

This alignment ensures the patterns work together cohesively. Clients inject specific use case instances they need, maintaining Interface Segregation despite using a generic contract.

---

## Hexagonal Architecture

Hexagonal Architecture (Ports and Adapters) separates the core business logic from external concerns through explicit ports and adapters.

### Core Concepts

#### Ports

Ports are interfaces that define how the application can be used or how it can use external systems.

**Primary Ports (Driving Side):**
- Application services that define use cases
- Location: `de.sample.aiarchitecture.application`

**Secondary Ports (Driven Side):**
- Repository interfaces
- Location: `de.sample.aiarchitecture.domain.model.*`

#### Adapters

Adapters are implementations that connect external systems to the ports.

**Primary Adapters (Driving / Incoming):**
- REST Controllers, Web MVC, MCP Server
- Location: `de.sample.aiarchitecture.portadapter.incoming`

**Secondary Adapters (Driven / Outgoing):**
- Repository implementations
- Location: `de.sample.aiarchitecture.portadapter.outgoing`

### Example: Product Management Flow

1. **Primary Adapter (REST Controller)** receives HTTP request
   ```java
   @RestController
   @RequestMapping("/api/products")
   public class ProductResource {
       private final UpdateProductPriceInputPort updateProductPriceInputPort;

       @PutMapping("/{id}/price")
       public ResponseEntity<ProductDto> updatePrice(
           @PathVariable String id,
           @RequestBody UpdatePriceRequest request) {

           UpdateProductPriceCommand command = new UpdateProductPriceCommand(
               id, request.newAmount(), request.currency());
           UpdateProductPriceResponse response = updateProductPriceInputPort.execute(command);

           return ResponseEntity.ok(ProductConverter.toDto(response));
       }
   }
   ```

2. **Primary Port (Input Port Interface)** defines use case contract
   ```java
   public interface UpdateProductPriceInputPort
       extends InputPort<UpdateProductPriceCommand, UpdateProductPriceResponse> {
       UpdateProductPriceResponse execute(UpdateProductPriceCommand input);
   }
   ```

3. **Use Case Implementation** executes business logic
   ```java
   @Service
   public class UpdateProductPriceUseCase implements UpdateProductPriceInputPort {
       private final ProductRepository productRepository;  // Output Port
       private final DomainEventPublisher eventPublisher;  // Output Port

       public UpdateProductPriceResponse execute(UpdateProductPriceCommand input) {
           Product product = productRepository.findById(ProductId.of(input.productId()))
               .orElseThrow(() -> new IllegalArgumentException("Product not found"));

           product.changePrice(new Price(new Money(input.newPriceAmount(),
               Currency.getInstance(input.newPriceCurrency()))));

           productRepository.save(product);
           eventPublisher.publishAndClearEvents(product);

           return new UpdateProductPriceResponse(...);
       }
   }
   ```

4. **Secondary Port (Output Port Interface)** defines infrastructure contract
   ```java
   public interface ProductRepository extends OutputPort, Repository<Product, ProductId> {
       Product save(@NonNull Product product);
       Optional<Product> findById(@NonNull ProductId id);
   }
   ```

5. **Secondary Adapter (Repository Implementation)** persists data
   ```java
   @Repository
   public class InMemoryProductRepository implements ProductRepository {
       private final ConcurrentHashMap<ProductId, Product> products = new ConcurrentHashMap<>();

       @Override
       public Product save(Product product) {
           products.put(product.id(), product);
           return product;
       }

       @Override
       public Optional<Product> findById(ProductId id) {
           return Optional.ofNullable(products.get(id));
       }
   }
   ```

### Benefits

1. **Testability**: Core business logic can be tested without external dependencies
2. **Flexibility**: Adapters can be swapped without changing core logic
3. **Technology Independence**: Business logic doesn't depend on frameworks
4. **Clear Boundaries**: Explicit separation of concerns

---

## Onion Architecture

Onion Architecture ensures that dependencies flow inward toward the domain core, with no outward dependencies from inner layers.

### Layers

```
┌─────────────────────────────────────┐
│  Infrastructure & Adapters          │  ← Outermost layer
│  (portadapter, infrastructure)      │
├─────────────────────────────────────┤
│  Application Services                │  ← Use cases
│  (application)                       │
├─────────────────────────────────────┤
│  Domain Model                        │  ← Core business logic
│  (domain.model)                      │
└─────────────────────────────────────┘
       ↓ Dependencies flow inward
```

#### Domain Model (Core)

**Location:** `de.sample.aiarchitecture.domain.model`

**Contains:**
- Aggregates, Entities, Value Objects
- Repository interfaces
- Domain Services
- Domain Events
- Factories, Specifications

**Rules:**
- NO dependencies on outer layers
- NO framework dependencies (Spring, JPA, etc.)
- Pure business logic

#### Application Layer

**Location:** `de.sample.aiarchitecture.application`

**Contains:**
- Application Services (use cases)
- Use case orchestration

**Rules:**
- Depends ONLY on domain model
- May use sharedkernel.application.port (outbound ports)
- NO dependencies on adapters
- NO infrastructure implementation details

#### Infrastructure & Adapters

**Location:**
- `de.sample.aiarchitecture.portadapter.incoming` (REST, Web, MCP adapters)
- `de.sample.aiarchitecture.portadapter.outgoing` (Repository implementations)
- `de.sample.aiarchitecture.infrastructure` (Spring configuration)

**Contains:**
- REST Controllers and DTOs
- Repository implementations
- Framework configuration

**Rules:**
- Depends on application and domain layers
- Contains framework-specific code
- Implements ports defined in inner layers

#### Shared Kernel Application Ports

**Location:** `de.sample.aiarchitecture.sharedkernel.application.port`

The `sharedkernel.application.port` package contains **outbound ports** (interfaces) used across all bounded contexts, making it part of the Shared Kernel (Strategic DDD pattern).

**Purpose:**
- Provides port interfaces shared across multiple bounded contexts
- Enables the application layer to remain framework-independent
- Acts as "ports" in Hexagonal Architecture terminology
- Part of the Shared Kernel (shared abstractions used by all contexts)

**Pattern:**
```
Application Layer → sharedkernel.application.port (interface) ← infrastructure/adapters (implementation)
```

**Interfaces in this package:**
- `Repository<T, ID>` - Base repository interface for all aggregate repositories
- `UseCase<INPUT, OUTPUT>` - Base interface for input ports (use cases)
- `DomainEventPublisher` - Interface for publishing domain events

**Example:**
```java
// sharedkernel.application.port.DomainEventPublisher (outbound port)
public interface DomainEventPublisher {
    void publish(DomainEvent event);
    void publishAndClearEvents(AggregateRoot<?, ?> aggregate);
}

// infrastructure.config.SpringDomainEventPublisher (adapter implementation)
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher eventPublisher;
    // Spring-specific implementation...
}
```

**Rules:**
1. **sharedkernel.application.port must contain ONLY interfaces** (enforced by ArchUnit)
2. No concrete classes, no annotations, no framework dependencies
3. Implementations reside in `infrastructure.config` or adapter packages
4. Application layer may depend on `sharedkernel.application.port`, never on implementations
5. Only ports used by **multiple bounded contexts** belong here (not context-specific ports)

**Benefits:**
- Application layer remains testable (easy to mock interfaces)
- Can swap infrastructure implementations without changing application code
- Follows Dependency Inversion Principle (depend on abstractions, not concretions)
- Supports framework-independent business logic
- Shared Kernel pattern reduces duplication across bounded contexts

**What Belongs in sharedkernel.application.port:**
- ✅ Base Repository interface (used by all aggregate repositories)
- ✅ Base UseCase interface (input port marker)
- ✅ DomainEventPublisher (used by all application services)
- ✅ Other abstractions needed across multiple bounded contexts

**What Does NOT Belong:**
- ❌ Concrete classes
- ❌ Spring annotations (@Component, @Service)
- ❌ Framework-specific code
- ❌ Context-specific interfaces (those belong in the context's application layer)

---

## Layered Architecture

Traditional layered architecture with strict dependency rules.

### Layer Dependencies

```
Primary Adapters (Web)
      ↓
Application Services
      ↓
Domain Model
      ↑
Secondary Adapters (Persistence)
```

**Rules:**
1. **Domain** may not depend on any other layer
2. **Application** may depend on Domain only
3. **Primary Adapters** may depend on Application and Domain
4. **Secondary Adapters** implement interfaces from Domain
5. **Adapters may NOT communicate directly** with each other

---

## Package Structure

```
de.sample.aiarchitecture
├── application                          # Application Layer (Use Cases)
│   ├── UseCase                          # Base use case interface
│   │
│   ├── product/                         # Product Bounded Context Use Cases
│   │   ├── CreateProductUseCase
│   │   ├── CreateProductInput
│   │   ├── CreateProductOutput
│   │   ├── UpdateProductPriceUseCase
│   │   ├── UpdateProductPriceInput
│   │   ├── UpdateProductPriceOutput
│   │   ├── GetProductByIdUseCase
│   │   ├── GetProductByIdInput
│   │   ├── GetProductByIdOutput
│   │   ├── GetAllProductsUseCase
│   │   ├── GetAllProductsInput
│   │   └── GetAllProductsOutput
│   │
│   ├── cart/                            # Shopping Cart Bounded Context Use Cases
│   │   ├── CreateCartUseCase
│   │   ├── CreateCartInput
│   │   ├── CreateCartOutput
│   │   ├── AddItemToCartUseCase
│   │   ├── AddItemToCartInput
│   │   ├── AddItemToCartOutput
│   │   ├── CheckoutCartUseCase
│   │   ├── CheckoutCartInput
│   │   ├── CheckoutCartOutput
│   │   ├── GetCartByIdUseCase
│   │   ├── GetCartByIdInput
│   │   └── GetCartByIdOutput
│
├── domain                               # Domain Layer (Core Business Logic)
│   └── model
│       ├── ddd                          # DDD Marker Interfaces
│       │   ├── AggregateRoot
│       │   ├── Entity
│       │   ├── Value
│       │   ├── Repository
│       │   ├── DomainService
│       │   ├── DomainEvent
│       │   ├── Factory
│       │   └── Specification
│       │
│       ├── shared                       # Shared Kernel (Strategic DDD Pattern)
│       │   ├── Money                    # Value Object (universal)
│       │   ├── ProductId                # Value Object (cross-context identifier)
│       │   └── Price                    # Value Object (wraps Money)
│       │
│       ├── product                      # Product Bounded Context
│       │   ├── Product                  # Aggregate Root
│       │   ├── SKU                      # Value Object
│       │   ├── ProductName              # Value Object
│       │   ├── ProductRepository        # Repository Interface
│       │   └── ProductFactory           # Factory
│       │
│       └── cart                         # Shopping Cart Bounded Context
│           ├── ShoppingCart             # Aggregate Root
│           ├── CartItem                 # Entity
│           ├── CartId                   # Value Object
│           ├── Quantity                 # Value Object
│           ├── CartRepository           # Repository Interface
│           └── CartTotalCalculator      # Domain Service
│
├── infrastructure                       # Infrastructure Configuration
│   ├── api                              # Public SPI (Service Provider Interface)
│   │   └── DomainEventPublisher        # Event publisher interface (SPI)
│   └── config                           # Spring Configuration
│       ├── SecurityConfiguration
│       └── SpringDomainEventPublisher  # Event publisher implementation
│
└── portadapter                          # Adapters (Hexagonal Architecture)
    ├── incoming                         # Incoming Adapters (Primary/Driving)
    │   ├── api                          # REST API (JSON/XML)
    │   │   ├── product
    │   │   │   ├── ProductResource      # REST Controller
    │   │   │   ├── ProductDto           # DTO
    │   │   │   └── ProductDtoConverter  # Converter
    │   │   └── cart
    │   │       ├── ShoppingCartResource
    │   │       ├── ShoppingCartDto
    │   │       └── ShoppingCartDtoConverter
    │   ├── mcp                          # MCP Server (AI interface)
    │   │   └── ProductCatalogMcpTools
    │   └── web                          # Web MVC (HTML)
    │       └── product
    │           └── ProductPageController
    │
    └── outgoing                         # Outgoing Adapters (Secondary/Driven)
        ├── product
        │   ├── InMemoryProductRepository  # Repository Implementation
        │   └── SampleDataInitializer      # Sample Data
        └── cart
            └── InMemoryShoppingCartRepository  # Repository Implementation
```

---

## Bounded Contexts

### Shared Kernel

**Responsibility:** Universal domain concepts shared across contexts

**Pattern:** Shared Kernel (DDD Strategic Pattern)

**Value Objects:**
- `Money` - monetary value with currency (universal concept)
- `ProductId` - product identifier (cross-context reference)
- `Price` - wraps Money with domain-specific validation

**Design Decision:**
- **Why Shared Kernel?** Ensures consistency for universal concepts across contexts
- **Trade-off:** Creates coupling but prevents duplication and inconsistency
- **Alternative Considered:** Separate Ways (duplicate in each context) - rejected due to high risk of currency handling bugs
- **References:** Eric Evans (DDD Chapter 14), Vaughn Vernon (Implementing DDD Chapter 2)

**Rules:**
- Must remain small and carefully curated
- No dependencies on specific bounded contexts
- Only universal concepts with consistent meaning

### Product Catalog Context

**Responsibility:** Product management, pricing, inventory

**Aggregates:**
- `Product` - manages product information, pricing, and stock

**Key Value Objects:**
- `SKU` - stock keeping unit
- `ProductName` - product name
- `ProductDescription` - product description
- `ProductStock` - inventory levels
- `Category` - product category

**Dependencies:**
- Shared Kernel: `Money`, `ProductId`, `Price`

**Use Cases:**
- Create product
- Update product price
- Update product stock
- Find products by category
- Check product availability

### Shopping Cart Context

**Responsibility:** Shopping cart operations and checkout

**Aggregates:**
- `ShoppingCart` - manages cart items and checkout process

**Entities:**
- `CartItem` - item within shopping cart

**Key Value Objects:**
- `CartId` - unique cart identifier
- `CustomerId` - customer identifier
- `Quantity` - item quantity
- `CartStatus` - cart state (ACTIVE, CHECKED_OUT, ABANDONED)

**Dependencies:**
- Shared Kernel: `Money`, `ProductId`, `Price`

**Use Cases:**
- Create cart
- Add item to cart
- Update item quantity
- Remove item from cart
- Checkout cart
- Calculate cart total

**Integration with Product Context:**
- Cart references Product by `ProductId` only (from Shared Kernel)
- Does NOT access Product aggregate directly
- Complete isolation enforced by ArchUnit tests
- Uses `Price` from Shared Kernel for price snapshots

---

## Architectural Rules

### Enforced by ArchUnit Tests

All architectural rules are automatically tested and enforced using ArchUnit.

#### DDD Strategic Patterns (Bounded Contexts)

1. **Shared Kernel must be context-independent** - no dependencies on Product or Cart contexts
2. **Product Context must not access Cart Context** - enforces bounded context isolation
3. **Cart Context must not access Product Context** - cart references products by ID only
4. **Both contexts may access Shared Kernel** - for universal value objects
5. **Shared Kernel must be minimal** - only universal concepts with consistent meaning

#### DDD Tactical Patterns

1. **Aggregate Roots** must implement `AggregateRoot` interface
2. **Entities** must implement `Entity` interface
3. **Value Objects** must be immutable (records or final classes)
4. **Value Objects** must implement `Value` interface
5. **Repositories** must be interfaces in domain layer
6. **Repository implementations** must be in outgoing adapters
7. **Aggregates reference other aggregates by ID only** (Vernon's Rule #2)

#### Domain Layer Rules

1. Domain must NOT depend on infrastructure
2. Domain must NOT depend on portadapters
3. Domain must NOT use Spring annotations
4. Domain must NOT use JPA annotations
5. Domain must be framework-independent

#### Application Layer Rules

1. Application Services must end with "ApplicationService" (legacy pattern)
2. Application Services must be annotated with `@Service`
3. Application Services must NOT depend on portadapters
4. Application Services may only use `sharedkernel.application.port` (not infrastructure implementations)

#### Clean Architecture (Use Case) Rules

1. Use Case interfaces must end with "UseCase"
2. Use Case interfaces must reside in `application` package
3. Use Case Input models must end with "Input"
4. Use Case Input models must be immutable (records or final classes)
5. Use Case Input models must reside in `application` package
6. Use Case Output models must end with "Output"
7. Use Case Output models must be immutable (records or final classes)
8. Use Case Output models must reside in `application` package
9. Application layer must NOT depend on DTOs (presentation concern)
10. Input/Output models must contain only primitives, Strings, or nested records (no domain entities)

#### Hexagonal Architecture Rules

1. Primary adapters may only call application services
2. Secondary adapters implement domain repository interfaces
3. Adapters must NOT communicate directly with each other
4. Repository implementations must be in `portadapter.outgoing`

#### Onion Architecture Rules

1. Dependencies flow inward toward domain
2. Domain layer has NO outward dependencies
3. Application layer depends only on domain
4. Outer layers depend on inner layers

#### Layered Architecture Rules

1. Infrastructure may not be accessed by any layer
2. Primary adapters may not be accessed by any layer
3. Secondary adapters may not be accessed by any layer
4. Application services may only be accessed by primary adapters
5. **sharedkernel.application.port must contain only interfaces** (Shared Kernel outbound ports pattern)

#### Naming Conventions

1. Application Services must end with "ApplicationService"
2. Repository interfaces must end with "Repository"
3. Controllers must end with "Controller"
4. DTOs must end with "Dto" and reside in portadapter
5. Converters must end with "Converter" and reside in portadapter
6. Domain Services must implement `DomainService`
7. Domain Events must implement `DomainEvent`
8. Factories must implement `Factory`
9. Specifications must end with "Specification"

#### Advanced DDD Patterns

1. **Domain Events** must be immutable (final or records)
2. **Domain Events** must have timestamp field
3. **Domain Events** must be framework-independent
4. **Domain Services** must be stateless (only final fields)
5. **Domain Services** must be framework-independent
6. **Factories** must be in domain.model package
7. **Factories** must be stateless
8. **Specifications** must be framework-independent

---

## References

### Books

1. **Domain-Driven Design: Tackling Complexity in the Heart of Software** by Eric Evans
   - Original DDD book defining strategic and tactical patterns

2. **Implementing Domain-Driven Design** by Vaughn Vernon
   - Practical guide to implementing DDD patterns
   - Aggregate design rules and patterns

3. **Hexagonal Architecture** by Alistair Cockburn
   - Original description of Ports and Adapters pattern

### Key Concepts

- **Ubiquitous Language**: Shared language between developers and domain experts
- **Bounded Context**: Explicit boundary for model validity
- **Aggregate**: Cluster of objects treated as a unit
- **Repository**: Collection-like interface for aggregates
- **Domain Event**: Something that happened in the domain
- **Value Object**: Immutable descriptor without identity
- **Entity**: Object with distinct identity
- **Aggregate Root**: Entry point to aggregate

### Design Principles

- **Dependency Inversion Principle**: High-level modules should not depend on low-level modules
- **Single Responsibility Principle**: Each class should have one reason to change
- **Open/Closed Principle**: Open for extension, closed for modification
- **Separation of Concerns**: Different concerns in different modules
- **Tell, Don't Ask**: Objects should tell other objects what to do

---

## Conclusion

This architecture provides:

1. **Maintainability**: Clear separation of concerns and dependencies
2. **Testability**: Business logic can be tested without infrastructure
3. **Flexibility**: Technology decisions can be changed without affecting business logic
4. **Scalability**: Clear boundaries enable team scaling
5. **Evolvability**: Domain model can evolve with business requirements

The combination of DDD, Hexagonal Architecture, and Onion Architecture creates a robust foundation for building complex enterprise applications that remain maintainable over time.
