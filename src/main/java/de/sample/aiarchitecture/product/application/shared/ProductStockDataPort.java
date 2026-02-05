package de.sample.aiarchitecture.product.application.shared;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Output port for accessing stock data from the Inventory context.
 *
 * <p>This port allows Product use cases to fetch stock information without
 * directly depending on the Inventory bounded context.
 *
 * <p><b>Hexagonal Architecture:</b> This is a secondary/driven port that defines what
 * the Product application layer needs from external stock data sources.
 */
public interface ProductStockDataPort extends OutputPort {

  /**
   * Stock information for a product.
   *
   * @param productId the product ID
   * @param availableStock the quantity available for purchase
   * @param isAvailable whether the product is available for purchase
   */
  record StockData(ProductId productId, int availableStock, boolean isAvailable) {}

  /**
   * Retrieves stock data for a product.
   *
   * @param productId the product ID to get stock for
   * @return stock data if found, empty if product has no stock record
   */
  Optional<StockData> getStockData(ProductId productId);

  /**
   * Retrieves stock data for multiple products.
   *
   * @param productIds the collection of product IDs to get stock for
   * @return map of product IDs to their stock data; products not found will not be in the map
   */
  Map<ProductId, StockData> getStockData(Collection<ProductId> productIds);
}
