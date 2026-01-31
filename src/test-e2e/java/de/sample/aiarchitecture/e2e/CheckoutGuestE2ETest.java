package de.sample.aiarchitecture.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E tests for the guest checkout flow.
 *
 * <p>Tests the complete checkout process for a guest user (not logged in):
 * <ul>
 *   <li>Adding products to cart</li>
 *   <li>Starting checkout</li>
 *   <li>Entering buyer information</li>
 *   <li>Entering delivery information</li>
 *   <li>Selecting payment method</li>
 *   <li>Reviewing and confirming order</li>
 * </ul>
 *
 * <p>Prerequisites:
 * <ul>
 *   <li>Application running at configured base URL</li>
 *   <li>Sample products loaded in database</li>
 * </ul>
 */
@DisplayName("Guest Checkout E2E Tests")
class CheckoutGuestE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Complete checkout flow as guest user")
  void completeGuestCheckoutFlow() {
    // Step 1: Navigate to product list and add a product to cart
    navigateTo("/products");
    waitForElement(".product-card");

    // Click "Add to Cart" on the first product
    page.locator(".product-card").first().locator("button:has-text('Add to Cart')").click();

    // Step 2: Navigate to cart and verify product is added
    navigateTo("/cart");
    waitForElement(".cart-item");
    assertTrue(elementExists(".cart-item"), "Cart should have at least one item");

    // Step 3: Start checkout
    clickButton("Checkout");
    waitForUrl("/checkout/buyer");

    // Step 4: Fill buyer information
    fillByName("email", "guest@example.com");
    fillByName("firstName", "Test");
    fillByName("lastName", "Guest");
    fillByName("phone", "+1-555-0100");
    submitForm("form");

    // Step 5: Wait for delivery page and fill delivery information
    waitForUrl("/checkout/delivery");
    fillByName("streetLine1", "123 Main Street");
    fillByName("city", "Springfield");
    fillByName("postalCode", "12345");
    fillByName("country", "United States");
    fillByName("state", "IL");

    // Select shipping option if available
    if (elementExists("select[name=\"shippingOptionId\"]")) {
      selectByName("shippingOptionId", "STANDARD");
    }
    submitForm("form");

    // Step 6: Wait for payment page and select payment method
    waitForUrl("/checkout/payment");
    // Select mock payment provider for testing
    if (elementExists("input[name=\"paymentProviderId\"]")) {
      page.locator("input[name=\"paymentProviderId\"][value=\"mock\"]").check();
    }
    submitForm("form");

    // Step 7: Wait for review page and verify order details
    waitForUrl("/checkout/review");
    assertTrue(pageContains("guest@example.com"), "Review page should show buyer email");
    assertTrue(pageContains("123 Main Street"), "Review page should show delivery address");

    // Step 8: Confirm order
    clickButton("Confirm Order");

    // Step 9: Verify confirmation page
    waitForUrl("/checkout/confirmation");
    assertTrue(pageContains("Thank you") || pageContains("confirmed") || pageContains("Order"),
        "Confirmation page should show success message");
  }

  @Test
  @DisplayName("Checkout should redirect to cart if cart is empty")
  void checkoutWithEmptyCartRedirectsToCart() {
    // Try to access checkout directly without items in cart
    navigateTo("/checkout/start");

    // Should redirect to cart with error message
    waitForUrl("/cart**");
    assertTrue(getCurrentPath().startsWith("/cart"), "Should redirect to cart page");
  }

  @Test
  @DisplayName("Buyer info validation shows errors for invalid input")
  void buyerInfoValidationShowsErrors() {
    // Add a product to cart first
    addProductToCart();

    // Start checkout
    navigateTo("/checkout/start");
    waitForUrl("/checkout/buyer");

    // Submit form with invalid email
    fillByName("email", "invalid-email");
    fillByName("firstName", "Test");
    fillByName("lastName", "User");
    submitForm("form");

    // Should show validation error
    assertTrue(elementExists(".error") || pageContains("valid email"),
        "Should show email validation error");

    // Should stay on buyer info page
    assertTrue(getCurrentPath().contains("/checkout/buyer"),
        "Should stay on buyer info page on validation error");
  }

  @Test
  @DisplayName("Can navigate back through checkout steps")
  void canNavigateBackThroughCheckoutSteps() {
    // Complete buyer info
    addProductToCart();
    navigateTo("/checkout/start");
    waitForUrl("/checkout/buyer");
    fillBuyerInfo();
    submitForm("form");

    // On delivery page, click back
    waitForUrl("/checkout/delivery");
    if (elementExists("a:has-text('Back')")) {
      clickLink("Back");
      waitForUrl("/checkout/buyer");
      assertTrue(getCurrentPath().contains("/checkout/buyer"),
          "Should navigate back to buyer info");
    }
  }

  /**
   * Helper method to add a product to the cart.
   */
  private void addProductToCart() {
    navigateTo("/products");
    waitForElement(".product-card");
    page.locator(".product-card").first().locator("button:has-text('Add to Cart')").click();
    // Wait briefly for cart to update
    page.waitForTimeout(500);
  }

  /**
   * Helper method to fill buyer information form.
   */
  private void fillBuyerInfo() {
    fillByName("email", "guest@example.com");
    fillByName("firstName", "Test");
    fillByName("lastName", "Guest");
    fillByName("phone", "+1-555-0100");
  }
}
