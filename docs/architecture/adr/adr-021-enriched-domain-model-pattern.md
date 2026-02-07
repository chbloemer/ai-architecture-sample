# ADR-021: Enriched Domain Model Pattern

**Date**: February 7, 2026
**Status**: Accepted
**Deciders**: Architecture Team

---

## Context

The application needs to display data that combines aggregate state with information from other bounded contexts (e.g., product details with current pricing from Pricing context and stock levels from Inventory context). The Interest Interface Pattern (State Mediator) was initially considered but added significant complexity (~500+ lines per aggregate) without clear benefit for cross-context enrichment scenarios.

### The Problem

Aggregates own their core state, but read operations often need data from multiple contexts:

```
Product aggregate: identity, name, description, category
Pricing context:   current price
Inventory context: stock level, availability
```

A direct aggregate getter approach would require the aggregate to know about external concepts, violating bounded context isolation.

---

## Decision

**Use Enriched Domain Model records that combine aggregate state with cross-context data via static factory methods.**

Enriched models are immutable records implementing `Value`, located in `{context}.domain.model/` alongside the aggregate they enrich. They own cross-context business rules that require data from multiple contexts.

### Implementation

```java
// product/domain/model/EnrichedProduct.java
public record EnrichedProduct(
    ProductId productId, String sku, String name, String description,
    String category, Money currentPrice, int stockQuantity, boolean isAvailable
) implements Value {

  // Factory method combines aggregate + external data
  public static EnrichedProduct from(Product product, ProductArticle article) {
    return new EnrichedProduct(
        product.id(), product.sku().value(), product.name().value(),
        product.description().value(), product.category().name(),
        article.currentPrice(), article.stockQuantity(), article.isAvailable());
  }

  // Cross-context business rules
  public boolean canPurchase() { return isAvailable && isInStock(); }
  public boolean hasStockFor(int qty) { return stockQuantity >= qty; }
}
```

### Responsibility Split

| Concern | Owner |
|---------|-------|
| Identity, mutations, invariants | Aggregate (e.g., `Product`) |
| Cross-context business rules | Enriched Model (e.g., `EnrichedProduct`) |
| Presentation formatting | ViewModel (e.g., `ProductDetailPageViewModel`) |

### Current Enriched Models

- `product.domain.model.EnrichedProduct` -- Product + pricing + stock
- `cart.domain.model.EnrichedCart` -- Cart + current article prices + availability
- `cart.domain.model.EnrichedCartItem` -- Cart item + current vs original price

---

## Consequences

### Positive

- Simple pattern: ~50 lines per enriched model vs ~500+ for Interest Interface
- Cross-context business rules have a clear home
- Immutable records are easy to test
- Clean separation: aggregate owns mutations, enriched model owns evaluation

### Negative

- Enriched model duplicates some aggregate field values (denormalization)
- Factory method creates coupling to aggregate's accessor API

---

## Related ADRs

- [ADR-009: Value Objects as Java Records](adr-009-value-objects-as-records.md) -- Enriched models are records implementing `Value`
- [ADR-019: Open Host Service Pattern](adr-019-open-host-service-pattern.md) -- External data flows through OHS to enriched models
- [ADR-022: ViewModel Pattern](adr-022-viewmodel-pattern.md) -- ViewModels consume enriched models for presentation
