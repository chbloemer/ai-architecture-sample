# Pug4j Template Engine Integration

Pug4j is a Java implementation of the Pug template engine for server-side HTML rendering.

## Dependencies

```gradle
dependencies {
  implementation 'de.neuland-bfi:spring-pug4j:3.6.0'
}
```

## Configuration

**File**: `src/main/java/de/sample/aiarchitecture/infrastructure/config/Pug4jConfiguration.java`

```java
@Configuration
public class Pug4jConfiguration {

  @Bean
  public SpringTemplateLoader templateLoader() {
    final SpringTemplateLoader templateLoader = new SpringTemplateLoader();
    templateLoader.setTemplateLoaderPath("classpath:/templates/");
    templateLoader.setEncoding("UTF-8");
    templateLoader.setSuffix(".pug");
    return templateLoader;
  }

  @Bean
  public PugEngine pugEngine(final SpringTemplateLoader templateLoader) {
    return PugEngine.builder()
        .templateLoader(templateLoader)
        .caching(false) // Disable for development
        .build();
  }

  @Bean
  public RenderContext renderContext() {
    return RenderContext.builder()
        .prettyPrint(true)
        .build();
  }

  @Bean
  public PugViewResolver pugViewResolver(
      final PugEngine pugEngine, final RenderContext renderContext) {
    final PugViewResolver viewResolver = new PugViewResolver();
    viewResolver.setEngine(pugEngine);
    viewResolver.setRenderContext(renderContext);
    viewResolver.setOrder(0);
    return viewResolver;
  }
}
```

## Debug Error Page

On template errors, a debug page shows the template source with the failing line highlighted.

**Enable** (development only — exposes template source):

```yaml
# application.yml
spring:
  pug4j:
    debug-error-page: true
```

Since spring-pug4j 3.6.0 this works on Spring Boot 4 out of the box
(`Boot4PugDebugErrorViewResolver` auto-configuration). The old property
`pug4j.spring.debug-error-page` still works but is deprecated. Requests with
`Accept: text/html` get the debug page; other clients get Spring Boot's JSON error response.

spring-pug4j 3.6.0 also auto-configures `SpringTemplateLoader`, `PugEngine`, and
`PugViewResolver` (configurable via `spring.pug4j.*` properties, analogous to
`spring.thymeleaf.*`). The manual configuration above takes precedence — the
auto-configuration backs off via `@ConditionalOnMissingBean`.

The error dispatch to `/error` must be permitted in the security configuration
(`.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()`), otherwise template errors surface
as a bare 403.

**Demo:** `GET /debug/pug-error` renders the intentionally broken template
`templates/debug/broken.pug` (see `PugDebugDemoPageController`) — kept as a learning artifact to
showcase the debug error page; not production code.

## Controller Example

**File**: `src/main/java/de/sample/aiarchitecture/portadapter/incoming/web/product/ProductPageController.java`

```java
@Controller
@RequestMapping("/products")
public class ProductPageController {

  private final GetAllProductsUseCase getAllProductsUseCase;
  private final GetProductByIdUseCase getProductByIdUseCase;

  @GetMapping
  public String showProductCatalog(final Model model) {
    GetAllProductsQuery query = new GetAllProductsQuery();
    GetAllProductsResponse response = getAllProductsUseCase.execute(query);

    model.addAttribute("products", response.products());
    model.addAttribute("title", "Product Catalog");
    return "product/catalog"; // Resolves to templates/product/catalog.pug
  }

  @GetMapping("/{id}")
  public String showProductDetail(@PathVariable final String id, final Model model) {
    GetProductByIdQuery query = new GetProductByIdQuery(id);

    try {
      GetProductByIdResponse response = getProductByIdUseCase.execute(query);
      model.addAttribute("product", response.product());
      return "product/detail";
    } catch (IllegalArgumentException e) {
      return "error/404";
    }
  }
}
```

## Template Structure

```
src/main/resources/templates/
├── layout.pug              # Base layout
├── product/
│   ├── catalog.pug        # Product listing
│   └── detail.pug         # Product details
└── error/
    └── 404.pug            # Error page
```

### Base Layout

```pug
doctype html
html(lang="en")
  head
    title= title || "E-Commerce Sample"
  body
    header
      h1= title
    main
      block content
    footer
      p Built with DDD & Pug4j
```

### Product Catalog

```pug
extends ../layout

block content
  h2 Product Catalog

  if products && products.size() > 0
    .product-grid
      each product in products
        .product-card
          h3= product.name().value()
          p= product.description().value()
          p Price: #{product.price().amount().amount()}
          a.btn(href=`/products/${product.id().value()}`) View Details
  else
    p No products available.
```

## Pug Syntax Quick Reference

```pug
// Variables
h1= title
p Total: #{count}

// Conditionals
if isAvailable
  span In Stock
else
  span Out of Stock

// Iteration
each product in products
  li= product.name()

// Template Inheritance
extends layout
block content
  h1 Content here

// Attributes
a(href="/products" class="btn") Click
```

## Naming Conventions

**MVC Controllers** (render HTML):
- Annotation: `@Controller`
- Suffix: `Controller`
- Example: `ProductPageController`

**REST Controllers** (return JSON):
- Annotation: `@RestController`
- Suffix: `Resource`
- Example: `ProductResource`

These naming conventions are enforced by ArchUnit tests.

## Testing

```bash
# Start application
./gradlew bootRun

# Access pages (credentials: user:password)
http://localhost:8080/products
http://localhost:8080/products/{id}
```
