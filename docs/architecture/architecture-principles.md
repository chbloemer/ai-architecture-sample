# Architecture Principles

This document describes the architectural patterns and principles used in the AI Architecture Sample Project.

## Table of Contents

1. [Overview](#overview)
2. [Domain-Driven Design (DDD)](#domain-driven-design-ddd)
3. [Hexagonal Architecture](#hexagonal-architecture)
4. [Onion Architecture](#onion-architecture)
5. [Layered Architecture](#layered-architecture)
6. [Package Structure](#package-structure)
7. [Bounded Contexts](#bounded-contexts)
8. [Architectural Rules](#architectural-rules)

---

## Overview

This sample project demonstrates a modern, maintainable architecture for enterprise applications using three complementary architectural patterns:

- **Domain-Driven Design (DDD)**: Focus on the core domain and domain logic
- **Hexagonal Architecture**: Separation of business logic from technical concerns
- **Onion Architecture**: Dependencies flow inward toward the domain core

These patterns work together to create a flexible, testable, and maintainable codebase that can evolve with changing business requirements.

---

## Domain-Driven Design (DDD)

Domain-Driven Design is an approach to software development that centers the development on programming a domain model that has a rich understanding of the processes and rules of the domain.

### Strategic Patterns

#### Bounded Contexts

A bounded context is an explicit boundary within which a domain model is defined and applicable. Our e-commerce application has two bounded contexts plus a shared kernel:

1. **Shared Kernel** (`de.sample.aiarchitecture.domain.model.shared`)
   - Small, carefully curated domain model shared across contexts
   - Value Objects: `Money`, `ProductId`, `Price`
   - **Pattern**: Shared Kernel (Eric Evans, DDD Chapter 14)
   - **Trade-off**: Creates coupling but ensures consistency for universal concepts

2. **Product Catalog Context** (`de.sample.aiarchitecture.domain.model.product`)
   - Manages products, pricing, inventory
   - Aggregate Root: `Product`
   - Value Objects: `SKU`, `ProductName`, `ProductDescription`, `ProductStock`, `Category`
   - Depends on: Shared Kernel

3. **Shopping Cart Context** (`de.sample.aiarchitecture.domain.model.cart`)
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

**Rules:**
1. External objects can only reference the aggregate root (never entities within)
2. Aggregate boundaries ensure invariants are maintained
3. Aggregates reference other aggregates by identity only

**Implementation:** See `de.sample.aiarchitecture.domain.model.ddd.AggregateRoot`

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

**Implementation:** See `de.sample.aiarchitecture.domain.model.ddd.Entity`

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
- Interface: `de.sample.aiarchitecture.domain.model.ddd.Value`
- Shared Value Objects: `de.sample.aiarchitecture.domain.model.shared.Money`, `ProductId`, `Price`
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
- Base Interface: `de.sample.aiarchitecture.domain.model.ddd.Repository`
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

**Implementation:** See `de.sample.aiarchitecture.domain.model.ddd.DomainService`

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
- Interface: `de.sample.aiarchitecture.domain.model.ddd.DomainEvent`
- Publisher: `de.sample.aiarchitecture.infrastructure.api.DomainEventPublisher`
- Examples: `ProductCreated`, `ProductPriceChanged`, `CartItemAddedToCart`, `CartCheckedOut`

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

**Implementation:** See `de.sample.aiarchitecture.domain.model.ddd.Factory`

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

**Implementation:** See `de.sample.aiarchitecture.domain.model.ddd.Specification`

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
   public class ProductController {
       private final ProductApplicationService productService;

       @PostMapping
       public ResponseEntity<ProductDto> createProduct(@RequestBody CreateProductRequest request) {
           Product product = productService.createProduct(...);
           return ResponseEntity.ok(ProductConverter.toDto(product));
       }
   }
   ```

2. **Primary Port (Application Service)** executes use case
   ```java
   @Service
   public class ProductApplicationService {
       private final ProductRepository productRepository;
       private final ProductFactory productFactory;

       public Product createProduct(SKU sku, ProductName name, ...) {
           Product product = productFactory.createProduct(sku, name, ...);
           return productRepository.save(product);
       }
   }
   ```

3. **Secondary Port (Repository Interface)** defines contract
   ```java
   public interface ProductRepository extends Repository<Product, ProductId> {
       Product save(@NonNull Product product);
   }
   ```

4. **Secondary Adapter (Repository Implementation)** persists data
   ```java
   @Repository
   public class InMemoryProductRepository implements ProductRepository {
       private final ConcurrentHashMap<ProductId, Product> products = new ConcurrentHashMap<>();

       @Override
       public Product save(Product product) {
           products.put(product.id(), product);
           return product;
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
- May use infrastructure.api (SPI)
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
├── application                          # Application Services (Use Cases)
│   ├── CartApplicationService
│   └── ProductApplicationService
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
│   ├── api                              # Public SPI
│   │   └── DomainEventPublisher
│   └── config                           # Spring Configuration
│       └── SecurityConfiguration
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

1. Application Services must end with "ApplicationService"
2. Application Services must be annotated with `@Service`
3. Application Services must NOT depend on portadapters
4. Application Services may only use `infrastructure.api` (not implementation)

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
