package de.sample.aiarchitecture;

import de.sample.aiarchitecture.cart.adapter.outgoing.product.CompositeArticleDataAdapter;
import de.sample.aiarchitecture.cart.application.additemtocart.AddItemToCartCommand;
import de.sample.aiarchitecture.cart.application.additemtocart.AddItemToCartUseCase;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdQuery;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdResult;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdUseCase;
import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartCommand;
import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartResult;
import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartUseCase;
import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort;
import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort.ArticleData;
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
import de.sample.aiarchitecture.inventory.adapter.incoming.openhost.InventoryService;
import de.sample.aiarchitecture.pricing.adapter.incoming.openhost.PricingService;
import de.sample.aiarchitecture.pricing.application.setproductprice.SetProductPriceCommand;
import de.sample.aiarchitecture.pricing.application.setproductprice.SetProductPriceInputPort;
import de.sample.aiarchitecture.product.adapter.incoming.openhost.ProductCatalogService;
import de.sample.aiarchitecture.product.adapter.incoming.openhost.ProductCatalogService.ProductInfo;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying the article data flow across bounded contexts.
 *
 * <p>This test suite validates:
 * <ul>
 *   <li>CompositeArticleDataAdapter aggregates data from ProductCatalogService,
 *       PricingService, and InventoryService (with fallback logic during transition)</li>
 *   <li>StartCheckoutUseCase uses CheckoutArticleDataPort for pricing</li>
 *   <li>ConfirmCheckoutUseCase uses resolver pattern for price validation</li>
 *   <li>GetCartByIdUseCase enriches cart items with fresh pricing data</li>
 *   <li>Price change detection works correctly</li>
 * </ul>
 *
 * <p><b>US-87:</b> Integration Tests for Article Data Flow
 */
