# AI Architecture Sample Project

A comprehensive demonstration of **Domain-Driven Design (DDD)**, **Clean Architecture**, **Hexagonal Architecture**, and **Onion Architecture** patterns using an e-commerce domain, with **MCP (Model Context Protocol)** server integration for AI assistant interaction.

## Overview

This project showcases best practices for structuring a Spring Boot application with clean architecture principles. It implements three bounded contexts:
- **Product Catalog** - Product management with pricing and inventory
- **Shopping Cart** - Customer shopping cart with checkout functionality
- **Portal** - Application home page and navigation

### Key Features

- **AI-Accessible Product Catalog** via MCP server (Spring AI 1.1.0-M3)
- **Complete Architecture Testing** with ArchUnit (10 test suites)
- **16 Architecture Decision Records** documenting design choices
- **Shared Kernel** pattern for cross-context value objects
- **Framework-Independent Domain** layer (no Spring/JPA in core)

## Architecture Patterns

### Domain-Driven Design (DDD)

**Strategic Patterns:**
- **Bounded Contexts**: Product Catalog, Shopping Cart
- **Shared Kernel**: Cross-context value objects (Money, Price, ProductId)
- **Context Mapping**: Contexts communicate via shared kernel

**Tactical Patterns:**
- **Aggregates**: Product, ShoppingCart
- **Entities**: CartItem
- **Value Objects**: ProductId, SKU, Price, Money, Quantity, Category, etc.
- **Repositories**: Interfaces in application layer, implementations in adapters
- **Domain Services**: PricingService, CartTotalCalculator
- **Domain Events**: ProductCreated, ProductPriceChanged, CartCheckedOut, CartItemAddedToCart
- **Factories**: ProductFactory
- **Specifications**: ProductAvailabilitySpecification

### Clean Architecture

- **Use Cases** (Input Ports): Explicit use case interfaces with single responsibility
- **Input/Output Models**: Commands, Queries, and Response objects decouple layers
- **Framework Independence**: Domain and application layers are framework-agnostic
- **Dependency Rule**: Dependencies point inward (Infrastructure → Adapters → Application → Domain)
- **Use Case Organization**: One use case per operation (CreateProductUseCase, AddItemToCartUseCase, etc.)

### Hexagonal Architecture (Ports and Adapters)

- **Input Ports**: Use case interfaces (defined in application layer)
- **Output Ports**: Repository and service interfaces (defined in sharedkernel.application.port)
- **Incoming Adapters** (Primary): REST Controllers, MCP Server, Web MVC
- **Outgoing Adapters** (Secondary): In-memory repository implementations

### Onion Architecture

Layers (from innermost to outermost):
1. **Domain Model** (per bounded context) - Pure business logic, framework-independent
2. **Application Services** (per bounded context) - Use case orchestration
3. **Adapters** (per bounded context) - External interfaces
4. **Shared Kernel** - Cross-context shared concepts
5. **Infrastructure** - Cross-cutting concerns

## Project Structure

