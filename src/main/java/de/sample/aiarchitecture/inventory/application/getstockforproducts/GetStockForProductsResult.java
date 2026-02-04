package de.sample.aiarchitecture.inventory.application.getstockforproducts;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * Output model containing stock information for multiple products.
 *
 * @param stocks map of product IDs to their stock data
 */
public record GetStockForProductsResult(@NonNull Map<ProductId, StockData> stocks) {

  /**
   * Stock information for a single product.
   *
   * @param productId the product identifier
   * @param availableStock the quantity of stock available (unreserved)
   * @param isAvailable whether any stock is available for purchase
   */
  public record StockData(
      @NonNull ProductId productId,
      int availableStock,
      boolean isAvailable) {

    public StockData {
      if (productId == null) {
        throw new IllegalArgumentException("ProductId cannot be null");
      }
      if (availableStock < 0) {
        throw new IllegalArgumentException("Available stock cannot be negative");
      }
    }
  }
}
