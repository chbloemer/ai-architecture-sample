# DTOs vs ViewModels: REST vs MVC Controllers Analysis

**Question**: Should MVC Controllers use DTOs/Converters like REST Controllers do?

**Short Answer**: **It depends on the use case**, but generally:
- ✅ **REST Controllers**: Always use DTOs (current approach is correct)
- ⚠️ **MVC Controllers**: Use ViewModels selectively (current direct domain approach is valid, but ViewModels offer benefits)

---

## Table of Contents

1. [Current Implementation Analysis](#current-implementation-analysis)
2. [REST Controllers: Why DTOs Are Essential](#rest-controllers-why-dtos-are-essential)
3. [MVC Controllers: Two Valid Approaches](#mvc-controllers-two-valid-approaches)
4. [Comparison Matrix](#comparison-matrix)
5. [Recommendation](#recommendation)
6. [Implementation Examples](#implementation-examples)

---

## Current Implementation Analysis

### REST Controller (ProductResource) - Uses DTOs ✅

**Current Pattern**:
```java
@RestController
@RequestMapping("/api/products")
public class ProductResource {

  private final ProductApplicationService productService;
  private final ProductDtoConverter converter;  // ← Converter

  @GetMapping
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    final List<ProductDto> products =
        productService.getAllProducts().stream()
            .map(converter::toDto)  // ← Domain → DTO
            .toList();

    return ResponseEntity.ok(products);
  }
}
```

**DTO Structure**:
```java
public record ProductDto(
    String id,
    String sku,
    String name,
    String description,
    BigDecimal price,      // ← Flattened
    String currency,       // ← Extracted from Money
    String category,
    int stock
) {}
```

**Converter**:
```java
@Component
public final class ProductDtoConverter {

  public ProductDto toDto(final Product product) {
    return new ProductDto(
        product.id().value(),
        product.sku().value(),
        product.name().value(),
        product.description().value(),
        product.price().value().amount(),           // ← Unwrap
        product.price().value().currency().getCurrencyCode(),  // ← Extract
        product.category().name(),
        product.stock().quantity()
    );
  }
}
```

**Benefits**:
- ✅ Stable API contract independent of domain changes
- ✅ Flattened structure (no nested objects)
- ✅ Serialization control
- ✅ Versioning capability
- ✅ Security (hide internal details)

---

### MVC Controller (ProductPageController) - Uses Domain Objects Directly

**Current Pattern**:
```java
@Controller
@RequestMapping("/products")
public class ProductPageController {

  private final ProductApplicationService productService;
  // NO converter! ← Direct domain usage

  @GetMapping
  public String showProductCatalog(final Model model) {
    final List<Product> products = productService.getAllProducts();

    model.addAttribute("products", products);  // ← Pass domain directly
    model.addAttribute("title", "Product Catalog");

    return "product/catalog";
  }
}
```

**Template Usage** (Direct Domain Access):
```pug
each product in products
  .product-card
    h3= product.name().value()                              // ← Call domain methods
    p= product.description().value()
    span= product.price().amount().amount()                 // ← Deep navigation
    span= product.price().amount().currency().getSymbol()   // ← Chain calls
```

**Current Approach Benefits**:
- ✅ Simple and straightforward
- ✅ No boilerplate (no DTOs, no converters)
- ✅ Follows ubiquitous language (domain methods in templates)
- ✅ DDD-aligned (templates speak domain language)
- ✅ Server-side only (no security concerns)

**Current Approach Drawbacks**:
- ⚠️ Templates tightly coupled to domain structure
- ⚠️ Complex expressions in templates (`product.price().amount().currency().getSymbol()`)
- ⚠️ No view-specific formatting
- ⚠️ Domain changes may break templates
- ⚠️ Difficult to test templates independently

---

## REST Controllers: Why DTOs Are Essential

### Reasons to ALWAYS Use DTOs in REST APIs

#### 1. **API Stability / Versioning**

**Problem**: Domain changes break API consumers
```java
// Domain changes from Money to Price
public class Product {
  private Money price;  // v1
  private Price price;  // v2 - different structure!
}
```

**Solution**: DTO remains stable
```java
public record ProductDto(
    BigDecimal price,    // ← Always same structure
    String currency
) {}

// Converter adapts to domain changes
public ProductDto toDto(Product product) {
    return new ProductDto(
        product.price().value().amount(),  // v1
        // OR
        product.priceAmount().amount(),    // v2
        // API consumers don't change!
    );
}
```

#### 2. **Security / Information Hiding**

**Problem**: Domain may contain sensitive data
```java
public class Product {
  private ProductId id;
  private Money costPrice;      // ← Internal cost (sensitive!)
  private Money sellingPrice;   // ← Public price
  private Supplier supplier;    // ← Sensitive business relationship
}
```

**Solution**: DTO exposes only public data
```java
public record ProductDto(
    String id,
    BigDecimal price,    // ← Only selling price exposed
    String currency
    // NO costPrice
    // NO supplier
) {}
```

#### 3. **Serialization Control**

**Problem**: Domain objects may not serialize well
```java
public class Product extends BaseAggregateRoot<Product, ProductId> {
  private List<DomainEvent> domainEvents;  // ← Should NOT be serialized
  private ProductId id;                     // ← Complex object
}
```

**Solution**: DTO controls serialization
```java
public record ProductDto(
    String id,           // ← Simple string, not complex object
    // NO domainEvents
) {}
```

#### 4. **API Contract Documentation**

**Problem**: Domain objects expose internal structure
```java
// What does the API return? Hard to document
@GetMapping
public Product getProduct() { ... }
```

**Solution**: DTO is clear contract
```java
// Clear, documented API contract
@GetMapping
public ProductDto getProduct() { ... }

/**
 * @returns {
 *   id: string,
 *   name: string,
 *   price: number,
 *   currency: string
 * }
 */
```

#### 5. **Performance Optimization**

**Problem**: Domain may require multiple queries
```java
public class Product {
  public List<Review> getReviews() {
    return reviewRepository.findByProductId(this.id);  // ← Lazy load
  }
}
```

**Solution**: DTO can aggregate efficiently
```java
public record ProductDto(
    String id,
    String name,
    int reviewCount    // ← Precomputed, no lazy loading
) {}
```

### Conclusion: REST Controllers

**✅ ALWAYS use DTOs in REST Controllers**

Reasons:
- External API boundary
- Needs stability and versioning
- Security concerns
- Serialization control
- Performance optimization
- Clear contract documentation

---

## MVC Controllers: Two Valid Approaches

### Approach 1: Direct Domain Objects (Current)

**When to Use**:
- ✅ Simple display pages
- ✅ Internal admin interfaces
- ✅ Rapid prototyping
- ✅ Templates closely aligned with domain
- ✅ Strong DDD culture in team

**Example**:
```java
@Controller
public class ProductPageController {

  @GetMapping("/products")
  public String showProducts(Model model) {
    model.addAttribute("products", productService.getAllProducts());
    return "product/catalog";
  }
}
```

```pug
each product in products
  h3= product.name().value()
  p= product.price().amount().amount() + " " + product.price().amount().currency().getSymbol()
```

**Pros**:
- ✅ Simple, no boilerplate
- ✅ Ubiquitous language in views
- ✅ DDD-aligned
- ✅ Quick to implement

**Cons**:
- ⚠️ Tight coupling to domain
- ⚠️ Complex template expressions
- ⚠️ Hard to test independently
- ⚠️ No view-specific logic

---

### Approach 2: ViewModels (Recommended for Complex Views)

**When to Use**:
- ✅ Complex view formatting
- ✅ Aggregating multiple domains
- ✅ View-specific calculations
- ✅ Forms with validation
- ✅ Large teams with separate frontend/backend developers

**Example**:
```java
// ViewModel (view-specific, different from DTO)
public record ProductViewModel(
    String id,
    String displayName,           // ← View-specific formatting
    String formattedPrice,        // ← "$29.99 USD" (pre-formatted)
    String shortDescription,      // ← Truncated for card view
    boolean isAvailable,          // ← Pre-calculated
    String availabilityBadge,     // ← "In Stock" / "Out of Stock"
    String detailUrl              // ← Computed URL
) {}

@Component
public class ProductViewModelConverter {

  public ProductViewModel toViewModel(Product product) {
    return new ProductViewModel(
        product.id().value(),
        product.name().value(),
        formatPrice(product.price()),                    // ← View formatting
        truncate(product.description().value(), 100),    // ← View logic
        product.isAvailable(),
        product.isAvailable() ? "In Stock" : "Out of Stock",
        "/products/" + product.id().value()
    );
  }

  private String formatPrice(Price price) {
    return String.format("%s %s",
        price.amount().amount(),
        price.amount().currency().getSymbol());
  }

  private String truncate(String text, int maxLength) {
    return text.length() > maxLength
        ? text.substring(0, maxLength) + "..."
        : text;
  }
}

@Controller
public class ProductPageController {

  private final ProductApplicationService productService;
  private final ProductViewModelConverter converter;  // ← ViewModel converter

  @GetMapping("/products")
  public String showProducts(Model model) {
    List<ProductViewModel> products = productService.getAllProducts()
        .stream()
        .map(converter::toViewModel)
        .toList();

    model.addAttribute("products", products);
    return "product/catalog";
  }
}
```

```pug
each product in products
  .product-card
    h3= product.displayName              // ← Simple property
    p= product.formattedPrice            // ← Pre-formatted
    p.description= product.shortDescription
    span(class=product.availabilityBadge)= product.availabilityBadge
    a(href=product.detailUrl) View Details
```

**Pros**:
- ✅ Clean templates (no complex expressions)
- ✅ View-specific formatting in Java (testable)
- ✅ Pre-calculated values
- ✅ Loosely coupled to domain
- ✅ Easier to test
- ✅ Better separation of concerns

**Cons**:
- ⚠️ More code (ViewModels + Converters)
- ⚠️ Less DDD-aligned (ubiquitous language hidden)
- ⚠️ Duplication between DTO and ViewModel

---

## Comparison Matrix

| Aspect | REST DTO | MVC Domain (Current) | MVC ViewModel |
|--------|----------|----------------------|---------------|
| **Purpose** | API contract | Display data | View presentation |
| **Audience** | External consumers | Server-side templates | Server-side templates |
| **Stability** | Must be stable | Can change freely | View-specific |
| **Coupling** | Decoupled from domain | Tightly coupled | Loosely coupled |
| **Security** | Critical (hide internals) | Not critical (server-side) | Not critical |
| **Formatting** | Raw data | Template does formatting | Pre-formatted |
| **Complexity** | Simple, flat | Domain structure exposed | View-optimized |
| **Testability** | Easy to test | Harder (need template engine) | Easy to test |
| **Boilerplate** | Medium | None | Medium |
| **Use Case** | API endpoints | Simple pages | Complex pages |
| **Recommendation** | ✅ Always | ✅ For simple views | ✅ For complex views |

---

## Recommendation

### For This Project

Given the current architecture and DDD focus, here's my recommendation:

#### REST Controllers (ProductResource)
**✅ Keep using DTOs** - This is correct and should not change.

```java
@RestController
public class ProductResource {
  private final ProductDtoConverter converter;  // ✅ KEEP
  // Always use DTOs for REST APIs
}
```

#### MVC Controllers (ProductPageController)
**⚠️ Hybrid Approach**: Use both patterns strategically

**For Simple List/Display Views**: Direct domain objects (current approach)
```java
@GetMapping("/products")
public String showProducts(Model model) {
  model.addAttribute("products", productService.getAllProducts());  // ✅ OK
  return "product/catalog";
}
```

**For Complex Views**: Introduce ViewModels
```java
@GetMapping("/products/{id}/detailed")
public String showDetailedProduct(@PathVariable String id, Model model) {
  ProductDetailViewModel viewModel = productService.findProductById(ProductId.of(id))
      .map(viewModelConverter::toDetailViewModel)  // ✅ Use ViewModel
      .orElseThrow();

  model.addAttribute("product", viewModel);
  return "product/detailed";
}
```

**For Forms**: Use Form DTOs
```java
public record CreateProductForm(
    @NotBlank String sku,
    @NotBlank String name,
    String description,
    @NotNull @Positive BigDecimal price,
    @NotBlank String currency,
    @NotNull @Min(0) Integer stock
) {}

@PostMapping("/products/new")
public String createProduct(@Valid CreateProductForm form, BindingResult result) {
  // ✅ Form DTO for input validation
}
```

---

## Implementation Examples

### Example 1: Simple Catalog (Current Approach - Keep As Is)

**Controller**:
```java
@GetMapping("/products")
public String showProductCatalog(Model model) {
  model.addAttribute("products", productService.getAllProducts());
  return "product/catalog";
}
```

**Template**:
```pug
each product in products
  .product-card
    h3= product.name().value()
    p= product.description().value()
```

**✅ This is fine** - Simple display, no complex formatting needed.

---

### Example 2: Complex Product Detail (Should Use ViewModel)

**Problem with Current Approach**:
```pug
// Too much logic in template
.product-detail
  h1= product.name().value()
  p.price= product.price().amount().amount() + " " + product.price().amount().currency().getSymbol()

  // Complex conditional logic
  if product.stock().quantity() > 10
    span.badge In Stock
  else if product.stock().quantity() > 0
    span.badge Low Stock
  else
    span.badge Out of Stock

  // URL construction in template
  a(href=`/products/${product.id().value()}/edit`) Edit

  // Formatting logic
  p= product.description().value().substring(0, 200) + "..."
```

**Better with ViewModel**:
```java
public record ProductDetailViewModel(
    String displayName,
    String formattedPrice,
    String stockBadge,
    String stockBadgeClass,
    String editUrl,
    String shortDescription,
    String fullDescription,
    boolean canAddToCart
) {}

@Component
public class ProductViewModelConverter {

  public ProductDetailViewModel toDetailViewModel(Product product) {
    return new ProductDetailViewModel(
        product.name().value(),
        formatPrice(product.price()),
        determineStockBadge(product.stock()),
        determineStockBadgeClass(product.stock()),
        "/products/" + product.id().value() + "/edit",
        truncate(product.description().value(), 200),
        product.description().value(),
        product.isAvailable() && product.stock().quantity() > 0
    );
  }

  private String formatPrice(Price price) {
    return String.format("%s %s",
        price.amount().amount(),
        price.amount().currency().getSymbol());
  }

  private String determineStockBadge(ProductStock stock) {
    if (stock.quantity() > 10) return "In Stock";
    if (stock.quantity() > 0) return "Low Stock";
    return "Out of Stock";
  }

  private String determineStockBadgeClass(ProductStock stock) {
    if (stock.quantity() > 10) return "badge-success";
    if (stock.quantity() > 0) return "badge-warning";
    return "badge-danger";
  }

  private String truncate(String text, int maxLength) {
    return text.length() > maxLength
        ? text.substring(0, maxLength) + "..."
        : text;
  }
}
```

**Clean Template**:
```pug
.product-detail
  h1= product.displayName
  p.price= product.formattedPrice
  span(class=product.stockBadgeClass)= product.stockBadge
  a(href=product.editUrl) Edit
  p.short= product.shortDescription
  if canAddToCart
    button.btn Add to Cart
```

**Benefits**:
- ✅ All formatting logic in Java (testable)
- ✅ Clean, simple templates
- ✅ Pre-calculated values
- ✅ View-specific concerns separated

---

### Example 3: Aggregating Multiple Domains (Definitely Use ViewModel)

**Scenario**: Dashboard showing product + sales stats + reviews

**ViewModel**:
```java
public record ProductDashboardViewModel(
    String productName,
    String formattedPrice,
    int totalSales,              // ← From sales domain
    BigDecimal revenue,          // ← Calculated
    double averageRating,        // ← From reviews domain
    int reviewCount,
    List<String> recentReviews,  // ← Aggregated
    String chartDataJson         // ← Pre-computed for charts
) {}

@Component
public class ProductDashboardViewModelConverter {

  private final SalesService salesService;
  private final ReviewService reviewService;

  public ProductDashboardViewModel toDashboardViewModel(Product product) {
    // Aggregate data from multiple domains
    var salesData = salesService.getSalesDataForProduct(product.id());
    var reviews = reviewService.getReviewsForProduct(product.id());

    return new ProductDashboardViewModel(
        product.name().value(),
        formatPrice(product.price()),
        salesData.totalSales(),
        calculateRevenue(salesData),
        reviews.averageRating(),
        reviews.count(),
        reviews.getRecent(5).stream().map(Review::getText).toList(),
        generateChartData(salesData)
    );
  }
}
```

**Template**:
```pug
.dashboard
  h1= product.productName
  .stats
    .stat
      span.value= product.totalSales
      span.label Total Sales
    .stat
      span.value= product.revenue
      span.label Revenue
    .stat
      span.value= product.averageRating
      span.label Average Rating

  .reviews
    each review in product.recentReviews
      p= review

  script.
    var chartData = !{product.chartDataJson};
    renderChart(chartData);
```

**✅ ViewModel is essential here** - Aggregating multiple domains, complex calculations.

---

## Summary & Decision Guide

### When to Use DTOs (REST Controllers)

**✅ ALWAYS** use DTOs in REST Controllers for:
- API stability
- Security
- Versioning
- Serialization control
- Clear contracts

### When to Use ViewModels (MVC Controllers)

Use ViewModels when:
- ✅ Complex view formatting required
- ✅ Aggregating multiple domains
- ✅ View-specific calculations
- ✅ Pre-computed values
- ✅ Large team (separation of concerns)
- ✅ Complex forms

Use Direct Domain when:
- ✅ Simple display pages
- ✅ Rapid prototyping
- ✅ Internal tools
- ✅ Strong DDD culture
- ✅ Minimal formatting needed

### Recommended Pattern for This Project

```
REST API (External)          MVC Pages (Internal)
─────────────────────        ────────────────────
ProductResource              ProductPageController
    ↓                            ↓
ProductDto (always)          Simple: Domain objects directly
ProductDtoConverter          Complex: ProductViewModel
                             Forms: ProductFormDto
```

### Action Items

1. **✅ Keep current approach** for simple catalog view
2. **Consider adding ViewModels** for:
   - Product detail page (complex formatting)
   - Product dashboard (aggregating domains)
   - Product forms (validation)
3. **Document the decision** - When to use each approach
4. **Create ViewModel convention** - Naming, location, testing

---

## Conclusion

**Direct Answer to Your Question**:

> "On ProductResource we use DtoConverters. Does it make sense to do the same on MVC Controllers?"

**Answer**: **It depends**:

1. **REST Controllers (ProductResource)**:
   - ✅ **YES, always use DTOs** - This is essential and correct

2. **MVC Controllers (ProductPageController)**:
   - ⚠️ **Both approaches are valid**, choose based on complexity:
     - **Simple views**: Direct domain objects (current approach is fine)
     - **Complex views**: ViewModels (recommended for maintainability)
     - **Forms**: Form DTOs (for validation)

**Current implementation is valid for a sample project**, but as complexity grows, introducing ViewModels for complex pages would improve:
- Testability
- Maintainability
- Separation of concerns
- Template simplicity

The key difference: REST DTOs are about **API contracts and stability**, while ViewModels are about **presentation logic and convenience**.

---

**Last Updated**: October 24, 2025
**Author**: AI Architecture Team
