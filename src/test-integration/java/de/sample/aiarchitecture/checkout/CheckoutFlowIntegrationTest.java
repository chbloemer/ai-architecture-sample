package de.sample.aiarchitecture.checkout;

import de.sample.aiarchitecture.cart.application.additemtocart.AddItemToCartCommand;
import de.sample.aiarchitecture.cart.application.additemtocart.AddItemToCartUseCase;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdQuery;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdResult;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdUseCase;
import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartCommand;
import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartResult;
import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartUseCase;
import de.sample.aiarchitecture.checkout.application.confirmcheckout.ConfirmCheckoutCommand;
import de.sample.aiarchitecture.checkout.application.confirmcheckout.ConfirmCheckoutInputPort;
import de.sample.aiarchitecture.checkout.application.confirmcheckout.ConfirmCheckoutResult;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResult;
import de.sample.aiarchitecture.checkout.application.startcheckout.StartCheckoutCommand;
import de.sample.aiarchitecture.checkout.application.startcheckout.StartCheckoutInputPort;
import de.sample.aiarchitecture.checkout.application.startcheckout.StartCheckoutResult;
import de.sample.aiarchitecture.checkout.application.submitbuyerinfo.SubmitBuyerInfoCommand;
import de.sample.aiarchitecture.checkout.application.submitbuyerinfo.SubmitBuyerInfoInputPort;
import de.sample.aiarchitecture.checkout.application.submitdelivery.SubmitDeliveryCommand;
import de.sample.aiarchitecture.checkout.application.submitdelivery.SubmitDeliveryInputPort;
import de.sample.aiarchitecture.checkout.application.submitpayment.SubmitPaymentCommand;
import de.sample.aiarchitecture.checkout.application.submitpayment.SubmitPaymentInputPort;
import de.sample.aiarchitecture.infrastructure.AiArchitectureApplication;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test verifying the complete checkout flow end-to-end.
 *
 * <p>This test verifies:
 * <ul>
 *   <li>Create cart and add items</li>
 *   <li>Start checkout (cart remains ACTIVE - can still be modified)</li>
 *   <li>Submit buyer information</li>
 *   <li>Submit delivery information</li>
 *   <li>Submit payment information</li>
 *   <li>Confirm checkout (cart becomes COMPLETED)</li>
 * </ul>
 *
 * <p><b>US-24:</b> Build and Manual Test - Verify complete checkout flow works end-to-end
 */
@SpringBootTest(classes = AiArchitectureApplication.class)
@Transactional
class CheckoutFlowIntegrationTest {

    @Autowired
    private GetOrCreateActiveCartUseCase getOrCreateActiveCartUseCase;

    @Autowired
    private AddItemToCartUseCase addItemToCartUseCase;

    @Autowired
    private GetCartByIdUseCase getCartByIdUseCase;

    @Autowired
    private StartCheckoutInputPort startCheckoutInputPort;

    @Autowired
    private GetCheckoutSessionInputPort getCheckoutSessionInputPort;

    @Autowired
    private SubmitBuyerInfoInputPort submitBuyerInfoInputPort;

    @Autowired
    private SubmitDeliveryInputPort submitDeliveryInputPort;

    @Autowired
    private SubmitPaymentInputPort submitPaymentInputPort;

    @Autowired
    private ConfirmCheckoutInputPort confirmCheckoutInputPort;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Gets the first product ID from sample data.
     */
    private String getFirstProductId() {
        List<Product> products = productRepository.findAll();
        assertFalse(products.isEmpty(), "Sample data should have products loaded");
        return products.get(0).id().value().toString();
    }

