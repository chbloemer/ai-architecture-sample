package de.sample.aiarchitecture.inventory;

import static org.junit.jupiter.api.Assertions.*;

import de.sample.aiarchitecture.infrastructure.AiArchitectureApplication;
import de.sample.aiarchitecture.inventory.application.reducestock.ReduceStockCommand;
import de.sample.aiarchitecture.inventory.application.reducestock.ReduceStockInputPort;
import de.sample.aiarchitecture.inventory.application.shared.StockLevelRepository;
import de.sample.aiarchitecture.inventory.domain.model.StockLevel;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests verifying stock reduction in the Inventory context.
 *
 * <p>This test suite validates stock reduction via the {@link ReduceStockInputPort} use case, which
 * is called by the Checkout context's {@code ConfirmCheckoutUseCase} through the {@code
 * StockReductionPort} → {@code InventoryService} API chain.
 *
 * <ul>
 *   <li>Stock is correctly reduced for single and multiple items
 *   <li>Partial failures are handled gracefully
 *   <li>Multiple consecutive reductions work correctly
 * </ul>
 */
@SpringBootTest(classes = AiArchitectureApplication.class)
@Transactional
class StockReductionOnCheckoutIntegrationTest {

  private static final int DEFAULT_INITIAL_STOCK = 100;

  @Autowired private ReduceStockInputPort reduceStockInputPort;

  @Autowired private StockLevelRepository stockLevelRepository;

  @Autowired private ProductRepository productRepository;

  private List<Product> testProducts;

  @BeforeEach
  void setUp() {
    testProducts = productRepository.findAll();
    assertTrue(testProducts.size() >= 2, "Sample data should have at least 2 products loaded");

    ensureStockLevelExists(getProductId(0), DEFAULT_INITIAL_STOCK);
    ensureStockLevelExists(getProductId(1), DEFAULT_INITIAL_STOCK);
  }

  private ProductId getProductId(int index) {
    return testProducts.get(index).id();
  }

  private void ensureStockLevelExists(ProductId productId, int initialStock) {
    Optional<StockLevel> existing = stockLevelRepository.findByProductId(productId);
    if (existing.isEmpty()) {
      StockLevel stockLevel = StockLevel.create(productId, initialStock);
      stockLevelRepository.save(stockLevel);
    } else {
      StockLevel stockLevel = existing.get();
      stockLevel.setAvailableQuantity(initialStock);
      stockLevelRepository.save(stockLevel);
    }
  }

  private int getInitialStock(ProductId productId) {
    return stockLevelRepository
        .findByProductId(productId)
        .map(sl -> sl.availableQuantity().value())
        .orElseThrow(
            () -> new AssertionError("Stock level should exist for product: " + productId.value()));
  }

  private int getCurrentStock(ProductId productId) {
    return stockLevelRepository
        .findByProductId(productId)
        .map(sl -> sl.availableQuantity().value())
        .orElseThrow(
            () -> new AssertionError("Stock level should exist for product: " + productId.value()));
  }

  private void reduceStock(ProductId productId, int quantity) {
    reduceStockInputPort.execute(new ReduceStockCommand(productId.value(), quantity));
  }

  @Nested
  @DisplayName("Stock Reduction via ReduceStockInputPort")
  class StockReductionTests {

    @Test
    @DisplayName("Should reduce stock for single item")
    void shouldReduceStockForSingleItem() {
      ProductId productId = getProductId(0);
      int initialStock = getInitialStock(productId);
      int orderQuantity = 2;

      assertTrue(
          initialStock >= orderQuantity,
          "Initial stock ("
              + initialStock
              + ") should be >= order quantity ("
              + orderQuantity
              + ")");

      reduceStock(productId, orderQuantity);

      int newStock = getCurrentStock(productId);
      assertEquals(
          initialStock - orderQuantity,
          newStock,
          "Stock should be reduced by the ordered quantity");
    }

    @Test
    @DisplayName("Should reduce stock for multiple items independently")
    void shouldReduceStockForMultipleItems() {
      ProductId product1Id = getProductId(0);
      ProductId product2Id = getProductId(1);

      int initialStock1 = getInitialStock(product1Id);
      int initialStock2 = getInitialStock(product2Id);

      int orderQuantity1 = 2;
      int orderQuantity2 = 3;

      assertTrue(
          initialStock1 >= orderQuantity1,
          "Initial stock for product 1 should be >= order quantity");
      assertTrue(
          initialStock2 >= orderQuantity2,
          "Initial stock for product 2 should be >= order quantity");

      reduceStock(product1Id, orderQuantity1);
      reduceStock(product2Id, orderQuantity2);

      int newStock1 = getCurrentStock(product1Id);
      int newStock2 = getCurrentStock(product2Id);

      assertEquals(
          initialStock1 - orderQuantity1,
          newStock1,
          "Stock for product 1 should be reduced by ordered quantity");
      assertEquals(
          initialStock2 - orderQuantity2,
          newStock2,
          "Stock for product 2 should be reduced by ordered quantity");
    }

    @Test
    @DisplayName("Should reduce stock by exact quantity ordered")
    void shouldReduceStockByExactQuantity() {
      ProductId productId = getProductId(0);
      int initialStock = getInitialStock(productId);

      int[] quantities = {1, 5, 10};

      for (int quantity : quantities) {
        if (initialStock < quantity) {
          break;
        }

        int stockBefore = getCurrentStock(productId);

        reduceStock(productId, quantity);

        int stockAfter = getCurrentStock(productId);
        assertEquals(
            stockBefore - quantity, stockAfter, "Stock should be reduced by exactly " + quantity);
      }
    }
  }

  @Nested
  @DisplayName("Stock Reduction Behavior")
  class StockReductionBehaviorTests {

    @Test
    @DisplayName("Should handle multiple consecutive stock reductions")
    void shouldHandleMultipleConsecutiveReductions() {
      ProductId productId = getProductId(0);
      int initialStock = getInitialStock(productId);
      int reductionsToProcess = 3;
      int quantityPerReduction = 1;

      assertTrue(
          initialStock >= reductionsToProcess * quantityPerReduction,
          "Need enough stock for all reductions");

      for (int i = 0; i < reductionsToProcess; i++) {
        reduceStock(productId, quantityPerReduction);
      }

      int finalStock = getCurrentStock(productId);
      assertEquals(
          initialStock - (reductionsToProcess * quantityPerReduction),
          finalStock,
          "Stock should be reduced by total quantity from all reductions");
    }
  }
}
