# Package Structure

Package organization of the ai-architecture project.

## Full Structure

```
de.sample.aiarchitecture
├── application                     # Application Services (Use Cases)
│   ├── CartApplicationService
│   └── ProductApplicationService
│
├── domain.model                    # Domain Layer (Core)
│   ├── ddd                        # DDD Marker Interfaces
│   ├── shared                     # Shared Kernel
│   │   ├── Money, ProductId, Price
│   ├── product                    # Product Bounded Context
│   │   ├── Product (Aggregate Root)
│   │   ├── SKU, ProductName (Value Objects)
│   │   ├── ProductRepository (Interface)
│   │   └── ProductFactory
│   └── cart                       # Cart Bounded Context
│       ├── ShoppingCart (Aggregate Root)
│       ├── CartItem (Entity)
│       ├── CartId, Quantity (Value Objects)
│       └── CartRepository (Interface)
│
├── infrastructure                  # Infrastructure
│   ├── api                        # Public SPI (interfaces only)
│   │   └── DomainEventPublisher
│   └── config                     # Spring Configuration
│       ├── SecurityConfiguration
│       └── SpringDomainEventPublisher
│
└── portadapter                    # Adapters
    ├── incoming                   # Primary Adapters (Driving)
    │   ├── api                   # REST API (JSON)
    │   │   ├── product/
    │   │   │   ├── ProductResource
    │   │   │   ├── ProductDto
    │   │   │   └── ProductDtoConverter
    │   │   └── cart/
    │   ├── mcp                   # MCP Server (AI)
    │   │   └── ProductCatalogMcpTools
    │   └── web                   # Web MVC (HTML)
    │       └── product/
    │           └── ProductPageController
    │
    └── outgoing                   # Secondary Adapters (Driven)
        ├── product/
        │   └── InMemoryProductRepository
        └── cart/
            └── InMemoryShoppingCartRepository
```

## Primary Adapters: API vs Web vs MCP

| Aspect | API (`api/`) | Web (`web/`) | MCP (`mcp/`) |
|--------|-------------|--------------|--------------|
| **Annotation** | `@RestController` | `@Controller` | `@Component` |
| **Naming** | `*Resource` | `*Controller` | `*McpTools` |
| **Returns** | JSON/XML (DTOs) | HTML (templates) | DTOs (JSON-RPC) |
| **URL** | `/api/*` | `/*` | `/mcp` |
| **Content-Type** | `application/json` | `text/html` | `application/json` |
| **Consumers** | Apps, systems | Browsers (humans) | AI assistants |
| **Data Model** | DTOs | Domain or ViewModels | DTOs |

## ArchUnit Enforcement

Naming conventions are enforced by ArchUnit tests:

```groovy
def "REST Controllers must end with 'Resource'"() {
  classes()
    .that().resideInAPackage("..incoming.api..")
    .and().areAnnotatedWith(RestController.class)
    .should().haveSimpleNameEndingWith("Resource")
    .check(allClasses)
}

def "MVC Controllers must end with 'Controller'"() {
  classes()
    .that().resideInAPackage("..incoming.web..")
    .and().areAnnotatedWith(Controller.class)
    .should().haveSimpleNameEndingWith("Controller")
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
| REST controller | `api/{domain}/*Resource.java` | `api/product/ProductResource.java` |
| MVC controller | `web/{domain}/*Controller.java` | `web/product/ProductPageController.java` |
| DTO | `api/{domain}/*Dto.java` | `api/product/ProductDto.java` |
| DTO converter | `api/{domain}/*DtoConverter.java` | `api/product/ProductDtoConverter.java` |
| Pug template | `resources/templates/{domain}/` | `templates/product/catalog.pug` |
| Domain aggregate | `domain.model.{context}/` | `domain.model.product/Product.java` |
| Repository interface | `domain.model.{context}/` | `domain.model.product/ProductRepository.java` |
| Repository impl | `portadapter.outgoing.{context}/` | `portadapter.outgoing.product/InMemoryProductRepository.java` |

## Why This Structure?

**Separation by Interface Type:**
- REST APIs (`api/`) - Machine-to-machine
- Web Pages (`web/`) - Human-to-machine
- MCP Server (`mcp/`) - AI-to-machine

**Benefits:**
- Clear intent from package name
- Independent evolution of each interface
- Different security policies per interface type
- Team ownership per interface

See [ADR-001: API/Web Package Separation](adr/adr-001-api-web-package-separation.md)

## Related Documentation

- [Architecture Principles](architecture-principles.md) - Hexagonal Architecture patterns
- [DTO vs ViewModel Analysis](dto-vs-viewmodel-analysis.md) - When to use each
- [MCP Server Integration](../integrations/mcp-server-integration.md) - MCP package details
