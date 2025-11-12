# Pug4j Template Engine Integration

Pug4j is a Java implementation of the Pug template engine for server-side HTML rendering.

## Dependencies

```gradle
dependencies {
  implementation 'de.neuland-bfi:spring-pug4j:3.4.0'
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
    templateLoader.setSuffix(".pug");
    return templateLoader;
  }

  @Bean
  public PugConfiguration pugConfiguration(final SpringTemplateLoader templateLoader) {
    final PugConfiguration configuration = new PugConfiguration();
    configuration.setTemplateLoader(templateLoader);
    configuration.setCaching(false); // Disable for development
    configuration.setPrettyPrint(true);
    return configuration;
  }

  @Bean
  public ViewResolver pugViewResolver(final PugConfiguration pugConfiguration) {
    final PugViewResolver viewResolver = new PugViewResolver();
    viewResolver.setConfiguration(pugConfiguration);
    viewResolver.setOrder(0);
    return viewResolver;
  }
}
```

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
