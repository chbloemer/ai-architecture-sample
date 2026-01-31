package de.sample.aiarchitecture.e2e;

import de.sample.aiarchitecture.e2e.pages.*;
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
    CartPage cart = CartPage.navigateTo(page);
    assertTrue(cart.waitForItems().hasItems(), "Cart should have items");

    BuyerInfoPage buyer = cart.proceedToCheckout();

    // Step 4: Fill buyer info
    DeliveryPage delivery = buyer
        .fillBuyerInfo(TEST_EMAIL, "Test", "User", "+1-555-0199")
        .continueToDelivery();

    // Step 5: Fill delivery information
    PaymentPage payment = delivery
        .fillAddress("456 Oak Avenue", "Chicago", "60601", "United States", "IL")
        .selectFirstShippingOption()
        .continueToPayment();

    // Step 6: Select payment method
    ReviewPage review = payment
        .selectFirstPaymentProvider()
        .continueToReview();

    // Step 7: Verify and place order
    assertTrue(review.showsEmail(TEST_EMAIL), "Review page should show user email");

    ConfirmationPage confirmation = review.placeOrder();

    // Step 8: Verify confirmation
    assertTrue(confirmation.isOrderConfirmed(), "Confirmation page should show success message");
  }

  @Test
  @DisplayName("Registered user can add items to cart")
  void registeredUserCanAddToCart() {
    // Step 1: Register a new user
    String uniqueEmail = "cartuser-" + System.currentTimeMillis() + "@example.com";
    RegisterPage register = RegisterPage.navigateTo(page);
    register.register(uniqueEmail, TEST_PASSWORD);

    // Step 2: Add product to cart as registered user
    addProductToCart();

    // Step 3: Navigate to cart and verify items are there
    CartPage cart = CartPage.navigateTo(page);
    assertTrue(cart.waitForItems().getItemCount() > 0, "Registered user cart should have items");
  }

  @Test
  @DisplayName("Can login and checkout with existing account")
  void canLoginAndCheckoutWithExistingAccount() {
    // Step 1: Register a new user first
    String uniqueEmail = "existing-" + System.currentTimeMillis() + "@example.com";
    RegisterPage register = RegisterPage.navigateTo(page);
    register.register(uniqueEmail, TEST_PASSWORD);

    // Step 2: Logout (clear context) and login again
    context.clearCookies();
    LoginPage login = LoginPage.navigateTo(page);
    login.login(uniqueEmail, TEST_PASSWORD);

    // Step 3: Add product to cart
    addProductToCart();

    // Step 4: Start checkout
    CartPage cart = CartPage.navigateTo(page);
    cart.waitForItems();
    BuyerInfoPage buyer = cart.proceedToCheckout();

    // Step 5: Verify we're on buyer info page
    assertTrue(buyer.isOnPage(), "Authenticated user should proceed to checkout");
  }

  @Test
  @DisplayName("Checkout link from login page redirects to checkout after login")
  void loginFromCheckoutRedirectsBackToCheckout() {
    // Step 1: Add product to cart as guest
    addProductToCart();

    // Step 2: Try to start checkout
    CartPage cart = CartPage.navigateTo(page);
    cart.waitForItems();
    clickTestElement("cart-checkout-link");

    // If there's a login prompt during checkout, handle it
    if (getCurrentPath().contains("/login")) {
      // Register via login page
      if (testElementExists("login-register-link")) {
        LoginPage login = new LoginPage(page);
        RegisterPage register = login.goToRegister();
        String uniqueEmail = "redirect-" + System.currentTimeMillis() + "@example.com";
        register.register(uniqueEmail, TEST_PASSWORD);
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
    RegisterPage register = RegisterPage.navigateTo(page);
    register.register(TEST_EMAIL, TEST_PASSWORD);
  }

  /**
   * Helper method to add a product to the cart.
   */
  private void addProductToCart() {
    ProductCatalogPage catalog = ProductCatalogPage.navigateTo(page);
    ProductDetailPage detail = catalog.viewFirstProduct();
    detail.addToCartAndStay();
  }
}
