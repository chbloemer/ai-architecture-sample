package de.sample.aiarchitecture.inventory;

import de.sample.aiarchitecture.checkout.domain.event.CheckoutConfirmed;
import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.infrastructure.AiArchitectureApplication;
import de.sample.aiarchitecture.inventory.adapter.incoming.event.CheckoutConfirmedEventConsumer;
import de.sample.aiarchitecture.inventory.application.shared.StockLevelRepository;
import de.sample.aiarchitecture.inventory.domain.model.StockLevel;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying stock reduction in the Inventory context when a checkout is confirmed.
 *
 * <p>This test suite validates the cross-context integration between Checkout and Inventory:
 * <ul>
 *   <li>CheckoutConfirmed event triggers stock reduction</li>
 *   <li>Stock is correctly reduced for single and multiple items</li>
 *   <li>Partial failures are handled gracefully</li>
 * </ul>
 */
@SpringBootTest(classes = AiArchitectureApplication.class)
@Transactional
class StockReductionOnCheckoutIntegrationTest {

    private static final int DEFAULT_INITIAL_STOCK = 100;

    @Autowired
    private CheckoutConfirmedEventConsumer eventConsumer;

    @Autowired
    private StockLevelRepository stockLevelRepository;

    @Autowired
    private ProductRepository productRepository;

    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProducts = productRepository.findAll();
        assertTrue(testProducts.size() >= 2, "Sample data should have at least 2 products loaded");

