# DTOs vs ViewModels: REST vs MVC Analysis

**Question:** Should MVC Controllers use DTOs/Converters like REST Controllers?

**Answer:**
- ✅ **REST Controllers**: Always use DTOs (current approach is correct)
- ⚠️ **MVC Controllers**: Current approach (direct domain) is valid, but ViewModels offer benefits

---

## Current Implementation

### REST Controller - Uses DTOs ✅

```java
@RestController
@RequestMapping("/api/products")
public class ProductResource {
  private final ProductApplicationService productService;
  private final ProductDtoConverter converter;

  @GetMapping
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    return ResponseEntity.ok(
      productService.getAllProducts().stream()
        .map(converter::toDto)
        .toList()
    );
  }
}
```

**Why DTOs?**
- Stable API contract independent of domain
- Flattened structure for JSON serialization
- Versioning capability
- Security (hide internal details)

### MVC Controller - Uses Domain Objects Directly

```java
@Controller
@RequestMapping("/products")
public class ProductPageController {
  private final ProductApplicationService productService;

  @GetMapping
  public String showProductCatalog(final Model model) {
    model.addAttribute("products", productService.getAllProducts());  // Direct domain
    return "product/catalog";
  }
}
```

**Pug Template:**
```pug
each product in products
  h3= product.name().value()  // Calls domain methods
  p= product.price().amount().amount()
```

---

## Comparison

| Aspect | REST (DTOs) | MVC (Domain Objects) | MVC (ViewModels) |
|--------|------------|---------------------|------------------|
| **Contract Stability** | High - DTOs buffer changes | Low - template breaks if domain changes | High - ViewModels buffer |
| **Complexity** | Medium - needs converters | Low - no conversion | Medium - needs converters |
| **Template Clarity** | High - simple properties | Low - nested method calls | High - simple properties |
| **Domain Exposure** | None - completely hidden | High - methods exposed to view | None - hidden behind ViewModel |
| **Use Case** | External APIs | Internal admin UIs | Customer-facing web apps |

---

## Recommendation

**REST Controllers (External APIs):**
Always use DTOs - non-negotiable for stable external contracts.

**MVC Controllers (Internal UIs):**
- **Small/Internal Apps**: Direct domain objects OK (less code, faster development)
- **Customer-Facing Apps**: Use ViewModels (better separation, stability)

**Current Implementation:** Valid for sample project - demonstrates both approaches.

---

## Related Documentation

- [Architecture Principles](architecture-principles.md) - Hexagonal Architecture patterns
- [Pug4j Integration](../integrations/pug4j-integration.md) - Template usage
