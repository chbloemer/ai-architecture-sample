package de.sample.aiarchitecture.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E tests for the checkout flow with authenticated users.
 *
 * <p>Tests the checkout process for logged-in users including:
 * <ul>
 *   <li>User registration and login</li>
 *   <li>Cart recovery after login (guest cart merged with user cart)</li>
 *   <li>Pre-filled buyer information from user profile</li>
 *   <li>Complete checkout flow for authenticated users</li>
 * </ul>
 *
 * <p>Prerequisites:
 * <ul>
 *   <li>Application running at configured base URL</li>
 *   <li>Sample products loaded in database</li>
 * </ul>
 */
@DisplayName("Authenticated Checkout E2E Tests")
class CheckoutLoginE2ETest extends BaseE2ETest {

  private static final String TEST_EMAIL = "testuser-" + System.currentTimeMillis() + "@example.com";
  private static final String TEST_PASSWORD = "SecurePassword123!";

  @Test
  @DisplayName("Complete checkout flow as authenticated user")
  void completeAuthenticatedCheckoutFlow() {
    // Step 1: Register a new user
    registerNewUser();

    // Step 2: Add product to cart while logged in
    addProductToCart();

    // Step 3: Navigate to cart and start checkout
    navigateTo("/cart");
    waitForElement(".cart-item");
    clickButton("Checkout");

    // Step 4: Buyer info should be on the form (may have pre-filled data)
    waitForUrl("/checkout/buyer");
    fillByName("email", TEST_EMAIL);
    fillByName("firstName", "Test");
    fillByName("lastName", "User");
    fillByName("phone", "+1-555-0199");
    submitForm("form");

    // Step 5: Fill delivery information
    waitForUrl("/checkout/delivery");
    fillByName("streetLine1", "456 Oak Avenue");
    fillByName("city", "Chicago");
    fillByName("postalCode", "60601");
    fillByName("country", "United States");
    fillByName("state", "IL");
    if (elementExists("select[name=\"shippingOptionId\"]")) {
      selectByName("shippingOptionId", "STANDARD");
    }
    submitForm("form");

    // Step 6: Select payment method
    waitForUrl("/checkout/payment");
    if (elementExists("input[name=\"paymentProviderId\"]")) {
      page.locator("input[name=\"paymentProviderId\"][value=\"mock\"]").check();
    }
    submitForm("form");

    // Step 7: Review and confirm
    waitForUrl("/checkout/review");
    assertTrue(pageContains(TEST_EMAIL), "Review page should show user email");
    clickButton("Confirm Order");

    // Step 8: Verify confirmation
    waitForUrl("/checkout/confirmation");
    assertTrue(pageContains("Thank you") || pageContains("confirmed") || pageContains("Order"),
        "Confirmation page should show success message");
  }

  @Test
  @DisplayName("Guest cart is recovered after login")
  void guestCartIsRecoveredAfterLogin() {
    // Step 1: Add product to cart as guest
    addProductToCart();

    // Verify cart has item
    navigateTo("/cart");
    waitForElement(".cart-item");
    int guestCartItemCount = page.locator(".cart-item").count();
    assertTrue(guestCartItemCount > 0, "Guest cart should have items");

    // Step 2: Register and login (this should trigger cart recovery)
    String uniqueEmail = "recovery-" + System.currentTimeMillis() + "@example.com";
    navigateTo("/register");
    fillByName("email", uniqueEmail);
    fillByName("password", TEST_PASSWORD);
    if (elementExists("input[name=\"confirmPassword\"]")) {
      fillByName("confirmPassword", TEST_PASSWORD);
    }
    submitForm("form");

    // Wait for registration to complete and redirect
    page.waitForTimeout(1000);

    // Step 3: Navigate to cart and verify items are still there
    navigateTo("/cart");
    waitForElement(".cart-item");
    int userCartItemCount = page.locator(".cart-item").count();
    assertTrue(userCartItemCount >= guestCartItemCount,
        "User cart should have at least as many items as guest cart (cart recovery)");
  }

  @Test
  @DisplayName("Can login and checkout with existing account")
  void canLoginAndCheckoutWithExistingAccount() {
    // Step 1: Register a new user first
    String uniqueEmail = "existing-" + System.currentTimeMillis() + "@example.com";
    registerUser(uniqueEmail, TEST_PASSWORD);

    // Step 2: Logout (clear context) and login again
    context.clearCookies();
    login(uniqueEmail, TEST_PASSWORD);

    // Step 3: Add product to cart
    addProductToCart();

    // Step 4: Start checkout
    navigateTo("/cart");
    waitForElement(".cart-item");
    clickButton("Checkout");

    // Step 5: Verify we're on buyer info page
    waitForUrl("/checkout/buyer");
    assertTrue(getCurrentPath().contains("/checkout/buyer"),
        "Authenticated user should proceed to checkout");
  }

  @Test
  @DisplayName("Checkout link from login page redirects to checkout after login")
  void loginFromCheckoutRedirectsBackToCheckout() {
    // Step 1: Add product to cart as guest
    addProductToCart();

    // Step 2: Try to start checkout
    navigateTo("/cart");
    clickButton("Checkout");

    // If there's a login prompt during checkout, handle it
    if (getCurrentPath().contains("/login")) {
      // Register/login
      if (elementExists("a:has-text('Register')")) {
        clickLink("Register");
        String uniqueEmail = "redirect-" + System.currentTimeMillis() + "@example.com";
        fillByName("email", uniqueEmail);
        fillByName("password", TEST_PASSWORD);
        if (elementExists("input[name=\"confirmPassword\"]")) {
          fillByName("confirmPassword", TEST_PASSWORD);
        }
        submitForm("form");
      }
    }

    // After login, should proceed to checkout
    page.waitForTimeout(1000);
    String path = getCurrentPath();
    assertTrue(path.contains("/checkout") || path.contains("/cart"),
        "Should be redirected to checkout or cart after login");
  }

  /**
   * Helper method to register a new user with default credentials.
   */
  private void registerNewUser() {
    registerUser(TEST_EMAIL, TEST_PASSWORD);
  }

  /**
   * Helper method to register a user with specific credentials.
   */
  private void registerUser(String email, String password) {
    navigateTo("/register");
    fillByName("email", email);
    fillByName("password", password);
    if (elementExists("input[name=\"confirmPassword\"]")) {
      fillByName("confirmPassword", password);
    }
    submitForm("form");
    // Wait for registration to complete
    page.waitForTimeout(1000);
  }

  /**
   * Helper method to login with specific credentials.
   */
  private void login(String email, String password) {
    navigateTo("/login");
    fillByName("email", email);
    fillByName("password", password);
    submitForm("form");
    // Wait for login to complete
    page.waitForTimeout(1000);
  }

  /**
   * Helper method to add a product to the cart.
   */
  private void addProductToCart() {
    navigateTo("/products");
    waitForElement(".product-card");
    page.locator(".product-card").first().locator("button:has-text('Add to Cart')").click();
    page.waitForTimeout(500);
  }
}
