# ADR-018: Page Object Pattern for E2E Tests

**Date**: January 31, 2026
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐

---

## Context

E2E tests using Playwright interact directly with page elements through selectors scattered across test classes:

```java
// From CheckoutGuestE2ETest.java (before refactoring)
clickTestElement("product-view-details-link");
waitForTestElement("product-detail");
clickTestElement("product-add-to-cart-button");
navigateTo("/cart");
waitForTestElement("cart-item");
clickTestElement("cart-checkout-link");
fillByName("email", "guest@example.com");
fillByName("firstName", "Test");
// ... 20+ more lines of low-level interactions
```

**Problems:**
- **67 data-test selectors** scattered as magic strings across test files
- **Duplicated helper methods** (`addProductToCart()`, `fillBuyerInfo()`) in multiple test classes
- **Leaked Playwright complexity** (e.g., `page.locator("[data-test='...']").first().check()`)
- **Low readability** - tests mix navigation logic with business assertions
- **High maintenance** - selector changes require updates in multiple locations

---

## Decision

**All E2E tests MUST use Page Objects to encapsulate page-specific selectors and interactions.**

### Page Object Structure

```
src/test-e2e/java/de/sample/aiarchitecture/e2e/pages/
├── BasePage.java              # Common methods for all pages
├── ProductCatalogPage.java    # Product listing interactions
├── ProductDetailPage.java     # Product detail and add to cart
├── CartPage.java              # Shopping cart operations
├── BuyerInfoPage.java         # Checkout: buyer information
├── DeliveryPage.java          # Checkout: delivery address
├── PaymentPage.java           # Checkout: payment selection
├── ReviewPage.java            # Checkout: order review
├── ConfirmationPage.java      # Order confirmation
├── LoginPage.java             # User login
└── RegisterPage.java          # User registration
```

### Design Rules

1. **One page object per page/view** - Each distinct page gets its own class
2. **Fluent API** - Navigation methods return the target page object
3. **URL validation** - Constructors validate expected URL pattern (fail-fast)
4. **Centralized selectors** - All `data-test` values are private constants
5. **No assertions in page objects** - Page objects return data; tests assert

---

## Rationale

### 1. **Single Point of Change**

Selectors are defined once:

```java
public class BuyerInfoPage extends BasePage {
    private static final String CONTINUE_BUTTON = "buyer-continue-button";
    private static final String EMAIL_FIELD = "email";
    // ...
}
```

If `buyer-continue-button` changes, update one constant instead of every test.

### 2. **Readable Test Code**

**Before:**
```java
clickTestElement("product-view-details-link");
waitForTestElement("product-detail");
clickTestElement("product-add-to-cart-button");
navigateTo("/cart");
waitForTestElement("cart-item");
clickTestElement("cart-checkout-link");
waitForUrl("/checkout/buyer");
fillByName("email", "guest@example.com");
fillByName("firstName", "Test");
fillByName("lastName", "Guest");
fillByName("phone", "+1-555-0100");
clickTestElement("buyer-continue-button");
```

**After:**
```java
ProductCatalogPage catalog = ProductCatalogPage.navigateTo(page);
ProductDetailPage detail = catalog.viewFirstProduct();
detail.addToCart();

CartPage cart = CartPage.navigateTo(page);
BuyerInfoPage buyer = cart.proceedToCheckout();
buyer.fillBuyerInfo("guest@example.com", "Test", "Guest", "+1-555-0100")
     .continueToDelivery();
```

Tests read like business workflows, not Playwright scripts.

### 3. **Fluent API Enables Chaining**

```java
DeliveryPage delivery = buyer
    .fillBuyerInfo("guest@example.com", "Test", "Guest", "+1-555-0100")
    .continueToDelivery();

PaymentPage payment = delivery
    .fillAddress("123 Main St", "Springfield", "12345", "US", "IL")
    .selectFirstShippingOption()
    .continueToPayment();
```

Navigation returns the expected next page, catching navigation errors early.

### 4. **Eliminates Duplication**

**Before:** `addProductToCart()` helper duplicated in both test classes.