```
src/main/java/de/sample/aiarchitecture/
├── sharedkernel/                         # Shared Kernel (cross-context)
│   ├── marker/                           # Architectural markers
│   │   ├── tactical/                     # DDD tactical patterns
│   │   │   ├── Id.java                   # Identity marker
│   │   │   ├── Entity.java
│   │   │   ├── Value.java
│   │   │   ├── AggregateRoot.java
│   │   │   ├── BaseAggregateRoot.java
│   │   │   ├── DomainEvent.java
│   │   │   ├── IntegrationEvent.java
│   │   │   ├── DomainService.java
│   │   │   ├── Factory.java
│   │   │   └── Specification.java
│   │   ├── strategic/                    # DDD strategic patterns
│   │   │   ├── BoundedContext.java
│   │   │   ├── SharedKernel.java
│   │   │   └── OpenHostService.java
│   │   └── port/                         # Hexagonal Architecture ports
│   │       ├── in/                       # Input ports (driving)
│   │       │   ├── InputPort.java
│   │       │   └── UseCase.java
│   │       └── out/                      # Output ports (driven)
│   │           ├── OutputPort.java
│   │           ├── Repository.java
│   │           ├── DomainEventPublisher.java
│   │           └── IdentityProvider.java
│   └── domain/
│       ├── model/                        # Shared value objects
│       │   ├── ProductId.java            # Cross-context ID
│       │   ├── UserId.java               # Cross-context ID
│       │   ├── Money.java                # Cross-context value
│       │   └── Price.java                # Cross-context value
│       └── specification/                # Composable specification pattern
│           ├── CompositeSpecification.java
│           ├── AndSpecification.java
│           ├── OrSpecification.java
│           ├── NotSpecification.java
│           └── SpecificationVisitor.java
│
├── product/                              # Product Catalog bounded context
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── Product.java              # Aggregate Root
│   │   │   ├── SKU.java                  # Value Objects
│   │   │   ├── ProductName.java
│   │   │   ├── ProductDescription.java
│   │   │   ├── ProductStock.java
│   │   │   ├── Category.java
│   │   │   └── ProductFactory.java       # Factory
│   │   ├── specification/                # Specifications
│   │   │   └── ProductAvailabilitySpecification.java
│   │   ├── service/                      # Domain services
│   │   │   └── PricingService.java
│   │   └── event/                        # Domain events
│   │       ├── ProductCreated.java
│   │       └── ProductPriceChanged.java
│   ├── application/                      # Application layer
│   │   ├── createproduct/                # Use case: Create Product
│   │   │   ├── CreateProductInputPort.java       # Input port interface
│   │   │   ├── CreateProductUseCase.java         # Use case implementation
│   │   │   ├── CreateProductCommand.java
│   │   │   └── CreateProductResponse.java
│   │   ├── getallproducts/               # Use case: Get All Products
│   │   │   ├── GetAllProductsInputPort.java
│   │   │   ├── GetAllProductsUseCase.java
│   │   │   ├── GetAllProductsQuery.java
│   │   │   └── GetAllProductsResponse.java
│   │   ├── getproductbyid/               # Use case: Get Product By ID
│   │   │   ├── GetProductByIdInputPort.java
│   │   │   ├── GetProductByIdUseCase.java
│   │   │   ├── GetProductByIdQuery.java
│   │   │   └── GetProductByIdResponse.java
│   │   ├── updateproductprice/           # Use case: Update Product Price
│   │   │   ├── UpdateProductPriceInputPort.java
│   │   │   ├── UpdateProductPriceUseCase.java
│   │   │   ├── UpdateProductPriceCommand.java
│   │   │   └── UpdateProductPriceResponse.java
│   │   ├── reduceproductstock/           # Use case: Reduce Product Stock
│   │   │   ├── ReduceProductStockInputPort.java
│   │   │   ├── ReduceProductStockUseCase.java
│   │   │   ├── ReduceProductStockCommand.java
│   │   │   └── ReduceProductStockResponse.java
│   │   └── shared/                       # Shared output ports
│   │       └── ProductRepository.java    # Repository interface
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters (primary)
│       │   ├── api/
│       │   │   ├── ProductResource.java  # REST API
│       │   │   ├── CreateProductRequest.java
│       │   │   └── ProductDto.java
│       │   ├── mcp/
│       │   │   └── ProductCatalogMcpToolProvider.java
│       │   ├── web/
│       │   │   └── ProductPageController.java
│       │   └── event/
│       │       └── ProductEventConsumer.java
│       └── outgoing/                     # Outgoing adapters (secondary)
│           └── persistence/
│               ├── InMemoryProductRepository.java
│               └── SampleDataInitializer.java
│
├── cart/                                 # Shopping Cart bounded context
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── ShoppingCart.java         # Aggregate Root
│   │   │   ├── CartItem.java             # Entity
│   │   │   ├── CartId.java               # Value Objects
│   │   │   ├── CartItemId.java
│   │   │   ├── CustomerId.java
│   │   │   ├── Quantity.java
│   │   │   └── CartStatus.java
│   │   ├── service/                      # Domain services
│   │   │   └── CartTotalCalculator.java
│   │   └── event/                        # Domain events
│   │       ├── CartCheckedOut.java
│   │       └── CartItemAddedToCart.java
│   ├── application/                      # Application layer
│   │   ├── createcart/                   # Use case: Create Cart
│   │   │   ├── CreateCartInputPort.java
│   │   │   ├── CreateCartUseCase.java
│   │   │   ├── CreateCartCommand.java
│   │   │   └── CreateCartResponse.java
│   │   ├── additemtocart/                # Use case: Add Item to Cart
│   │   │   ├── AddItemToCartInputPort.java
│   │   │   ├── AddItemToCartUseCase.java
│   │   │   ├── AddItemToCartCommand.java
│   │   │   └── AddItemToCartResponse.java
│   │   ├── checkoutcart/                 # Use case: Checkout Cart
│   │   │   ├── CheckoutCartInputPort.java
│   │   │   ├── CheckoutCartUseCase.java
│   │   │   ├── CheckoutCartCommand.java
│   │   │   └── CheckoutCartResponse.java
│   │   ├── getallcarts/                  # Use case: Get All Carts
│   │   │   ├── GetAllCartsInputPort.java
│   │   │   ├── GetAllCartsUseCase.java
│   │   │   ├── GetAllCartsQuery.java
│   │   │   └── GetAllCartsResponse.java
│   │   ├── getcartbyid/                  # Use case: Get Cart By ID
│   │   │   ├── GetCartByIdInputPort.java
│   │   │   ├── GetCartByIdUseCase.java
│   │   │   ├── GetCartByIdQuery.java
│   │   │   └── GetCartByIdResponse.java
│   │   ├── getorcreateactivecart/        # Use case: Get or Create Active Cart
│   │   │   ├── GetOrCreateActiveCartInputPort.java
│   │   │   ├── GetOrCreateActiveCartUseCase.java
│   │   │   ├── GetOrCreateActiveCartCommand.java
│   │   │   └── GetOrCreateActiveCartResponse.java
│   │   ├── removeitemfromcart/           # Use case: Remove Item from Cart
│   │   │   ├── RemoveItemFromCartInputPort.java
│   │   │   ├── RemoveItemFromCartUseCase.java
│   │   │   ├── RemoveItemFromCartCommand.java
│   │   │   └── RemoveItemFromCartResponse.java
│   │   └── shared/                       # Shared output ports
│   │       └── ShoppingCartRepository.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   ├── api/
│       │   │   ├── ShoppingCartResource.java
│       │   │   ├── AddToCartRequest.java
│       │   │   ├── ShoppingCartDto.java
│       │   │   └── ShoppingCartDtoConverter.java
│       │   └── event/
│       │       └── CartEventConsumer.java
│       └── outgoing/                     # Outgoing adapters
│           └── persistence/
│               └── InMemoryShoppingCartRepository.java
│
├── portal/                               # Portal bounded context
│   └── adapter/                          # Adapters
│       └── incoming/                     # Incoming adapters
│           └── web/
│               └── HomePageController.java
│
└── infrastructure/                       # Infrastructure (cross-cutting)
    └── config/                           # Spring configuration
        ├── SecurityConfiguration.java
        ├── TransactionConfiguration.java
        ├── SpringDomainEventPublisher.java
        ├── AsyncConfiguration.java
        └── AsyncInitializationProcessor.java
```