@SpringBootTest(classes = AiArchitectureApplication.class)
@Transactional
class ArticleDataFlowIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCatalogService productCatalogService;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ArticleDataPort articleDataPort;

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
    private SetProductPriceInputPort setProductPriceInputPort;

    private ProductId testProductId;
    private String testProductIdString;

    @BeforeEach
    void setUp() {
        // Get first available product from sample data
        List<Product> products = productRepository.findAll();
        assertFalse(products.isEmpty(), "Sample data should have products loaded");
        testProductId = products.get(0).id();
        testProductIdString = testProductId.value().toString();
    }

    @Nested
    @DisplayName("CompositeArticleDataAdapter Tests")
    class CompositeArticleDataAdapterTests {

        @Test
        @DisplayName("Should aggregate data from available OHS services")
        void shouldAggregateDataFromOhsServices() {
            // When: Fetching article data through the composite adapter
            Optional<ArticleData> result = articleDataPort.getArticleData(testProductId);

            // Then: Data should be present (with fallback logic for pricing/inventory if needed)
            assertTrue(result.isPresent(), "Article data should be found");
            ArticleData articleData = result.get();

            // Verify product name comes from ProductCatalogService
            Optional<ProductInfo> productInfo = productCatalogService.getProductInfo(testProductId);
            assertTrue(productInfo.isPresent(), "Product info should be available");
            assertEquals(productInfo.get().name(), articleData.name(),
                "Product name should come from ProductCatalogService");

            // Verify pricing is available (from PricingService or fallback to Product context)
            assertNotNull(articleData.currentPrice(), "Price should be resolved");
            assertTrue(articleData.currentPrice().amount().compareTo(BigDecimal.ZERO) > 0,
                "Price should be greater than zero");

            // Verify stock data is available
            assertTrue(articleData.availableStock() >= 0, "Stock should not be negative");
        }

        @Test
        @DisplayName("Should fetch bulk article data for multiple products")
        void shouldFetchBulkArticleData() {
            // Given: Multiple products from sample data
            List<Product> products = productRepository.findAll();
            assertTrue(products.size() >= 2, "Need at least 2 products for bulk test");

            Set<ProductId> productIds = Set.of(
                products.get(0).id(),
                products.get(1).id()
            );

            // When: Fetching article data for multiple products
            Map<ProductId, ArticleData> result = articleDataPort.getArticleData(productIds);

            // Then: All requested products should have data
            assertEquals(2, result.size(), "Should return data for all requested products");

            for (ProductId productId : productIds) {
                assertTrue(result.containsKey(productId),
                    "Should have data for product: " + productId.value());
                ArticleData data = result.get(productId);
                assertNotNull(data.name(), "Product should have name");
                assertNotNull(data.currentPrice(), "Product should have price");
            }
        }

        @Test
        @DisplayName("Should return empty for empty input")
        void shouldReturnEmptyForEmptyInput() {
            Map<ProductId, ArticleData> result = articleDataPort.getArticleData(Set.of());
            assertTrue(result.isEmpty(), "Should return empty map for empty input");
        }

        @Test
        @DisplayName("Should return empty Optional for unknown product")
        void shouldReturnEmptyForUnknownProduct() {
            ProductId unknownId = ProductId.of("00000000-0000-0000-0000-000000000000");
            Optional<ArticleData> result = articleDataPort.getArticleData(unknownId);
            assertTrue(result.isEmpty(), "Should return empty for unknown product");
        }

        @Test
        @DisplayName("CompositeArticleDataAdapter is properly wired as ArticleDataPort")
        void compositeAdapterIsProperlyWired() {
            // Verify that the ArticleDataPort is implemented by CompositeArticleDataAdapter
            assertTrue(articleDataPort instanceof CompositeArticleDataAdapter,
                "ArticleDataPort should be implemented by CompositeArticleDataAdapter");
        }
    }

    @Nested
    @DisplayName("StartCheckoutUseCase with Resolver Tests")
    class StartCheckoutUseCaseTests {

        @Test
        @DisplayName("Should use fresh pricing from CheckoutArticleDataPort when starting checkout")
        void shouldUseFreshPricingWhenStartingCheckout() {
            // Given: A cart with items
            String customerId = "test-customer-checkout-" + System.currentTimeMillis();
            GetOrCreateActiveCartResult cartResponse =
                getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
            String cartId = cartResponse.cartId();

            addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, testProductIdString, 2));

            // When: Starting checkout
            StartCheckoutResult result = startCheckoutInputPort.execute(new StartCheckoutCommand(cartId));

            // Then: Checkout session should have line items with current prices
            assertNotNull(result.sessionId(), "Session should be created");
            assertFalse(result.lineItems().isEmpty(), "Should have line items");

            // Verify line item has pricing from CheckoutArticleDataPort
            StartCheckoutResult.LineItemData lineItem = result.lineItems().get(0);
            assertEquals(testProductIdString, lineItem.productId());
            assertNotNull(lineItem.unitPrice(), "Unit price should be set from pricing service");
            assertEquals(2, lineItem.quantity());
        }

        @Test
        @DisplayName("Should include product name from ProductCatalogService in line items")
        void shouldIncludeProductNameInLineItems() {
            // Given: A cart with items
            String customerId = "test-customer-name-" + System.currentTimeMillis();
            GetOrCreateActiveCartResult cartResponse =
                getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
            String cartId = cartResponse.cartId();

            addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, testProductIdString, 1));

            // When: Starting checkout
            StartCheckoutResult result = startCheckoutInputPort.execute(new StartCheckoutCommand(cartId));

            // Then: Line item should have product name
            StartCheckoutResult.LineItemData lineItem = result.lineItems().get(0);
            Optional<ProductInfo> productInfo = productCatalogService.getProductInfo(testProductId);
            assertTrue(productInfo.isPresent());
            assertEquals(productInfo.get().name(), lineItem.productName(),
                "Line item should have product name from ProductCatalogService");
        }
    }

    @Nested
    @DisplayName("ConfirmCheckoutUseCase with Resolver Tests")
    class ConfirmCheckoutUseCaseTests {

        @Test
        @DisplayName("Should validate pricing with resolver during confirmation")
        void shouldValidatePricingDuringConfirmation() {
            // Given: A complete checkout session ready to confirm
            String customerId = "test-customer-confirm-" + System.currentTimeMillis();
            GetOrCreateActiveCartResult cartResponse =
                getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
            String cartId = cartResponse.cartId();

            addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, testProductIdString, 1));

            StartCheckoutResult startResult = startCheckoutInputPort.execute(new StartCheckoutCommand(cartId));
            String sessionId = startResult.sessionId();

            // Complete all checkout steps
            completeCheckoutSteps(sessionId);

            // When: Confirming checkout
            ConfirmCheckoutResult result = confirmCheckoutInputPort.execute(
                new ConfirmCheckoutCommand(sessionId));

            // Then: Checkout should be confirmed successfully
            assertEquals("CONFIRMED", result.status(), "Checkout should be confirmed");
            assertNotNull(result.totalAmount(), "Total should be calculated");
        }

        @Test
        @DisplayName("Should use resolver to fetch fresh pricing during confirmation")
        void shouldUseFreshPricingDuringConfirmation() {
            // Given: A checkout session
            String customerId = "test-customer-pricing-" + System.currentTimeMillis();
            GetOrCreateActiveCartResult cartResponse =
                getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
            String cartId = cartResponse.cartId();

            addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, testProductIdString, 1));

            StartCheckoutResult startResult = startCheckoutInputPort.execute(new StartCheckoutCommand(cartId));
            String sessionId = startResult.sessionId();

            // Complete all checkout steps
            completeCheckoutSteps(sessionId);

            // Get session before confirmation
            GetCheckoutSessionResult beforeConfirm =
                getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(sessionId));
            assertEquals("REVIEW", beforeConfirm.currentStep(), "Should be at review step");

            // When: Confirming - the resolver will fetch fresh prices from CheckoutArticleDataPort
            ConfirmCheckoutResult result = confirmCheckoutInputPort.execute(
                new ConfirmCheckoutCommand(sessionId));

            // Then: Confirmation should use prices from the resolver
            assertEquals("CONFIRMED", result.status());
        }
    }

    @Nested
    @DisplayName("GetCartByIdUseCase Enrichment Tests")
    class GetCartByIdUseCaseEnrichmentTests {

        @Test
        @DisplayName("Should enrich cart items with fresh pricing data")
        void shouldEnrichCartItemsWithFreshPricing() {
            // Given: A cart with items
            String customerId = "test-customer-enrich-" + System.currentTimeMillis();
            GetOrCreateActiveCartResult cartResponse =
                getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
            String cartId = cartResponse.cartId();

            addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, testProductIdString, 2));

            // When: Getting cart by ID
            GetCartByIdResult result = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));

            // Then: Cart items should have current pricing from ArticleDataPort
            assertTrue(result.found(), "Cart should be found");
            assertFalse(result.items().isEmpty(), "Cart should have items");

            GetCartByIdResult.CartItemSummary item = result.items().get(0);

            // Verify current price is fetched from pricing service
            assertNotNull(item.currentPriceAmount(), "Current price should be set");
            assertNotNull(item.currentPriceCurrency(), "Current price currency should be set");
        }

        @Test
        @DisplayName("Should include both price at addition and current price")
        void shouldIncludeBothPriceAtAdditionAndCurrentPrice() {
            // Given: A cart with items
            String customerId = "test-customer-prices-" + System.currentTimeMillis();
            GetOrCreateActiveCartResult cartResponse =
                getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
            String cartId = cartResponse.cartId();

            addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, testProductIdString, 1));

            // When: Getting cart
            GetCartByIdResult result = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));

            // Then: Item should have both price at addition and current price
            GetCartByIdResult.CartItemSummary item = result.items().get(0);

            assertNotNull(item.unitPriceAmount(), "Price at addition should be set");
            assertNotNull(item.unitPriceCurrency(), "Price at addition currency should be set");
            assertNotNull(item.currentPriceAmount(), "Current price should be set");
            assertNotNull(item.currentPriceCurrency(), "Current price currency should be set");
        }
    }

    @Nested
    @DisplayName("Price Change Detection Tests")
    class PriceChangeDetectionTests {

        @Test
        @DisplayName("Should detect when price has not changed")
        void shouldDetectNoPriceChange() {
            // Given: A cart with items (price at addition equals current price)
            String customerId = "test-customer-nochange-" + System.currentTimeMillis();
            GetOrCreateActiveCartResult cartResponse =
                getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
            String cartId = cartResponse.cartId();

            addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, testProductIdString, 1));

            // When: Getting cart immediately (price hasn't changed)
            GetCartByIdResult result = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));

            // Then: Price should not be marked as changed
            GetCartByIdResult.CartItemSummary item = result.items().get(0);
            assertFalse(item.priceChanged(),
                "Price should not be marked as changed when current equals original");

            // Verify prices are equal
            assertEquals(0, item.unitPriceAmount().compareTo(item.currentPriceAmount()),
                "Prices should be equal");
        }

        @Test
        @DisplayName("Should detect when price has changed")
        void shouldDetectPriceChange() {
            // Given: A cart with items
            String customerId = "test-customer-change-" + System.currentTimeMillis();
            GetOrCreateActiveCartResult cartResponse =
                getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
            String cartId = cartResponse.cartId();

            addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, testProductIdString, 1));

            // Get initial cart state
            GetCartByIdResult initialResult = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));
            GetCartByIdResult.CartItemSummary initialItem = initialResult.items().get(0);
            BigDecimal originalPrice = initialItem.unitPriceAmount();

            // When: Price changes (update to a different price)
            BigDecimal newPrice = originalPrice.add(BigDecimal.valueOf(10.00));
            setProductPriceInputPort.execute(new SetProductPriceCommand(
                testProductIdString,
                newPrice,
                "EUR"
            ));

            // Then: Getting cart should show price changed
            GetCartByIdResult updatedResult = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));
            GetCartByIdResult.CartItemSummary updatedItem = updatedResult.items().get(0);

            assertTrue(updatedItem.priceChanged(),
                "Price should be marked as changed after price update");
            assertEquals(0, originalPrice.compareTo(updatedItem.unitPriceAmount()),
                "Price at addition should remain the original price");
            assertEquals(0, newPrice.compareTo(updatedItem.currentPriceAmount()),
                "Current price should be the new price");
        }

        @Test
        @DisplayName("Price change detection should work for multiple items")
        void shouldDetectPriceChangeForMultipleItems() {
            // Given: Multiple products
            List<Product> products = productRepository.findAll();
            assertTrue(products.size() >= 2, "Need at least 2 products");

            ProductId product1Id = products.get(0).id();
            ProductId product2Id = products.get(1).id();

            // Create cart with both products
            String customerId = "test-customer-multi-" + System.currentTimeMillis();
            GetOrCreateActiveCartResult cartResponse =
                getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
            String cartId = cartResponse.cartId();

            addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, product1Id.value().toString(), 1));
            addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, product2Id.value().toString(), 1));

            // Get initial prices
            GetCartByIdResult initialResult = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));
            assertEquals(2, initialResult.items().size(), "Cart should have 2 items");

            // Change price for only first product
            GetCartByIdResult.CartItemSummary item1Initial = initialResult.items().stream()
                .filter(i -> i.productId().equals(product1Id.value().toString()))
                .findFirst()
                .orElseThrow();

            BigDecimal newPrice = item1Initial.unitPriceAmount().add(BigDecimal.valueOf(5.00));
            setProductPriceInputPort.execute(new SetProductPriceCommand(
                product1Id.value().toString(),
                newPrice,
                "EUR"
            ));

            // When: Getting cart
            GetCartByIdResult result = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));

            // Then: Only first product should show price change
            GetCartByIdResult.CartItemSummary item1 = result.items().stream()
                .filter(i -> i.productId().equals(product1Id.value().toString()))
                .findFirst()
                .orElseThrow();

            GetCartByIdResult.CartItemSummary item2 = result.items().stream()
                .filter(i -> i.productId().equals(product2Id.value().toString()))
                .findFirst()
                .orElseThrow();

            assertTrue(item1.priceChanged(), "First product should show price changed");
            assertFalse(item2.priceChanged(), "Second product should not show price changed");
        }
    }

    @Nested
    @DisplayName("End-to-End Article Data Flow Tests")
    class EndToEndFlowTests {

        @Test
        @DisplayName("Complete flow: Cart -> Checkout -> Confirm with fresh pricing at each step")
        void completeFlowWithFreshPricingAtEachStep() {
            // Given: A cart with items
            String customerId = "test-e2e-" + System.currentTimeMillis();
            GetOrCreateActiveCartResult cartResponse =
                getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
            String cartId = cartResponse.cartId();

            addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, testProductIdString, 2));

            // Step 1: Verify cart enrichment
            GetCartByIdResult cartResult = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));
            assertTrue(cartResult.found());
            assertNotNull(cartResult.items().get(0).currentPriceAmount(),
                "Cart should have fresh pricing from ArticleDataPort");

            // Step 2: Start checkout (uses CheckoutArticleDataPort for pricing)
            StartCheckoutResult startResult = startCheckoutInputPort.execute(new StartCheckoutCommand(cartId));
            assertNotNull(startResult.lineItems().get(0).unitPrice(),
                "Checkout should have pricing from CheckoutArticleDataPort");

            // Step 3: Complete checkout steps
            String sessionId = startResult.sessionId();
            completeCheckoutSteps(sessionId);

            // Step 4: Confirm (uses resolver with fresh pricing)
            ConfirmCheckoutResult confirmResult = confirmCheckoutInputPort.execute(
                new ConfirmCheckoutCommand(sessionId));
            assertEquals("CONFIRMED", confirmResult.status(),
                "Checkout should be confirmed with resolver validation");
        }

        @Test
        @DisplayName("All three OHS services are accessible")
        void ohsServicesAreAccessible() {
            // Verify ProductCatalogService returns data
            Optional<ProductInfo> productInfo = productCatalogService.getProductInfo(testProductId);
            assertTrue(productInfo.isPresent(), "ProductCatalogService should return product info");

            // Verify PricingService and InventoryService can be called (may or may not have data
            // depending on async initialization, but calls should not fail)
            assertDoesNotThrow(() -> pricingService.getPrice(testProductId),
                "PricingService should be callable");
            assertDoesNotThrow(() -> inventoryService.getStock(testProductId),
                "InventoryService should be callable");

            // Verify composite adapter returns aggregated data (with fallback for pricing if needed)
            Optional<ArticleData> articleData = articleDataPort.getArticleData(testProductId);
            assertTrue(articleData.isPresent(), "CompositeAdapter should return aggregated data");
            assertEquals(productInfo.get().name(), articleData.get().name(),
                "Product name should match");
        }
    }

    /**
     * Helper method to complete all checkout steps before confirmation.
     */
    private void completeCheckoutSteps(String sessionId) {
        // Submit buyer info
        submitBuyerInfoInputPort.execute(new SubmitBuyerInfoCommand(
            sessionId,
            "test@example.com",
            "John",
            "Doe",
            "+1-555-0100"
        ));

        // Submit delivery
        submitDeliveryInputPort.execute(new SubmitDeliveryCommand(
            sessionId,
            "123 Main Street",
            null,
            "Springfield",
            "12345",
            "United States",
            "IL",
            "STANDARD",
            "Standard Shipping",
            "5-7 business days",
            new BigDecimal("5.99"),
            "EUR"
        ));

        // Submit payment
        submitPaymentInputPort.execute(new SubmitPaymentCommand(
            sessionId,
            "mock",
            null
        ));
    }
}
