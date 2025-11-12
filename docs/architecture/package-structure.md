# Package Structure

Package organization of the ai-architecture project.

## Full Structure

```
de.sample.aiarchitecture
│
├── sharedkernel                    # Shared Kernel (cross-context)
│   ├── domain
│   │   ├── marker                 # DDD Marker Interfaces
│   │   │   ├── AggregateRoot, Entity, Value
│   │   │   ├── Repository, DomainService
│   │   │   └── Factory, Specification, DomainEvent
│   │   └── common                 # Shared Value Objects
│   │       ├── Money, Price, ProductId
│   └── application
│       └── marker                 # Use Case Patterns
│           ├── InputPort
│           └── OutputPort
│
├── product                         # Product Bounded Context
│   ├── domain
│   │   ├── model                  # Domain Model
│   │   │   ├── Product (Aggregate Root)
│   │   │   ├── SKU, ProductName, ProductDescription
│   │   │   ├── ProductStock, Category (Value Objects)
│   │   │   ├── ProductFactory
│   │   │   └── ProductAvailabilitySpecification
│   │   ├── service                # Domain Services
│   │   │   └── PricingService
│   │   └── event                  # Domain Events
│   │       ├── ProductCreated
│   │       └── ProductPriceChanged
│   ├── application                # Application Layer
│   │   ├── port
│   │   │   ├── in                 # Input Ports (primary ports - use cases)
│   │   │   │   ├── CreateProductInputPort
│   │   │   │   ├── GetAllProductsInputPort
│   │   │   │   ├── GetProductByIdInputPort
│   │   │   │   ├── ReduceProductStockInputPort
│   │   │   │   └── UpdateProductPriceInputPort
│   │   │   └── out                # Output Ports (secondary ports)
│   │   │       └── ProductRepository (interface)
│   │   └── usecase                # Use Case Implementations
│   │       ├── createproduct/
│   │       │   ├── CreateProductUseCase (implements CreateProductInputPort)
│   │       │   ├── CreateProductCommand
│   │       │   └── CreateProductResponse
│   │       ├── getallproducts/
│   │       ├── getproductbyid/
│   │       ├── reduceproductstock/
│   │       └── updateproductprice/
│   └── adapter                    # Adapters
│       ├── incoming               # Incoming Adapters (Primary)
│       │   ├── api/
│       │   │   ├── ProductResource
│       │   │   ├── ProductDto
│       │   │   └── CreateProductRequest
│       │   ├── mcp/
│       │   │   └── ProductCatalogMcpTools
│       │   ├── web/
│       │   │   └── ProductPageController
│       │   └── event/
│       │       └── ProductEventListener
│       └── outgoing               # Outgoing Adapters (Secondary)
│           └── persistence/
│               ├── InMemoryProductRepository
│               └── SampleDataInitializer
│
├── cart                            # Cart Bounded Context
│   ├── domain
│   │   ├── model                  # Domain Model
│   │   │   ├── ShoppingCart (Aggregate Root)
│   │   │   ├── CartItem (Entity)
│   │   │   ├── CartId, CartItemId (Value Objects)
│   │   │   ├── CustomerId, Quantity
│   │   │   └── CartStatus
│   │   ├── service                # Domain Services
│   │   │   └── CartTotalCalculator
│   │   └── event                  # Domain Events
│   │       ├── CartCheckedOut
│   │       └── CartItemAddedToCart
│   ├── application                # Application Layer
│   │   ├── port
│   │   │   ├── in                 # Input Ports (primary ports - use cases)
│   │   │   │   ├── AddItemToCartInputPort
│   │   │   │   ├── CheckoutCartInputPort
│   │   │   │   ├── CreateCartInputPort
│   │   │   │   ├── GetAllCartsInputPort
│   │   │   │   ├── GetCartByIdInputPort
│   │   │   │   ├── GetOrCreateActiveCartInputPort
│   │   │   │   └── RemoveItemFromCartInputPort
│   │   │   └── out                # Output Ports
│   │   │       └── ShoppingCartRepository (interface)
│   │   └── usecase                # Use Case Implementations
│   │       ├── createcart/
│   │       │   ├── CreateCartUseCase (implements CreateCartInputPort)
│   │       │   ├── CreateCartCommand
│   │       │   └── CreateCartResponse
│   │       ├── additemtocart/
│   │       ├── checkoutcart/
│   │       ├── getallcarts/
│   │       ├── getcartbyid/
│   │       ├── getorcreateactivecart/
│   │       └── removeitemfromcart/
│   └── adapter                    # Adapters
│       ├── incoming               # Incoming Adapters
│       │   ├── api/
│       │   │   ├── ShoppingCartResource
│       │   │   ├── ShoppingCartDto
│       │   │   ├── AddToCartRequest
│       │   │   └── ShoppingCartDtoConverter
│       │   └── event/
│       │       └── CartEventListener
│       └── outgoing               # Outgoing Adapters
│           └── persistence/
│               └── InMemoryShoppingCartRepository
│
├── portal                          # Portal Bounded Context
│   └── adapter                    # Adapters
│       └── incoming               # Incoming Adapters (Primary)
│           └── web/
│               └── HomePageController
│
└── infrastructure                  # Infrastructure (cross-cutting)
    ├── api                        # Public SPI (interfaces only)
    │   └── DomainEventPublisher
    └── config                     # Spring Configuration
        ├── SecurityConfiguration
        ├── TransactionConfiguration
        └── SpringDomainEventPublisher
```