**After:** `ProductCatalogPage.viewFirstProduct().addToCart()` - reusable across all tests.

---

## Consequences

### Positive

✅ **Selectors centralized** - One change location per element
✅ **Tests are shorter** - 173 → 128 lines in CheckoutGuestE2ETest
✅ **Business-readable** - Tests describe user journeys, not DOM manipulation
✅ **Type-safe navigation** - Compiler catches invalid page transitions
✅ **Reusable components** - Page objects shared across test classes

### Neutral

⚠️ **More files** - 11 new page object classes
⚠️ **Learning curve** - Team needs to understand page object pattern

### Negative

❌ **Initial overhead** - Creating page objects takes time upfront
❌ **Maintenance of page objects** - New pages require new classes

---

## Implementation

### BasePage.java

```java
public abstract class BasePage {
    protected static final String BASE_URL = System.getProperty("e2e.baseUrl", "http://localhost:8080");
    protected final Page page;

    protected BasePage(Page page, String expectedUrlPattern) {
        this.page = page;
        page.waitForURL(BASE_URL + expectedUrlPattern);
    }

    protected void click(String dataTest) {
        page.locator("[data-test='" + dataTest + "']").click();
    }

    protected void fill(String name, String value) {
        page.locator("input[name='" + name + "']").fill(value);
    }

    protected boolean exists(String dataTest) {
        return page.locator("[data-test='" + dataTest + "']").count() > 0;
    }

    // ... other common methods
}
```

### Example Page Object

```java
public class BuyerInfoPage extends BasePage {
    private static final String URL_PATTERN = "/checkout/buyer";
    private static final String CONTINUE_BUTTON = "buyer-continue-button";

    public BuyerInfoPage(Page page) {
        super(page, URL_PATTERN);
    }

    public BuyerInfoPage fillBuyerInfo(String email, String firstName,
                                        String lastName, String phone) {
        fill("email", email);
        fill("firstName", firstName);
        fill("lastName", lastName);
        fill("phone", phone);
        return this;
    }

    public DeliveryPage continueToDelivery() {
        click(CONTINUE_BUTTON);
        return new DeliveryPage(page);
    }
}
```

### Test Usage

```java
@Test
void completeGuestCheckoutFlow() {
    ProductCatalogPage catalog = ProductCatalogPage.navigateTo(page);
    ProductDetailPage detail = catalog.viewFirstProduct();
    detail.addToCart();

    CartPage cart = CartPage.navigateTo(page);
    BuyerInfoPage buyer = cart.proceedToCheckout();

    DeliveryPage delivery = buyer
        .fillBuyerInfo("guest@example.com", "Test", "Guest", "+1-555-0100")
        .continueToDelivery();

    PaymentPage payment = delivery
        .fillAddress("123 Main St", "Springfield", "12345", "US", "IL")
        .selectFirstShippingOption()
        .continueToPayment();

    ReviewPage review = payment
        .selectFirstPaymentProvider()
        .continueToReview();

    assertTrue(review.showsEmail("guest@example.com"));

    ConfirmationPage confirmation = review.placeOrder();
    assertTrue(confirmation.isOrderConfirmed());
}
```

---

## Alternatives Considered

### 1. Keep Helpers in Test Classes

**Rejected:** Leads to duplication across test classes and doesn't encapsulate selectors.

### 2. Screenplay Pattern

**Rejected:** More complex abstraction (actors, tasks, questions) - overkill for current test scope. Can migrate later if needed.

### 3. Component Objects Only

**Rejected:** Would still require managing navigation logic in tests. Full page objects provide clearer structure.

---

## References

- [Martin Fowler - Page Object](https://martinfowler.com/bliki/PageObject.html)
- [Selenium - Page Object Models](https://www.selenium.dev/documentation/test_practices/encouraged/page_object_models/)
- [Playwright - Page Object Model](https://playwright.dev/docs/pom)

### Related ADRs

- [ADR-017: Data-Test Attributes for E2E Test Selectors](adr-017-e2e-data-test-attributes.md) - Selector naming convention

---

**Approved by**: Architecture Team
**Date**: January 31, 2026
**Version**: 1.0