## Getting Started

### Prerequisites
- Java 21
- Gradle 9.1 or higher

### Running the Application

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

## API Documentation

### Product API

#### Get All Products
```bash
curl http://localhost:8080/api/products
```

#### Get Product by ID
```bash
curl http://localhost:8080/api/products/{productId}
```

#### Get Product by SKU
```bash
curl http://localhost:8080/api/products/sku/LAPTOP-001
```

#### Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "TEST-001",
    "name": "Test Product",
    "description": "A test product",
    "price": 99.99,
    "category": "Electronics",
    "stock": 10
  }'
```

#### Delete Product
```bash
curl -X DELETE http://localhost:8080/api/products/{productId}
```

### Shopping Cart API

#### Create Cart
```bash
curl -X POST "http://localhost:8080/api/carts?customerId=customer-123"
```

#### Get Active Cart for Customer
```bash
curl http://localhost:8080/api/carts/customer/customer-123/active
```

#### Get Cart by ID
```bash
curl http://localhost:8080/api/carts/{cartId}
```

#### Add Item to Cart
```bash
curl -X POST http://localhost:8080/api/carts/{cartId}/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "{productId}",
    "quantity": 2
  }'
```

#### Remove Item from Cart
```bash
curl -X DELETE http://localhost:8080/api/carts/{cartId}/items/{itemId}
```

#### Checkout Cart
```bash
curl -X POST http://localhost:8080/api/carts/{cartId}/checkout
```

#### Delete Cart
```bash
curl -X DELETE http://localhost:8080/api/carts/{cartId}
```

### MCP (Model Context Protocol) API

The product catalog is accessible via MCP server for AI assistants like Claude. The server runs on `http://localhost:8080/mcp` using **streamable HTTP** (HTTP + Server-Sent Events).

