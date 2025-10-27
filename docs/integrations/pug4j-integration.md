# Pug4j Template Engine Integration

This document describes the integration of Pug4j (Java implementation of the Pug template engine) with the Spring Boot application.

---

## Table of Contents

1. [Overview](#overview)
2. [Dependencies](#dependencies)
3. [Configuration](#configuration)
4. [Controllers](#controllers)
5. [Templates](#templates)
6. [Testing](#testing)
7. [Naming Conventions](#naming-conventions)

---

## Overview

**Pug4j** is a Java implementation of the Pug template engine (formerly known as Jade). It provides:
- Clean, readable syntax (no angle brackets)
- Template inheritance and mixins
- Server-side rendering for HTML pages
- Integration with Spring MVC

**Use Cases**:
- Server-side rendered web pages
- Admin interfaces
- Static content pages
- SEO-friendly HTML output

---

## Dependencies

### Gradle Configuration

**File**: `build.gradle`

```gradle
dependencies {
  // Pug4j Template Engine
  implementation 'de.neuland-bfi:spring-pug4j:3.4.0'
}
```

**Version**: 3.4.0
**Repository**: Maven Central
**License**: MIT

---

## Configuration

### Spring Configuration Class

**File**: `src/main/java/de/sample/aiarchitecture/infrastructure/config/Pug4jConfiguration.java`

```java
@Configuration
public class Pug4jConfiguration {

  @Bean
  public SpringTemplateLoader templateLoader() {
    final SpringTemplateLoader templateLoader = new SpringTemplateLoader();
    templateLoader.setTemplateLoaderPath("classpath:/templates/");
    templateLoader.setSuffix(".pug");
    return templateLoader;
  }

  @Bean
  public PugConfiguration pugConfiguration(final SpringTemplateLoader templateLoader) {
    final PugConfiguration configuration = new PugConfiguration();
    configuration.setTemplateLoader(templateLoader);
    configuration.setCaching(false); // Disable for development
    configuration.setPrettyPrint(true); // Pretty-print HTML
    return configuration;
  }

  @Bean
  public ViewResolver pugViewResolver(final PugConfiguration pugConfiguration) {
    final PugViewResolver viewResolver = new PugViewResolver();
    viewResolver.setConfiguration(pugConfiguration);
    viewResolver.setOrder(0); // Process Pug templates first
    return viewResolver;
  }
}
```

**Key Settings**:
- **Template Location**: `src/main/resources/templates/`
- **File Extension**: `.pug`
- **Caching**: Disabled in development (enable in production)
- **Pretty Print**: Enabled for readable HTML output
- **View Resolver Order**: 0 (processed before other resolvers)

---

## Controllers

### MVC Controller vs REST Controller

The project uses **two types of controllers** with different naming conventions:

| Type | Annotation | Suffix | Purpose | Example |
|------|------------|--------|---------|---------|
| **MVC Controller** | `@Controller` | `Controller` | Server-side HTML rendering | `ProductPageController` |
| **REST Controller** | `@RestController` | `Resource` | RESTful JSON API | `ProductResource` |

### ProductPageController

**File**: `src/main/java/de/sample/aiarchitecture/portadapter/incoming/web/product/ProductPageController.java`

```java
@Controller
@RequestMapping("/products")
public class ProductPageController {

  private final ProductApplicationService productApplicationService;

  public ProductPageController(final ProductApplicationService productApplicationService) {
    this.productApplicationService = productApplicationService;
  }

  /**
   * Displays the product catalog page.
   */
  @GetMapping
  public String showProductCatalog(final Model model) {
    final List<Product> products = productApplicationService.getAllProducts();

    model.addAttribute("products", products);
    model.addAttribute("title", "Product Catalog");
    model.addAttribute("totalProducts", products.size());

    return "product/catalog"; // Resolves to templates/product/catalog.pug
  }

  /**
   * Displays a single product detail page.
   */
  @GetMapping("/{id}")
  public String showProductDetail(@PathVariable final String id, final Model model) {
    return productApplicationService
        .findProductById(ProductId.of(id))
        .map(product -> {
          model.addAttribute("product", product);
          model.addAttribute("title", product.name().value());
          model.addAttribute("isAvailable", product.isAvailable());
          return "product/detail";
        })
        .orElse("error/404");
  }

  /**
   * Displays a sample landing page.
   */
  @GetMapping("/sample")
  public String showSamplePage(final Model model) {
    model.addAttribute("title", "Pug4j Sample Page");
    model.addAttribute("message", "Welcome to the Product Catalog");
    model.addAttribute("features", List.of("DDD Architecture", "Clean Code", "Pug Templates"));

    return "product/sample";
  }
}
```

**Endpoints**:
- `GET /products` - Product catalog page
- `GET /products/{id}` - Product detail page
- `GET /products/sample` - Sample demonstration page

---

## Templates

### Template Structure

```
src/main/resources/templates/
├── layout.pug                 # Base layout template
├── product/
│   ├── catalog.pug           # Product listing page
│   ├── detail.pug            # Product detail page
│   └── sample.pug            # Sample/demo page
└── error/
    └── 404.pug               # 404 error page
```

### Base Layout Template

**File**: `templates/layout.pug`

```pug
doctype html
html(lang="en")
  head
    meta(charset="UTF-8")
    meta(name="viewport" content="width=device-width, initial-scale=1.0")
    title= title || "E-Commerce Sample"
    style.
      /* CSS styles here */
  body
    header
      h1= title || "E-Commerce Sample"
      nav
        a(href="/products") Products
        a(href="/products/sample") Sample Page
        a(href="/api/products") REST API
    main
      block content
    footer
      p Built with DDD, Clean Architecture & Pug4j
```

**Features**:
- **Template Inheritance**: Other templates extend this layout
- **Blocks**: Content block for child templates
- **Variables**: Dynamic title
- **Embedded CSS**: Styles defined inline

### Sample Page Template

**File**: `templates/product/sample.pug`

```pug
extends ../layout

block content
  h2= message

  p This is a sample page demonstrating Pug4j template engine.

  h3 Project Features
  ul
    each feature in features
      li= feature

  h3 Architecture Patterns
  ul
    li Domain-Driven Design (DDD)
    li Clean Architecture
    li Testing
```

**Pug Features Demonstrated**:
- **Template Inheritance**: `extends ../layout`
- **Content Blocks**: `block content`
- **Variables**: `= message`
- **Iteration**: `each feature in features`
- **HTML Generation**: Clean, readable syntax

### Product Catalog Template

**File**: `templates/product/catalog.pug`

```pug
extends ../layout

block content
  h2 Product Catalog

  p Total Products: #{totalProducts}

  if products && products.size() > 0
    .product-grid
      each product in products
        .product-card
          h3= product.name().value()
          p.product-description= product.description().value()

          .product-info
            .info-row
              span.label SKU:
              span.value= product.sku().value()

            .info-row
              span.label Price:
              span.value= product.price().amount().amount() + " " + product.price().amount().currency().getSymbol()

            .info-row
              span.label Status:
              if product.isAvailable()
                span.badge.badge-success Available
              else
                span.badge.badge-danger Out of Stock

          a.btn(href=`/products/${product.id().value()}`) View Details
  else
    .empty-state
      p No products available.
```

**Pug Features Used**:
- **Conditionals**: `if/else`
- **Iteration**: `each product in products`
- **Interpolation**: `#{totalProducts}`
- **Method Calls**: `product.name().value()`
- **String Templates**: `` `href=\`/products/${id}\`` ``

### Product Detail Template

**File**: `templates/product/detail.pug`

```pug
extends ../layout

block content
  .product-detail
    .product-header
      h2= product.name().value()
      if isAvailable
        span.badge.badge-success In Stock
      else
        span.badge.badge-danger Out of Stock

    .product-content
      .product-main
        h3 Description
        p= product.description().value() || "No description available."

        h3 Pricing
        .price-box
          .price-main= product.price().amount().amount() + " " + product.price().amount().currency().getSymbol()

      .product-sidebar
        h3 Product Information
        .info-section
          .info-item
            .info-label Product ID
            .info-value= product.id().value()

          .info-item
            .info-label Availability
            if isAvailable
              .info-value.text-success ✓ Available for purchase
            else
              .info-value.text-danger ✗ Currently out of stock

    .product-actions
      a.btn.btn-secondary(href="/products") ← Back to Catalog
      if isAvailable
        button.btn.btn-primary Add to Cart
```

### 404 Error Template

**File**: `templates/error/404.pug`

```pug
extends ../layout

block content
  .error-page
    h1.error-code 404
    h2.error-message Product Not Found

    p.error-description The product you're looking for doesn't exist.

    .error-actions
      a.btn(href="/products") Browse All Products
```

---

## Testing

### Manual Testing

1. **Start the application**:
   ```bash
   ./gradlew bootRun
   ```

2. **Test endpoints** (use credentials: `user:password`):
   ```bash
   # Sample page
   curl -u user:password http://localhost:8080/products/sample

   # Product catalog
   curl -u user:password http://localhost:8080/products

   # Product detail (use actual product ID)
   curl -u user:password http://localhost:8080/products/{product-id}
   ```

3. **Browser testing**:
   - Navigate to http://localhost:8080/products/sample
   - Login with username `user` and the generated password from console
   - Explore the product catalog

### Expected Output

**Sample Page** (`/products/sample`):
- Title: "Pug4j Sample Page"
- List of project features
- List of architecture patterns
- Navigation links

**Product Catalog** (`/products`):
- Grid of product cards
- Product information (SKU, price, stock, availability)
- "View Details" buttons

**Product Detail** (`/products/{id}`):
- Full product information
- Availability status
- "Back to Catalog" link
- "Add to Cart" button (if available)

---

## Naming Conventions

### Controller Naming Rules

The project enforces **strict naming conventions** through ArchUnit tests:

#### MVC Controllers (Traditional Web Pages)

- **Annotation**: `@Controller`
- **Suffix**: Must end with `Controller`
- **Purpose**: Server-side HTML rendering with templates
- **Example**: `ProductPageController`

```java
@Controller
@RequestMapping("/products")
public class ProductPageController {  // ✅ Correct - ends with "Controller"
  // Renders Pug templates
}
```

#### REST Controllers (JSON APIs)

- **Annotation**: `@RestController`
- **Suffix**: Must end with `Resource`
- **Purpose**: RESTful JSON API endpoints
- **Example**: `ProductResource`

```java
@RestController
@RequestMapping("/api/products")
public class ProductResource {  // ✅ Correct - ends with "Resource"
  // Returns JSON responses
}
```

### ArchUnit Test

**File**: `src/test-architecture/groovy/de/sample/aiarchitecture/NamingConventionsArchUnitTest.groovy`

```groovy
def "Controller Klassen müssen mit 'Controller' enden"() {
  expect:
  classes()
    .that().resideInAPackage(PRIMARY_ADAPTER_PACKAGE)
    .and().areAnnotatedWith(Controller.class)
    .should().haveSimpleNameEndingWith("Controller")
    .because("@Controller annotated classes should follow naming conventions")
    .check(allClasses)
}

def "REST Controllers müssen mit 'Resource' enden (REST best practice)"() {
  expect:
  classes()
    .that().resideInAPackage(PRIMARY_ADAPTER_PACKAGE)
    .and().areAnnotatedWith(RestController.class)
    .should().haveSimpleNameEndingWith("Resource")
    .because("@RestController annotated classes should end with 'Resource' following RESTful naming conventions")
    .check(allClasses)
}
```

**Verification**:
```bash
./gradlew test-architecture
```

---

## Comparison: REST vs MVC

| Feature | REST Controller | MVC Controller |
|---------|----------------|----------------|
| **Annotation** | `@RestController` | `@Controller` |
| **Returns** | JSON/XML data | HTML views |
| **View Technology** | N/A (serialization) | Pug4j templates |
| **Naming Suffix** | `Resource` | `Controller` |
| **Use Case** | API endpoints | Web pages |
| **Example URL** | `/api/products` | `/products` |
| **Content-Type** | `application/json` | `text/html` |

### Example: Same Domain, Two Approaches

**REST API** (`ProductResource`):
```java
@RestController
@RequestMapping("/api/products")
public class ProductResource {

  @GetMapping
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    // Returns JSON
    return ResponseEntity.ok(products);
  }
}
```

**MVC Page** (`ProductPageController`):
```java
@Controller
@RequestMapping("/products")
public class ProductPageController {

  @GetMapping
  public String showProductCatalog(Model model) {
    model.addAttribute("products", products);
    // Returns Pug template name
    return "product/catalog";
  }
}
```

---

## Pug Syntax Reference

### Variables

```pug
h1= title
p= product.name().value()
p Total: #{count}
```

### Conditionals

```pug
if isAvailable
  p In Stock
else
  p Out of Stock
```

### Iteration

```pug
each product in products
  li= product.name()
```

### Template Inheritance

```pug
// layout.pug
doctype html
html
  body
    block content

// page.pug
extends layout

block content
  h1 My Page
```

### Attributes

```pug
a(href="/products" class="btn") Click me
input(type="text" name="search" required)
```

### String Interpolation

```pug
a(href=`/products/${id}`) View
```

---

## Benefits of Pug4j

### Developer Experience

1. **Clean Syntax**: No closing tags, less verbose
2. **Readability**: Indentation-based structure
3. **DRY**: Template inheritance reduces duplication
4. **Type Safety**: Direct access to Java objects

### Architecture Benefits

1. **Separation of Concerns**: Templates separate from logic
2. **Hexagonal Architecture**: MVC controllers are primary adapters
3. **DDD Alignment**: Templates work directly with domain objects
4. **Clean Architecture**: View layer doesn't pollute domain

### Example: Domain Objects in Templates

```pug
// Direct access to domain model
h2= product.name().value()
p= product.sku().value()
p= product.price().amount().amount()

// Calling domain methods
if product.isAvailable()
  span Available
```

**Benefits**:
- No need for separate DTOs for views
- Templates reflect domain language
- Changes to domain automatically reflected in views

---

## Production Considerations

### Performance

**Enable Caching in Production**:
```java
@Bean
public PugConfiguration pugConfiguration(final SpringTemplateLoader templateLoader) {
  final PugConfiguration configuration = new PugConfiguration();
  configuration.setTemplateLoader(templateLoader);
  configuration.setCaching(true);  // Enable for production
  configuration.setPrettyPrint(false); // Disable pretty print
  return configuration;
}
```

### Profile-Based Configuration

```java
@Configuration
public class Pug4jConfiguration {

  @Value("${spring.profiles.active:dev}")
  private String activeProfile;

  @Bean
  public PugConfiguration pugConfiguration(final SpringTemplateLoader templateLoader) {
    final PugConfiguration configuration = new PugConfiguration();
    configuration.setTemplateLoader(templateLoader);
    configuration.setCaching(!"dev".equals(activeProfile));
    configuration.setPrettyPrint("dev".equals(activeProfile));
    return configuration;
  }
}
```

---

## Troubleshooting

### Template Not Found

**Error**: `TemplateException: Template not found`

**Solution**: Check template path and naming:
- Template location: `src/main/resources/templates/`
- View name: `product/catalog` → `templates/product/catalog.pug`
- Extension: Must be `.pug`

### View Resolver Conflicts

**Error**: Wrong template engine rendering Pug files

**Solution**: Set correct view resolver order:
```java
viewResolver.setOrder(0); // Process Pug first
```

### Compilation Errors

**Error**: Pug syntax errors

**Solution**:
- Check indentation (use spaces, not tabs)
- Verify tag names and attributes
- Test template in isolation

---

## Summary

### What We Implemented

1. ✅ **Dependency**: Added `spring-pug4j:3.4.0`
2. ✅ **Configuration**: Created `Pug4jConfiguration` class
3. ✅ **Controller**: Created `ProductPageController` (MVC)
4. ✅ **Templates**:
   - Base layout
   - Product catalog
   - Product detail
   - Sample page
   - 404 error page
5. ✅ **Testing**: Verified with curl and browser
6. ✅ **Architecture**: Follows naming conventions (ArchUnit enforced)

### Key Takeaways

- **MVC Controllers** use `@Controller` and end with `Controller`
- **REST Controllers** use `@RestController` and end with `Resource`
- **Pug templates** provide clean, readable HTML generation
- **Domain objects** work directly in templates (no extra DTOs needed)
- **Hexagonal architecture** preserved (controllers are primary adapters)
- **ArchUnit tests** enforce naming conventions automatically

---

**Documentation Date**: October 24, 2025
**Version**: 1.0
**Author**: AI Architecture Team
