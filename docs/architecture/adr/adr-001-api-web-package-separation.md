# ADR-001: Separate REST API and Web MVC Controllers into Different Packages

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team

---

## Context

Initially, both REST controllers (@RestController) and MVC controllers (@Controller) were placed in the same `adapter.incoming.web` package structure within each bounded context:

```
product/adapter/incoming/web/
├── ProductResource (REST API)
├── ProductPageController (MVC)
├── ProductDto
├── ProductDtoConverter
└── ...

cart/adapter/incoming/web/
├── ShoppingCartResource (REST API)
├── ShoppingCartDto
├── ShoppingCartDtoConverter
└── ...
```

### Problems with Original Structure

1. **Mixed Concerns**: REST APIs and web pages serve different purposes
   - REST APIs: JSON/XML for programmatic access (mobile apps, integrations)
   - Web pages: HTML for browser-based user interfaces

2. **Unclear Intent**: Package name "web" doesn't distinguish between:
   - Machine-to-machine APIs
   - Human-facing web pages

3. **Security Policies**: Different security requirements
   - APIs: Token-based, CORS, rate limiting
   - Web: Session-based, CSRF protection

4. **Versioning**: APIs need versioning (v1, v2), web pages don't

5. **Testing**: Different testing strategies
   - API: Contract testing, API spec validation
   - Web: UI testing, template rendering

6. **Team Organization**: Different teams may own different interfaces
   - Backend team: REST APIs
   - Frontend/Fullstack team: Web pages

---

## Decision

**We will separate REST API controllers and Web MVC controllers into distinct packages within each bounded context:**

```
product/adapter/incoming/
├── api/              ← REST APIs (@RestController)
│   ├── ProductResource
│   ├── ProductDto
│   ├── ProductDtoConverter
│   └── CreateProductRequest
├── web/              ← Web Pages (@Controller)
│   └── ProductPageController
└── mcp/              ← MCP Server (@Component)
    └── ProductCatalogMcpTools

cart/adapter/incoming/
├── api/              ← REST APIs (@RestController)
│   ├── ShoppingCartResource
│   ├── ShoppingCartDto
│   ├── ShoppingCartDtoConverter
│   ├── CartItemDto
│   └── AddToCartRequest
└── event/            ← Event Listeners (@Component)
    └── CartEventListener

portal/adapter/incoming/
└── web/              ← Web Pages (@Controller)
    └── HomePageController
```

### Package Responsibilities

#### `{context}.adapter.incoming.api`
- **Purpose**: RESTful JSON/XML APIs for programmatic access
- **Annotation**: `@RestController`
- **Naming**: Ends with `Resource` (e.g., `ProductResource`)
- **Returns**: JSON/XML (DTOs)
- **URL Pattern**: `/api/*`
- **Consumers**: Mobile apps, SPAs, external systems, integrations
- **Contains**:
  - REST controller classes
  - DTOs (Data Transfer Objects)
  - DTO converters
  - Request/Response models

#### `{context}.adapter.incoming.web`
- **Purpose**: Server-side rendered HTML pages for browsers
- **Annotation**: `@Controller`
- **Naming**: Ends with `Controller` (e.g., `ProductPageController`)
- **Returns**: HTML (via templates)
- **URL Pattern**: `/products/*`, `/cart/*`
- **Consumers**: Web browsers (humans)
- **Contains**:
  - MVC controller classes
  - ViewModels (if used)
  - ViewModel converters (if needed)

---

## Rationale

### 1. **Separation of Concerns**

REST APIs and web pages are fundamentally different interfaces to the same application:

```java
// API - Machine interface
@RestController
@RequestMapping("/api/products")
public class ProductResource {
  @GetMapping
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    return ResponseEntity.ok(products);  // Returns JSON
  }
}

// Web - Human interface
@Controller
@RequestMapping("/products")
public class ProductPageController {
  @GetMapping
  public String showProducts(Model model) {
    model.addAttribute("products", products);
    return "product/catalog";  // Returns HTML
  }
}
```

### 2. **Clear Intent and Discoverability**

Package names clearly communicate purpose:
- `api` → "This is a programmatic interface"
- `web` → "This is a user interface"

Developers immediately understand what each package contains.

### 3. **Independent Evolution**

APIs and web pages can evolve independently:
- Add API versioning (`/api/v1`, `/api/v2`) without affecting web
- Redesign web UI without breaking API contracts
- Different release cycles for API stability vs. UI features

### 4. **Security Configuration**

Easier to apply different security policies:

```java
@Configuration
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) {
    http
      .securityMatcher("/api/**")
      .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
      .oauth2ResourceServer(oauth2 -> oauth2.jwt())  // Token-based
      .csrf().disable();  // No CSRF for APIs
    return http.build();
  }

  @Bean
  public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) {
    http
      .securityMatcher("/products/**", "/cart/**")
      .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
      .formLogin()  // Session-based
      .csrf().enable();  // CSRF protection for web
    return http.build();
  }
}
```

### 5. **Testing Strategy**

Different testing approaches:

```groovy
// API Tests - Contract testing
class ProductResourceTest {
  def "API should return valid JSON schema"() {
    // Test API contract, status codes, JSON structure
  }
}

// Web Tests - UI testing
class ProductPageControllerTest {
  def "Page should render product catalog"() {
    // Test template rendering, HTML structure
  }
}
```

