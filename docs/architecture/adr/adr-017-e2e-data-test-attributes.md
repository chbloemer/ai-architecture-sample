# ADR-017: Data-Test Attributes for E2E Test Selectors

**Date**: January 31, 2026
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐

---

## Context

Current E2E tests use brittle selectors coupled to UI implementation details:

```java
// From CheckoutGuestE2ETest.java
page.locator("button:has-text('Add to Cart')").click();      // Breaks if label changes
page.locator("a:has-text('View Details')").click();          // Breaks on text change
waitForElement(".product-card");                              // Breaks on CSS refactoring
clickButton("Place Order");                                   // Couples test to UI text
page.locator("a:has-text('Back')").click();                  // Language/text dependent
```

**Problems:**
- UI text changes (labels, translations) break tests
- CSS class refactoring breaks tests
- Tests become maintenance burden
- No clear contract between templates and tests

---

## Decision

**All HTML elements interacted with in E2E tests MUST have a `data-test` attribute.**

### Naming Convention

Pattern: `{context}-{element}-{action}` (kebab-case)

| Element Type | Pattern | Example |
|--------------|---------|---------|
| Buttons | `{context}-{action}-button` | `product-add-to-cart-button` |
| Links | `{context}-{target}-link` | `cart-checkout-link` |
| Inputs | `{context}-{field}-input` | `buyer-email-input` |
| Containers | `{context}-{name}-container` | `product-card-container` |
| Forms | `{context}-{name}-form` | `buyer-info-form` |

### Examples by Feature

**Product Catalog:**
- `product-card-container`
- `product-view-details-link`
- `product-add-to-cart-button`
- `product-detail-container`

**Shopping Cart:**
- `cart-item-container`
- `cart-checkout-link`
- `cart-remove-item-button`

**Checkout:**
- `buyer-email-input`
- `buyer-info-form`
- `checkout-place-order-button`
- `checkout-back-link`
- `confirmation-message`

---

## Rationale

### 1. **Decouples Tests from UI Implementation**

**Before:**
```java
page.locator("button:has-text('Add to Cart')").click();
```

**After:**
```html
<button data-test="product-add-to-cart-button">Add to Cart</button>
```
```java
page.locator("[data-test='product-add-to-cart-button']").click();
```

Button text can change to "Add", "Buy Now", or translated text without breaking tests.

### 2. **Explicit Test Contract**

`data-test` attributes document which elements are part of the test API:
- Developers know not to remove/rename these without updating tests
- Code review can verify test attributes exist for testable elements

### 3. **Readable Test Code**

**Before:**
```java
page.locator(".product-card").first().locator("a:has-text('View Details')").click();
```

**After:**
```java
page.locator("[data-test='product-view-details-link']").first().click();
```

### 4. **No Runtime Impact**

`data-test` attributes:
- Are ignored by browsers
- Don't affect styling or behavior
- Can be stripped in production builds if needed

---

## Consequences

### Positive

✅ **UI text/styling can change** without breaking tests
✅ **Tests are more readable** with semantic selectors
✅ **Clear contract** between templates and tests
✅ **Language-independent** - works with translations
✅ **Refactoring-safe** - CSS changes don't break tests

### Neutral

⚠️ **Requires adding attributes** to HTML templates
⚠️ **Team education** needed on naming conventions

### Negative

❌ **Slight template verbosity** - minor HTML additions

---

## Implementation

### HTML Template Example

```html
<!-- Before -->
<div class="product-card">
  <a href="/products/1">View Details</a>
  <button>Add to Cart</button>
</div>

<!-- After -->
<div class="product-card" data-test="product-card-container">
  <a href="/products/1" data-test="product-view-details-link">View Details</a>
  <button data-test="product-add-to-cart-button">Add to Cart</button>
</div>
```

### Test Code Example

```java
// Before
page.locator(".product-card").first().locator("a:has-text('View Details')").click();
waitForElement(".product-detail");
page.locator("button:has-text('Add to Cart')").click();

// After
page.locator("[data-test='product-view-details-link']").first().click();
waitForElement("[data-test='product-detail-container']");
page.locator("[data-test='product-add-to-cart-button']").click();
```

### BaseE2ETest Helper Methods

Consider adding helper methods:

```java
protected void clickTestElement(String dataTestValue) {
    page.locator("[data-test='" + dataTestValue + "']").click();
}

protected void waitForTestElement(String dataTestValue) {
    page.locator("[data-test='" + dataTestValue + "']").waitFor();
}
```

---

## Migration Strategy

1. **New tests**: Must use `data-test` selectors
2. **Existing tests**: Migrate when touching related code
3. **Templates**: Add `data-test` attributes when editing

---

## References

- [Testing Library - Which Query Should I Use](https://testing-library.com/docs/queries/about/#priority)
- [Playwright Best Practices](https://playwright.dev/docs/best-practices#use-locators)
- [Google Testing Blog - Just Say No to More End-to-End Tests](https://testing.googleblog.com/2015/04/just-say-no-to-more-end-to-end-tests.html)

### Related ADRs

- ADR-015: ArchUnit for Architecture Governance (automated enforcement principle)

---

**Approved by**: Architecture Team
**Date**: January 31, 2026
**Version**: 1.0