    @Test
    void completeCheckoutFlow_shouldTransitionCartThroughAllStates() {
        // Step 1: Create a cart and add a product
        String customerId = "test-customer-" + System.currentTimeMillis();

        GetOrCreateActiveCartResult cartResponse =
            getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
        String cartId = cartResponse.cartId();
        assertNotNull(cartId, "Cart should be created");

        // Get a real product ID from sample data
        String productId = getFirstProductId();

        // Add a product
        addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, productId, 2));

        // Verify cart is ACTIVE with items
        GetCartByIdResult cartBefore = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));
        assertTrue(cartBefore.found(), "Cart should be found");
        assertEquals("ACTIVE", cartBefore.cart().orElseThrow().status().name(), "Cart should be ACTIVE before checkout");
        assertFalse(cartBefore.cart().orElseThrow().items().isEmpty(), "Cart should have items");

        // Step 2: Start checkout (cart remains ACTIVE - user can still modify it)
        StartCheckoutResult startResponse =
            startCheckoutInputPort.execute(new StartCheckoutCommand(cartId));
        String sessionId = startResponse.sessionId();
        assertNotNull(sessionId, "Checkout session should be created");
        assertEquals("BUYER_INFO", startResponse.currentStep(), "Should start at BUYER_INFO step");
        assertEquals("ACTIVE", startResponse.status(), "Session should be ACTIVE");

        // Verify cart remains ACTIVE during checkout (can still be modified)
        GetCartByIdResult cartAfterStart = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));
        assertEquals("ACTIVE", cartAfterStart.cart().orElseThrow().status().name(), "Cart should remain ACTIVE during checkout");

        // Step 3: Submit buyer information
        submitBuyerInfoInputPort.execute(new SubmitBuyerInfoCommand(
            sessionId,
            "test@example.com",
            "John",
            "Doe",
            "+1-555-0100"
        ));

        GetCheckoutSessionResult afterBuyer =
            getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(sessionId));
        assertEquals("DELIVERY", afterBuyer.currentStep(), "Should advance to DELIVERY step");

        // Step 4: Submit delivery information
        submitDeliveryInputPort.execute(new SubmitDeliveryCommand(
            sessionId,
            "123 Main Street",
            null,              // streetLine2
            "Springfield",
            "12345",
            "United States",
            "IL",              // state
            "STANDARD",        // shippingOptionId
            "Standard Shipping",
            "5-7 business days",
            new BigDecimal("5.99"),
            "EUR"
        ));

        GetCheckoutSessionResult afterDelivery =
            getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(sessionId));
        assertEquals("PAYMENT", afterDelivery.currentStep(), "Should advance to PAYMENT step");

        // Step 5: Submit payment information (using "mock" payment provider)
        submitPaymentInputPort.execute(new SubmitPaymentCommand(
            sessionId,
            "mock",
            null  // providerReference - not needed for mock provider
        ));

        GetCheckoutSessionResult afterPayment =
            getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(sessionId));
        assertEquals("REVIEW", afterPayment.currentStep(), "Should advance to REVIEW step");

        // Step 6: Confirm checkout
        ConfirmCheckoutResult confirmResponse =
            confirmCheckoutInputPort.execute(new ConfirmCheckoutCommand(sessionId));

        assertEquals("CONFIRMED", confirmResponse.status(), "Session should be CONFIRMED");
        // Note: orderReference is set when complete() is called, not during confirm()
        // The confirm step just validates and marks the session as CONFIRMED

        // Verify cart status is now COMPLETED (via CheckoutConfirmed event -> CompleteCart)
        // The CheckoutEventConsumer listens for CheckoutConfirmed and calls CompleteCartUseCase
        GetCartByIdResult cartAfterConfirm = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));
        assertEquals("COMPLETED", cartAfterConfirm.cart().orElseThrow().status().name(),
            "Cart should be COMPLETED after checkout confirmation");
    }

    @Test
    void checkoutFlow_shouldRejectEmptyCart() {
        // Create an empty cart
        String customerId = "test-customer-empty-" + System.currentTimeMillis();

        GetOrCreateActiveCartResult cartResponse =
            getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
        String cartId = cartResponse.cartId();

        // Try to start checkout with empty cart - should fail
        assertThrows(IllegalArgumentException.class, () ->
            startCheckoutInputPort.execute(new StartCheckoutCommand(cartId)),
            "Should reject checkout of empty cart"
        );
    }

    @Test
    void checkoutFlow_allowsMultipleCheckoutSessionsWhileCartIsActive() {
        // Create a cart and add a product
        String customerId = "test-customer-double-" + System.currentTimeMillis();

        GetOrCreateActiveCartResult cartResponse =
            getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
        String cartId = cartResponse.cartId();

        // Get a real product ID from sample data
        String productId = getFirstProductId();

        // Add a product
        addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, productId, 1));

        // Start first checkout
        StartCheckoutResult firstSession = startCheckoutInputPort.execute(new StartCheckoutCommand(cartId));
        assertNotNull(firstSession.sessionId(), "First checkout session should be created");

        // Cart remains ACTIVE, so starting another checkout is allowed
        // (user abandoned previous checkout and started fresh)
        StartCheckoutResult secondSession = startCheckoutInputPort.execute(new StartCheckoutCommand(cartId));
        assertNotNull(secondSession.sessionId(), "Second checkout session should be created");

        // Verify cart is still ACTIVE
        GetCartByIdResult cart = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));
        assertEquals("ACTIVE", cart.cart().orElseThrow().status().name(), "Cart should remain ACTIVE");
    }
}
