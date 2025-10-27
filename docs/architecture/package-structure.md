# Package Structure

This document describes the package organization of the ai-architecture project.

---

## Primary Adapters (Hexagonal Architecture)

Primary adapters handle **incoming requests** from external actors (users, systems).

### Package Structure

```
portadapter/incoming/
├── api/              ← REST APIs (Machine Interface)
│   ├── product/
│   │   ├── ProductResource.java
│   │   ├── ProductDto.java
│   │   ├── ProductDtoConverter.java
│   │   └── CreateProductRequest.java
│   └── cart/
│       ├── ShoppingCartResource.java
│       ├── ShoppingCartDto.java
│       ├── ShoppingCartDtoConverter.java
│       ├── CartItemDto.java
│       └── AddToCartRequest.java
├── mcp/              ← MCP Server (AI Interface)
│   └── ProductCatalogMcpTools.java
└── web/              ← Web Pages (Human Interface)
    ├── product/
    │   └── ProductPageController.java
    └── cart/
        └── (Future MVC controllers)
```

---

## API Package (`portadapter.incoming.api`)

**Purpose**: RESTful JSON APIs for programmatic access

### Characteristics

| Aspect | Details |
|--------|---------|
| **Annotation** | `@RestController` |
| **Naming** | Ends with `Resource` |
| **Returns** | JSON/XML (DTOs) |
| **URL Pattern** | `/api/*` |
| **Content-Type** | `application/json` |
| **Consumers** | Mobile apps, SPAs, external systems |

### Contents

- **Resources**: REST controller classes (`*Resource.java`)
- **DTOs**: Data Transfer Objects (`*Dto.java`)
- **Converters**: DTO converters (`*DtoConverter.java`)
- **Requests**: Request models (`*Request.java`)

### Example

```java
package de.sample.aiarchitecture.portadapter.incoming.api.product;

@RestController
@RequestMapping("/api/products")
public class ProductResource {

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productDtos);  // Returns JSON
    }
}
```

**Endpoints**:
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product
- `DELETE /api/products/{id}` - Delete product

---

## Web Package (`portadapter.incoming.web`)

**Purpose**: Server-side rendered HTML pages for browsers

### Characteristics

| Aspect | Details |
|--------|---------|
| **Annotation** | `@Controller` |
| **Naming** | Ends with `Controller` |
| **Returns** | HTML (via Pug templates) |
| **URL Pattern** | `/products/*`, `/cart/*` |
| **Content-Type** | `text/html` |
| **Consumers** | Web browsers (humans) |

### Contents

- **Controllers**: MVC controller classes (`*Controller.java`)
- **ViewModels**: View models (if used) (`*ViewModel.java`)
- **Converters**: ViewModel converters (if needed) (`*ViewModelConverter.java`)

### Example

```java
package de.sample.aiarchitecture.portadapter.incoming.web.product;

@Controller
@RequestMapping("/products")
public class ProductPageController {

    @GetMapping
    public String showProductCatalog(Model model) {
        model.addAttribute("products", products);
        return "product/catalog";  // Returns Pug template → HTML
    }
}
```

**Endpoints**:
- `GET /products` - Product catalog page
- `GET /products/{id}` - Product detail page
- `GET /products/sample` - Sample demonstration page

---

## Comparison: API vs Web

| Feature | API (`api/`) | Web (`web/`) |
|---------|-------------|--------------|
| **Purpose** | Programmatic access | User interface |
| **Annotation** | `@RestController` | `@Controller` |
| **Naming** | `*Resource` | `*Controller` |
| **Returns** | JSON/XML | HTML |
| **URL** | `/api/*` | `/*` |
| **Content-Type** | `application/json` | `text/html` |
| **Template Engine** | N/A (serialization) | Pug4j |
| **Data Model** | DTOs | Domain objects or ViewModels |
| **Converter** | DtoConverter | ViewModel converter (optional) |
| **Security** | Token-based, CORS | Session-based, CSRF |
| **Versioning** | Yes (`/api/v1`, `/api/v2`) | No |
| **Consumers** | Apps, systems | Browsers |
| **Example** | `ProductResource` | `ProductPageController` |

---

## Why This Structure?

### 1. **Separation of Concerns**

REST APIs and web pages serve different purposes:
- **APIs**: Machine-to-machine communication
- **Web**: Human-to-machine communication

### 2. **Clear Intent**

Package names communicate purpose:
- `api` → "This is a programmatic interface"
- `web` → "This is a user interface"

### 3. **Independent Evolution**

- Add API versioning without affecting web
- Redesign UI without breaking API
- Different release cycles

### 4. **Security Policies**

Easier to apply different security configurations:
- APIs: OAuth2, JWT tokens, rate limiting, no CSRF
- Web: Form login, sessions, CSRF protection

