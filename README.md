# AI Architecture Sample Project

A comprehensive demonstration of **Domain-Driven Design (DDD)**, **Hexagonal Architecture**, and **Onion Architecture** patterns using an e-commerce domain, with **MCP (Model Context Protocol)** server integration for AI assistant interaction.

## Overview

This project showcases best practices for structuring a Spring Boot application with clean architecture principles. It implements two bounded contexts:
- **Product Catalog** - Product management with pricing and inventory
- **Shopping Cart** - Customer shopping cart with checkout functionality

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
- **Repositories**: Defined in domain, implemented in adapters
- **Domain Services**: PricingService, CartTotalCalculator
- **Domain Events**: ProductCreated, ProductPriceChanged, CartCheckedOut, CartItemAddedToCart
- **Factories**: ProductFactory
- **Specifications**: ProductAvailabilitySpecification

### Hexagonal Architecture (Ports and Adapters)
- **Primary Ports**: Application Services (use cases)
- **Primary Adapters**: REST Controllers, MCP Server
- **Secondary Ports**: Repository interfaces (defined in domain)
- **Secondary Adapters**: In-memory repository implementations

### Onion Architecture
Layers (from innermost to outermost):
1. **Domain Model** (`domain.model.*`) - Pure business logic, framework-independent
2. **Application Services** (`application.*`) - Use case orchestration
3. **Adapters** (`portadapter.*`) - External interfaces
4. **Infrastructure** (`infrastructure.*`) - Cross-cutting concerns

## Project Structure

```
src/main/java/de/sample/aiarchitecture/
├── domain/
│   └── model/
│       ├── ddd/                          # DDD marker interfaces
│       │   ├── AggregateRoot.java
│       │   ├── Entity.java
│       │   ├── Value.java
│       │   ├── Repository.java
│       │   ├── DomainService.java
│       │   ├── Factory.java
│       │   ├── Specification.java
│       │   └── DomainEvent.java
│       ├── shared/                       # Shared Kernel
│       │   ├── ProductId.java            # Cross-context ID
│       │   ├── Money.java                # Cross-context value
│       │   └── Price.java                # Cross-context value
│       ├── product/                      # Product Catalog bounded context
│       │   ├── Product.java              # Aggregate Root
│       │   ├── SKU.java, ProductName.java # Value Objects
│       │   ├── ProductRepository.java    # Repository interface
│       │   ├── ProductFactory.java       # Factory
│       │   ├── PricingService.java       # Domain Service
│       │   ├── ProductAvailabilitySpecification.java
│       │   └── ProductCreated.java       # Domain Event
│       └── cart/                         # Shopping Cart bounded context
│           ├── ShoppingCart.java         # Aggregate Root
│           ├── CartItem.java             # Entity
│           ├── CartId.java, Quantity.java # Value Objects
│           ├── ShoppingCartRepository.java # Repository interface
│           ├── CartTotalCalculator.java  # Domain Service
│           └── CartCheckedOut.java       # Domain Event
├── application/
│   ├── ProductApplicationService.java
│   └── ShoppingCartApplicationService.java
├── portadapter/
│   ├── incoming/                         # Incoming Adapters (Primary/Driving)
│   │   ├── api/                          # REST API (JSON/XML)
│   │   │   ├── product/
│   │   │   │   ├── ProductResource.java
│   │   │   │   ├── ProductDto.java
│   │   │   │   └── ProductDtoConverter.java
│   │   │   └── cart/
│   │   │       ├── ShoppingCartResource.java
│   │   │       ├── ShoppingCartDto.java
│   │   │       └── ShoppingCartDtoConverter.java
│   │   ├── mcp/                          # MCP Server (AI interface)
│   │   │   └── ProductCatalogMcpTools.java
│   │   └── web/                          # Web MVC (HTML)
│   │       ├── product/
│   │       │   └── ProductPageController.java
│   │       └── cart/
│   └── outgoing/                         # Outgoing Adapters (Secondary/Driven)
│       ├── product/
│       │   ├── InMemoryProductRepository.java
│       │   └── SampleDataInitializer.java
│       └── cart/
│           └── InMemoryShoppingCartRepository.java
└── infrastructure/
    ├── api/                              # Public SPI
    │   └── AsyncInitialize.java
    └── config/
        ├── SecurityConfiguration.java
        └── DomainConfiguration.java
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

**Domain Layer** - Framework-independent business logic
- No Spring/JPA annotations in domain models
- All business rules in domain objects
- Repository interfaces defined here, implemented in adapters
- Dependencies point inward toward domain

**Application Layer** - Use case orchestration
- Thin coordination layer (no business logic)
- Manages transactions and domain event publishing
- One service method per use case

**Adapter Layer** - External interfaces
- Primary Adapters (incoming):
  - `api.*` - REST APIs (@RestController) returning JSON
  - `web.*` - Web MVC (@Controller) returning HTML
  - `mcp.*` - MCP Server for AI assistants
- Secondary Adapters (outgoing): Repository implementations
- DTO conversion happens here
- Adapters don't communicate directly

**Infrastructure Layer** - Cross-cutting concerns
- `infrastructure.api.*` - Public SPI for application layer
- `infrastructure.config.*` - Spring configuration
- `infrastructure.event.*` - Event listeners

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