        // Ensure stock levels exist for test products
        // SampleDataInitializer should have created these, but we ensure they exist
        // in case the test runs in isolation or sample data setup failed
        ensureStockLevelExists(getProductId(0), DEFAULT_INITIAL_STOCK);
        ensureStockLevelExists(getProductId(1), DEFAULT_INITIAL_STOCK);
    }

    private ProductId getProductId(int index) {
        return testProducts.get(index).id();
    }

    /**
     * Ensures a stock level exists for a product, creating one if needed.
     */
    private void ensureStockLevelExists(ProductId productId, int initialStock) {
        Optional<StockLevel> existing = stockLevelRepository.findByProductId(productId);
        if (existing.isEmpty()) {
            StockLevel stockLevel = StockLevel.create(productId, initialStock);
            stockLevelRepository.save(stockLevel);
        }
    }

    private int getInitialStock(ProductId productId) {
        return stockLevelRepository.findByProductId(productId)
            .map(sl -> sl.availableQuantity().value())
            .orElseThrow(() -> new AssertionError("Stock level should exist for product: " + productId.value()));
    }

    private int getCurrentStock(ProductId productId) {
        return stockLevelRepository.findByProductId(productId)
            .map(sl -> sl.availableQuantity().value())
            .orElseThrow(() -> new AssertionError("Stock level should exist for product: " + productId.value()));
    }

    private CheckoutConfirmed createCheckoutConfirmedEvent(List<CheckoutConfirmed.LineItemInfo> items) {
        return new CheckoutConfirmed(
            UUID.randomUUID(),
            CheckoutSessionId.generate(),
            CartId.generate(),
            CustomerId.of("test-customer"),
            Money.euro(new BigDecimal("99.99")),
            items,
            Instant.now(),
            1
        );
    }

    @Nested
    @DisplayName("Stock Reduction on CheckoutConfirmed Event")
    class StockReductionTests {

        @Test
        @DisplayName("Should reduce stock for single item when checkout is confirmed")
        void shouldReduceStockForSingleItem() {
            // Arrange
            ProductId productId = getProductId(0);
            int initialStock = getInitialStock(productId);
            int orderQuantity = 2;

            // Ensure we have enough stock
            assertTrue(initialStock >= orderQuantity,
                "Initial stock (" + initialStock + ") should be >= order quantity (" + orderQuantity + ")");

            CheckoutConfirmed event = createCheckoutConfirmedEvent(
                List.of(new CheckoutConfirmed.LineItemInfo(productId, orderQuantity))
            );

            // Act
            eventConsumer.onCheckoutConfirmed(event);

            // Assert
            int newStock = getCurrentStock(productId);
            assertEquals(initialStock - orderQuantity, newStock,
                "Stock should be reduced by the ordered quantity");
        }

        @Test
        @DisplayName("Should reduce stock for multiple items when checkout is confirmed")
        void shouldReduceStockForMultipleItems() {
            // Arrange
            ProductId product1Id = getProductId(0);
            ProductId product2Id = getProductId(1);

            int initialStock1 = getInitialStock(product1Id);
            int initialStock2 = getInitialStock(product2Id);

            int orderQuantity1 = 2;
            int orderQuantity2 = 3;

            // Ensure we have enough stock for both products
            assertTrue(initialStock1 >= orderQuantity1,
                "Initial stock for product 1 should be >= order quantity");
            assertTrue(initialStock2 >= orderQuantity2,
                "Initial stock for product 2 should be >= order quantity");

            CheckoutConfirmed event = createCheckoutConfirmedEvent(
                List.of(
                    new CheckoutConfirmed.LineItemInfo(product1Id, orderQuantity1),
                    new CheckoutConfirmed.LineItemInfo(product2Id, orderQuantity2)
                )
            );

            // Act
            eventConsumer.onCheckoutConfirmed(event);

            // Assert
            int newStock1 = getCurrentStock(product1Id);
            int newStock2 = getCurrentStock(product2Id);

            assertEquals(initialStock1 - orderQuantity1, newStock1,
                "Stock for product 1 should be reduced by ordered quantity");
            assertEquals(initialStock2 - orderQuantity2, newStock2,
                "Stock for product 2 should be reduced by ordered quantity");
        }

        @Test
        @DisplayName("Should handle unknown product gracefully without affecting other items")
        void shouldHandleUnknownProductGracefully() {
            // Arrange
            ProductId validProductId = getProductId(0);
            ProductId unknownProductId = ProductId.of("00000000-0000-0000-0000-000000000000");

            int initialStock = getInitialStock(validProductId);
            int orderQuantity = 1;

            assertTrue(initialStock >= orderQuantity,
                "Initial stock should be >= order quantity");

            CheckoutConfirmed event = createCheckoutConfirmedEvent(
                List.of(
                    new CheckoutConfirmed.LineItemInfo(unknownProductId, 5),
                    new CheckoutConfirmed.LineItemInfo(validProductId, orderQuantity)
                )
            );

            // Act - should not throw exception even with unknown product
            assertDoesNotThrow(() -> eventConsumer.onCheckoutConfirmed(event),
                "Event processing should continue despite failures for individual items");

            // Assert - valid product's stock should still be reduced
            int newStock = getCurrentStock(validProductId);
            assertEquals(initialStock - orderQuantity, newStock,
                "Stock for valid product should still be reduced despite unknown product failure");
        }

        @Test
        @DisplayName("Should handle empty item list gracefully")
        void shouldHandleEmptyItemList() {
            // Arrange
            CheckoutConfirmed event = createCheckoutConfirmedEvent(List.of());

            // Act & Assert - should not throw exception
            assertDoesNotThrow(() -> eventConsumer.onCheckoutConfirmed(event),
                "Event processing should handle empty item list gracefully");
        }

        @Test
        @DisplayName("Should reduce stock by exact quantity ordered")
        void shouldReduceStockByExactQuantity() {
            // Arrange
            ProductId productId = getProductId(0);
            int initialStock = getInitialStock(productId);

            // Test with different quantities
            int[] quantities = {1, 5, 10};

            for (int quantity : quantities) {
                if (initialStock < quantity) {
                    break; // Skip if not enough stock
                }

                int stockBefore = getCurrentStock(productId);

                CheckoutConfirmed event = createCheckoutConfirmedEvent(
                    List.of(new CheckoutConfirmed.LineItemInfo(productId, quantity))
                );

                // Act
                eventConsumer.onCheckoutConfirmed(event);

                // Assert
                int stockAfter = getCurrentStock(productId);
                assertEquals(stockBefore - quantity, stockAfter,
                    "Stock should be reduced by exactly " + quantity);
            }
        }
    }

    @Nested
    @DisplayName("Event Processing Behavior")
    class EventProcessingBehaviorTests {

        @Test
        @DisplayName("Should process event with correct session and cart IDs")
        void shouldProcessEventWithCorrectIds() {
            // Arrange
            ProductId productId = getProductId(0);
            int initialStock = getInitialStock(productId);

            assertTrue(initialStock >= 1, "Need at least 1 item in stock");

            CheckoutSessionId sessionId = CheckoutSessionId.generate();
            CartId cartId = CartId.generate();

            CheckoutConfirmed event = new CheckoutConfirmed(
                UUID.randomUUID(),
                sessionId,
                cartId,
                CustomerId.of("customer-123"),
                Money.euro(new BigDecimal("49.99")),
                List.of(new CheckoutConfirmed.LineItemInfo(productId, 1)),
                Instant.now(),
                1
            );

            // Act - should process without issues regardless of session/cart IDs
            assertDoesNotThrow(() -> eventConsumer.onCheckoutConfirmed(event),
                "Event should be processed successfully");

            // Assert
            int newStock = getCurrentStock(productId);
            assertEquals(initialStock - 1, newStock,
                "Stock should be reduced by 1");
        }

        @Test
        @DisplayName("Should handle multiple consecutive checkout events")
        void shouldHandleMultipleConsecutiveEvents() {
            // Arrange
            ProductId productId = getProductId(0);
            int initialStock = getInitialStock(productId);
            int ordersToProcess = 3;
            int quantityPerOrder = 1;

            assertTrue(initialStock >= ordersToProcess * quantityPerOrder,
                "Need enough stock for all orders");

            // Act - process multiple events
            for (int i = 0; i < ordersToProcess; i++) {
                CheckoutConfirmed event = createCheckoutConfirmedEvent(
                    List.of(new CheckoutConfirmed.LineItemInfo(productId, quantityPerOrder))
                );
                eventConsumer.onCheckoutConfirmed(event);
            }

            // Assert
            int finalStock = getCurrentStock(productId);
            assertEquals(initialStock - (ordersToProcess * quantityPerOrder), finalStock,
                "Stock should be reduced by total quantity from all orders");
        }
    }
}
