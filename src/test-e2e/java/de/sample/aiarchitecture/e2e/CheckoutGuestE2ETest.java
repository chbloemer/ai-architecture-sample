package de.sample.aiarchitecture.e2e;

import de.sample.aiarchitecture.e2e.pages.*;
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
    // Step 1: Navigate to product catalog and add product to cart
    ProductCatalogPage catalog = ProductCatalogPage.navigateTo(page);
    ProductDetailPage detail = catalog.viewFirstProduct();
    detail.addToCart();

    // Step 2: Navigate to cart and verify product is added
    CartPage cart = CartPage.navigateTo(page);
    assertTrue(cart.hasItems(), "Cart should have at least one item");

    // Step 3: Start checkout and fill buyer information
    BuyerInfoPage buyer = cart.proceedToCheckout();
    DeliveryPage delivery = buyer
        .fillBuyerInfo("guest@example.com", "Test", "Guest", "+1-555-0100")
        .continueToDelivery();

    // Step 4: Fill delivery information
    PaymentPage payment = delivery
        .fillAddress("123 Main Street", "Springfield", "12345", "United States", "IL")
        .selectFirstShippingOption()
        .continueToPayment();

    // Step 5: Select payment method
    ReviewPage review = payment
        .selectFirstPaymentProvider()
        .continueToReview();

    // Step 6: Verify order details and place order
    assertTrue(review.showsEmail("guest@example.com"), "Review page should show buyer email");
    assertTrue(review.showsAddress("123 Main Street"), "Review page should show delivery address");

    ConfirmationPage confirmation = review.placeOrder();

    // Step 7: Verify confirmation
    assertTrue(confirmation.isOrderConfirmed(), "Confirmation page should show success message");
  }

  @Test
  @DisplayName("Checkout should redirect to cart if no active session")
  void checkoutWithNoActiveSessionRedirectsToCart() {
    // Try to access buyer info page directly without starting checkout
    navigateTo("/checkout/buyer");

    // Should redirect to cart with error message (no active checkout session)
    waitForUrl("/cart**");
    assertTrue(getCurrentPath().startsWith("/cart"), "Should redirect to cart page");
  }

  @Test
  @DisplayName("Buyer info validation shows errors for invalid input")
  void buyerInfoValidationShowsErrors() {
    // Add a product to cart and start checkout properly
    ProductCatalogPage catalog = ProductCatalogPage.navigateTo(page);
    ProductDetailPage detail = catalog.viewFirstProduct();
    detail.addToCart();

    // Go to cart and start checkout properly (with cartId parameter)
    CartPage cart = CartPage.navigateTo(page);
    BuyerInfoPage buyer = cart.proceedToCheckout();

    // Submit form with invalid email
    buyer.fillBuyerInfo("invalid-email", "Test", "User", "")
        .submitWithErrors();

    // Should show validation error and stay on buyer info page
    assertTrue(buyer.hasValidationErrors(), "Should show email validation error");
    assertTrue(buyer.isOnPage(), "Should stay on buyer info page on validation error");
  }

  @Test
  @DisplayName("Can navigate back through checkout steps")
  void canNavigateBackThroughCheckoutSteps() {
    // Add product to cart and start checkout properly
    ProductCatalogPage catalog = ProductCatalogPage.navigateTo(page);
    ProductDetailPage detail = catalog.viewFirstProduct();
    detail.addToCart();

    // Go to cart and start checkout properly (with cartId parameter)
    CartPage cart = CartPage.navigateTo(page);
    BuyerInfoPage buyer = cart.proceedToCheckout();

    // Fill buyer info and continue
    DeliveryPage delivery = buyer
        .fillBuyerInfo("guest@example.com", "Test", "Guest", "+1-555-0100")
        .continueToDelivery();

    // On delivery page, click back if available
    if (delivery.hasBackLink()) {
      BuyerInfoPage backToBuyer = delivery.goBack();
      assertTrue(backToBuyer.isOnPage(), "Should navigate back to buyer info");
    }
  }

}