## Bounded Context Organization

Each bounded context (product, cart) follows the same internal structure:

```
{context}/
├── domain/             # Domain layer (innermost)
│   ├── model/          # Aggregates, entities, value objects
│   ├── service/        # Domain services
│   └── event/          # Domain events
├── application/        # Application layer
│   ├── port/           # Ports (interfaces)
│   │   └── out/        # Output ports (repositories, external services)
│   └── usecase/        # Use cases (input ports)
└── adapter/            # Adapter layer (outermost)
    ├── incoming/       # Incoming adapters (primary/driving)
    │   ├── api/        # REST API
    │   ├── web/        # Web MVC
    │   ├── mcp/        # MCP server
    │   └── event/      # Domain event listeners
    └── outgoing/       # Outgoing adapters (secondary/driven)
        └── persistence/ # Repository implementations
```

## Primary Adapters: API vs Web vs MCP vs Event

| Aspect | API (`adapter.incoming.api`) | Web (`adapter.incoming.web`) | MCP (`adapter.incoming.mcp`) | Event (`adapter.incoming.event`) |
|--------|-------------|--------------|--------------|--------------|
| **Annotation** | `@RestController` | `@Controller` | `@Component` | `@Component` |
| **Naming** | `*Resource` | `*Controller` | `*McpTools` | `*EventListener` |
| **Returns** | JSON/XML (DTOs) | HTML (templates) | DTOs (JSON-RPC) | void |
| **URL** | `/api/*` | `/*` | `/mcp` | N/A |
| **Content-Type** | `application/json` | `text/html` | `application/json` | N/A |
| **Consumers** | Apps, systems | Browsers (humans) | AI assistants | Domain events |
| **Data Model** | DTOs | Domain or ViewModels | DTOs | Domain events |

## ArchUnit Enforcement

Naming conventions and architectural rules are enforced by ArchUnit tests:

```groovy
def "REST Controllers must end with 'Resource'"() {
  classes()
    .that().resideInAPackage("..adapter.incoming.api..")
    .and().areAnnotatedWith(RestController.class)
    .should().haveSimpleNameEndingWith("Resource")
    .check(allClasses)
}

def "MVC Controllers must end with 'Controller'"() {
  classes()
    .that().resideInAPackage("..adapter.incoming.web..")
    .and().areAnnotatedWith(Controller.class)
    .should().haveSimpleNameEndingWith("Controller")
    .check(allClasses)
}

def "Repository Interfaces must reside in application output port package"() {
  classes()
    .that().implement(Repository.class)
    .and().areInterfaces()
    .should().resideInAnyPackage(
      "..product.application.port.out..",
      "..cart.application.port.out.."
    )
    .check(allClasses)
}

def "Repository Implementations must reside in adapter.outgoing package"() {
  classes()
    .that().implement(Repository.class)
    .and().areNotInterfaces()
    .should().resideInAnyPackage(
      "..product.adapter.outgoing..",
      "..cart.adapter.outgoing.."
    )
    .check(allClasses)
}
```