#### Available MCP Tools

**all-products**
- Returns all products in the catalog with complete details
- No parameters required

**product-by-sku**
- Find product by SKU (e.g., "LAPTOP-001")
- Parameters: `sku` (String) - must contain uppercase letters, numbers, hyphens only

**product-by-category**
- Find all products in a category
- Parameters: `categoryName` (String)
- Valid categories: Electronics, Clothing, Books, Home & Garden, Sports & Outdoors

**product-by-id**
- Get product by internal UUID
- Parameters: `id` (String) - UUID format

#### Connecting AI Assistants

Create `.mcp.json` in the project root:

```json
{
  "mcpServers": {
    "product-catalog": {
      "type": "http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

AI assistants will automatically discover and connect to the MCP server.

**See:** [docs/integrations/mcp-server-integration.md](docs/integrations/mcp-server-integration.md) for detailed MCP server documentation.

## Sample Data

The application initializes with 11 sample products across different categories:
- **Electronics**: Laptop, Smartphone, Tablet
- **Clothing**: T-Shirt, Jeans
- **Books**: DDD Book, Clean Architecture Book
- **Home & Garden**: Office Chair, Standing Desk
- **Sports**: Yoga Mat, Dumbbells

## Architecture Documentation

For comprehensive architecture documentation, see:
- **[docs/architecture/](docs/architecture/)** - Complete architecture documentation
- **[docs/architecture/architecture-principles.md](docs/architecture/architecture-principles.md)** - Detailed patterns and principles
- **[CLAUDE.md](CLAUDE.md)** - Development guidelines and best practices

### Quick Reference

**Domain Layer** (per bounded context) - Framework-independent business logic
- No Spring/JPA annotations in domain models
- All business rules in domain objects
- Organized into: domain.model, domain.service, domain.event
- Dependencies point inward toward domain

**Application Layer** (per bounded context) - Use case orchestration
- Thin coordination layer (no business logic)
- Manages transactions and domain event publishing
- Defines ports: Input ports (use cases) and output ports (repositories)
- One use case class per operation

**Adapter Layer** (per bounded context) - External interfaces
- Incoming Adapters (Primary):
  - `adapter.incoming.api` - REST APIs (@RestController) returning JSON
  - `adapter.incoming.web` - Web MVC (@Controller) returning HTML
  - `adapter.incoming.mcp` - MCP Server for AI assistants
- Outgoing Adapters (Secondary):
  - `adapter.outgoing.persistence` - Repository implementations
- DTO conversion happens here
- Adapters don't communicate directly

**Shared Kernel** - Cross-context shared concepts
- `sharedkernel.marker.tactical` - DDD tactical patterns (Entity, Value, AggregateRoot, DomainEvent, etc.)
- `sharedkernel.marker.strategic` - DDD strategic patterns (BoundedContext, SharedKernel)
- `sharedkernel.marker.port.in` - Input ports (UseCase, InputPort)
- `sharedkernel.marker.port.out` - Output ports (Repository, DomainEventPublisher, IdentityProvider)
- `sharedkernel.domain.model` - Shared value objects (Money, Price, ProductId, UserId)
- `sharedkernel.domain.specification` - Composable specification pattern

**Infrastructure Layer** - Cross-cutting concerns
- `infrastructure.config` - Spring configuration and framework integrations

## Testing

### Architecture Tests

Architecture rules are **actively enforced** using ArchUnit with 10 comprehensive test suites:

```bash
./gradlew test-architecture
```

**Test Suites:**
- **DddTacticalPatternsArchUnitTest** - Aggregate, Entity, Value Object, Repository patterns
- **DddAdvancedPatternsArchUnitTest** - Domain Events, Services, Factories, Specifications
- **DddStrategicPatternsArchUnitTest** - Bounded Context isolation
- **HexagonalArchitectureArchUnitTest** - Ports and Adapters rules
- **OnionArchitectureArchUnitTest** - Dependency flow rules
- **LayeredArchitectureArchUnitTest** - Layer access rules
- **NamingConventionsArchUnitTest** - Naming standards
- **PackageCyclesArchUnitTest** - Circular dependency detection
- **UseCasePatternsArchUnitTest** - Application service patterns
- **BaseArchUnitTest** - Common test infrastructure

These tests verify:
- Domain layer has no framework dependencies
- Aggregates only reference other aggregates by ID
- Value objects are immutable records
- Repository interfaces live in domain
- No circular package dependencies
- Naming conventions are followed
- Bounded contexts are properly isolated

### Unit Tests
```bash
./gradlew test
```

## Key Design Decisions

1. **In-Memory Storage**: Uses ConcurrentHashMap for simplicity; production would use JPA/database
2. **Security**: Permits all requests for demo purposes
3. **Price Snapshot**: Cart items store price at time of addition (common e-commerce pattern)
4. **Package-Private Constructors**: CartItem can only be created through ShoppingCart
5. **Immutable Value Objects**: All value objects are Java records
6. **MCP as Primary Adapter**: MCP server implemented as incoming adapter, maintaining clean architecture
7. **Read-Only MCP Tools**: AI access limited to queries for safety
8. **Shared Kernel**: Cross-context value objects shared between bounded contexts

**Architecture Decision Records:** See [docs/architecture/adr/README.md](docs/architecture/adr/README.md) for 16 documented architectural decisions.

## Business Rules Demonstrated

### Product Aggregate
- Price must be positive
- Stock cannot be negative
- SKU must be unique

### Shopping Cart Aggregate
- Cannot modify checked-out cart
- Cannot checkout empty cart
- Each product appears once (quantities combined)
- Cart stores price snapshot at time of addition

## Further Reading

### Project Documentation
- **[Architecture Documentation](docs/architecture/)** - Complete architecture guide
- **[Architecture Principles](docs/architecture/architecture-principles.md)** - Detailed DDD, Hexagonal, and Onion patterns
- **[CLAUDE.md](CLAUDE.md)** - Development guidelines and documentation standards

### External Resources
- **[Domain-Driven Design](https://www.domainlanguage.com/ddd/)** by Eric Evans
- **[Implementing Domain-Driven Design](https://vaughnvernon.com/)** by Vaughn Vernon
- **[Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)** by Alistair Cockburn
- **[Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)** by Robert C. Martin
- **[ArchUnit](https://www.archunit.org/)** - Architecture testing framework

## License

This is a sample project for educational purposes.