### 6. **ArchUnit Enforcement**

Our naming conventions already distinguish them:
- `@RestController` → Must end with `Resource`
- `@Controller` → Must end with `Controller`

The package structure reinforces this distinction:

```groovy
// NamingConventionsArchUnitTest.groovy
def "REST Controllers must end with 'Resource' (REST best practice)"() {
  classes()
    .that().resideInAPackage("..adapter.incoming.api..")  // ← Package check
    .and().areAnnotatedWith(RestController.class)
    .should().haveSimpleNameEndingWith("Resource")
    .check(allClasses)
}

def "Controller classes must end with 'Controller'"() {
  classes()
    .that().resideInAPackage("..adapter.incoming.web..")  // ← Package check
    .and().areAnnotatedWith(Controller.class)
    .should().haveSimpleNameEndingWith("Controller")
    .check(allClasses)
}
```

---

## Consequences

### Positive

✅ **Clearer Architecture**: Package structure reflects application boundaries
✅ **Better Separation**: API and web concerns are isolated
✅ **Easier Security**: Apply different policies per interface type
✅ **Independent Versioning**: APIs can be versioned independently
✅ **Team Ownership**: Clear boundaries for different teams
✅ **Discoverability**: New developers immediately understand structure
✅ **Testing**: Easier to apply different testing strategies
✅ **Documentation**: API docs separate from web page docs

### Neutral

⚠️ **More Packages**: Slightly more directory nesting
⚠️ **Refactoring Effort**: One-time cost to reorganize

### Negative

❌ **None identified**

---

## Implementation

### Changes Made

1. **Created new package structure within each bounded context**:
   ```bash
   mkdir product/adapter/incoming/{api,web,mcp}
   mkdir cart/adapter/incoming/{api,event}
   mkdir portal/adapter/incoming/web
   ```

2. **Moved REST controllers to `api` package**:
   - `product/adapter/incoming/web/ProductResource.java` → `product/adapter/incoming/api/ProductResource.java`
   - `cart/adapter/incoming/web/ShoppingCartResource.java` → `cart/adapter/incoming/api/ShoppingCartResource.java`

3. **Moved DTOs and converters to `api` package**:
   - All `*Dto.java` files moved to `{context}/adapter/incoming/api/`
   - All `*DtoConverter.java` files moved to `{context}/adapter/incoming/api/`
   - All request/response models moved to `{context}/adapter/incoming/api/`

4. **Kept MVC controllers in `web` package**:
   - `product/adapter/incoming/web/ProductPageController.java` (unchanged location)
   - `portal/adapter/incoming/web/HomePageController.java`

5. **Updated package declarations**:
   ```java
   // Before
   package de.sample.aiarchitecture.product.adapter.incoming.web;

   // After (REST)
   package de.sample.aiarchitecture.product.adapter.incoming.api;

   // After (MVC)
   package de.sample.aiarchitecture.product.adapter.incoming.web;
   ```

6. **Verified architecture tests**: All ArchUnit tests pass ✅

7. **Verified application**: Builds and runs successfully ✅

### Migration Checklist

- [x] Create `api` package structure
- [x] Move REST controller classes
- [x] Move DTO classes
- [x] Move DTO converter classes
- [x] Move request/response classes
- [x] Keep MVC controllers in `web`
- [x] Update package declarations
- [x] Run architecture tests
- [x] Build application
- [x] Test endpoints
- [x] Document decision (this ADR)

---

## Alternatives Considered

### Alternative 1: Keep everything in `web`

**Rejected**: Doesn't distinguish between different types of interfaces.

### Alternative 2: `rest` instead of `api`

**Rejected**: "API" is more general and doesn't commit to REST specifically. Could support GraphQL, gRPC later.

### Alternative 3: `http` for both

**Rejected**: Too generic. Both REST and web use HTTP.

### Alternative 4: Separate by URL pattern (`/api` vs `/pages`)

**Rejected**: Package structure should reflect architectural boundaries, not URL patterns.

---

## References

### Related Patterns

1. **Hexagonal Architecture**: Primary adapters separated by interface type
2. **Ports and Adapters**: Clear distinction between different adapter types
3. **RESTful API Design**: APIs as first-class interface (not afterthought)
4. **BFF Pattern**: Backends for Frontends - different APIs for different clients

### Related ADRs

- None yet (this is ADR-001)

### External References

- [RESTful Web Services](https://restfulapi.net/)
- [Spring MVC vs REST Controller](https://spring.io/guides/gs/serving-web-content/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

## Validation

### Tests

All architecture tests pass:
```bash
./gradlew test-architecture
# BUILD SUCCESSFUL
```

### Build

Application builds successfully:
```bash
./gradlew build
# BUILD SUCCESSFUL
```

### Runtime

Application starts and endpoints work:
- ✅ REST API: `http://localhost:8080/api/products`
- ✅ Web Pages: `http://localhost:8080/products`

---

## Review and Update

**Next Review**: After 3 months of usage
**Review Date**: January 24, 2026

**Update Criteria**:
- Team feedback on structure
- New requirements (GraphQL, gRPC, etc.)
- Security policy changes
- Team organization changes

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