**Verify:**
```bash
./gradlew test-architecture
```

## File Location Quick Reference

**Where to put a new...**

| File Type | Location | Example |
|-----------|----------|---------|
| REST controller | `{context}.adapter.incoming.api/` | `product.adapter.incoming.api/ProductResource.java` |
| MVC controller | `{context}.adapter.incoming.web/` | `product.adapter.incoming.web/ProductPageController.java` |
| MCP tool | `{context}.adapter.incoming.mcp/` | `product.adapter.incoming.mcp/ProductCatalogMcpTools.java` |
| DTO | `{context}.adapter.incoming.api/` | `product.adapter.incoming.api/ProductDto.java` |
| DTO converter | `{context}.adapter.incoming.api/` | `product.adapter.incoming.api/ProductDtoConverter.java` |
| Request DTO | `{context}.adapter.incoming.api/` | `product.adapter.incoming.api/CreateProductRequest.java` |
| Pug template | `resources/templates/{context}/` | `templates/product/catalog.pug` |
| Domain aggregate | `{context}.domain.model/` | `product.domain.model/Product.java` |
| Value object | `{context}.domain.model/` | `product.domain.model/SKU.java` |
| Domain event | `{context}.domain.event/` | `product.domain.event/ProductCreated.java` |
| Domain service | `{context}.domain.service/` | `product.domain.service/PricingService.java` |
| Factory | `{context}.domain.model/` | `product.domain.model/ProductFactory.java` |
| Repository interface | `{context}.application.port.out/` | `product.application.port.out/ProductRepository.java` |
| Repository impl | `{context}.adapter.outgoing.persistence/` | `product.adapter.outgoing.persistence/InMemoryProductRepository.java` |
| Use case | `{context}.application.usecase.{name}/` | `product.application.usecase.createproduct/CreateProductUseCase.java` |
| Command/Query | `{context}.application.usecase.{name}/` | `product.application.usecase.createproduct/CreateProductCommand.java` |
| Response | `{context}.application.usecase.{name}/` | `product.application.usecase.createproduct/CreateProductResponse.java` |
| Shared value object | `sharedkernel.domain.common/` | `sharedkernel.domain.common/Money.java` |

## Why This Structure?

**Bounded Context per Package:**
- Each bounded context is a self-contained package
- Clear ownership and boundaries
- Independent evolution of contexts
- Enables future extraction to microservices

**Separation by Interface Type:**
- REST APIs (`adapter.incoming.api/`) - Machine-to-machine
- Web Pages (`adapter.incoming.web/`) - Human-to-machine
- MCP Server (`adapter.incoming.mcp/`) - AI-to-machine

**Hexagonal Architecture (Ports & Adapters):**
- Input ports defined in application.usecase (use case interfaces)
- Output ports defined in application.port.out (repository interfaces)
- Incoming adapters implement/use input ports
- Outgoing adapters implement output ports
- Dependencies point inward toward domain

**Benefits:**
- Clear intent from package name
- Bounded contexts are isolated
- Independent evolution of each context and interface
- Different security policies per interface type
- Framework-independent domain layer
- Team ownership per bounded context

See [ADR-001: API/Web Package Separation](adr/adr-001-api-web-package-separation.md)

## Related Documentation

- [Architecture Principles](architecture-principles.md) - Hexagonal Architecture patterns
- [DTO vs ViewModel Analysis](dto-vs-viewmodel-analysis.md) - When to use each
- [MCP Server Integration](../integrations/mcp-server-integration.md) - MCP package details