### 5. **Team Organization**

Different teams can own different interfaces:
- Backend team: `api/`
- Frontend team: `web/`

### 6. **Testing**

Different testing strategies:
- API: Contract tests, OpenAPI validation
- Web: UI tests, template rendering

---

## ArchUnit Enforcement

Our architecture tests enforce this structure:

```groovy
// NamingConventionsArchUnitTest.groovy

def "REST Controllers müssen mit 'Resource' enden"() {
  classes()
    .that().resideInAPackage("..incoming.api..")
    .and().areAnnotatedWith(RestController.class)
    .should().haveSimpleNameEndingWith("Resource")
    .check(allClasses)
}

def "Controller Klassen müssen mit 'Controller' enden"() {
  classes()
    .that().resideInAPackage("..incoming.web..")
    .and().areAnnotatedWith(Controller.class)
    .should().haveSimpleNameEndingWith("Controller")
    .check(allClasses)
}
```

**Verification**:
```bash
./gradlew test-architecture
# Ensures naming conventions are followed
```

---

## Migration History

**Original Structure** (Before):
```
portadapter/incoming/web/
├── product/
│   ├── ProductResource (REST)     ← Mixed!
│   ├── ProductPageController (MVC) ← Mixed!
│   ├── ProductDto
│   └── ...
```

**Current Structure** (After):
```
portadapter/incoming/
├── api/
│   └── product/
│       ├── ProductResource (REST)  ← Separated
│       ├── ProductDto
│       └── ...
├── mcp/
│   └── ProductCatalogMcpTools     ← AI interface
└── web/
    └── product/
        └── ProductPageController (MVC) ← Separated
```

**See**: [ADR-001: API/Web Package Separation](./adr/adr-001-api-web-package-separation.md)

---

## Best Practices

### For REST APIs (`api/`)

1. ✅ Use DTOs to decouple from domain
2. ✅ Include converters to transform domain → DTO
3. ✅ Use `@RestController` annotation
4. ✅ End class names with `Resource`
5. ✅ Return `ResponseEntity<>` for better control
6. ✅ Use `/api` URL prefix
7. ✅ Validate input with `@Valid`
8. ✅ Document with OpenAPI/Swagger

### For Web Pages (`web/`)

1. ✅ Use domain objects directly (for simple pages)
2. ✅ Use ViewModels for complex pages
3. ✅ Use `@Controller` annotation
4. ✅ End class names with `Controller`
5. ✅ Return view names (template paths)
6. ✅ Use semantic URLs (`/products`, not `/api/products`)
7. ✅ Add data to `Model` for templates
8. ✅ Keep controllers thin (delegate to services)

---

## Future Considerations

### GraphQL

If adding GraphQL:
```
portadapter/incoming/
├── api/
│   └── (REST resources)
├── graphql/          ← New package
│   ├── ProductResolver.java
│   └── schema.graphqls
├── mcp/
│   └── (AI interface)
└── web/
    └── (MVC controllers)
```

### gRPC

If adding gRPC:
```
portadapter/incoming/
├── api/
│   └── (REST resources)
├── grpc/             ← New package
│   ├── ProductGrpcService.java
│   └── product.proto
├── mcp/
│   └── (AI interface)
└── web/
    └── (MVC controllers)
```

### WebSocket

If adding WebSocket:
```
portadapter/incoming/
├── api/
│   └── (REST resources)
├── mcp/
│   └── (AI interface)
├── websocket/        ← New package
│   └── ProductWebSocketHandler.java
└── web/
    └── (MVC controllers)
```

---

## Quick Reference

### File Location Lookup

**Where to put a new...**

| File Type | Location | Example |
|-----------|----------|---------|
| REST controller | `api/{domain}/*Resource.java` | `api/product/ProductResource.java` |
| MVC controller | `web/{domain}/*Controller.java` | `web/product/ProductPageController.java` |
| DTO | `api/{domain}/*Dto.java` | `api/product/ProductDto.java` |
| DTO converter | `api/{domain}/*DtoConverter.java` | `api/product/ProductDtoConverter.java` |
| ViewModel | `web/{domain}/*ViewModel.java` | `web/product/ProductViewModel.java` |
| Request model | `api/{domain}/*Request.java` | `api/product/CreateProductRequest.java` |
| Pug template | `resources/templates/{domain}/` | `templates/product/catalog.pug` |

### URL Patterns

| Type | Pattern | Package | Example |
|------|---------|---------|---------|
| REST API | `/api/{resource}` | `api/` | `/api/products` |
| Web Page | `/{resource}` | `web/` | `/products` |

---

**Last Updated**: October 24, 2025
**Version**: 1.0
