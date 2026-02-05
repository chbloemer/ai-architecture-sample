package de.sample.aiarchitecture.inventory.adapter.incoming.openhost;

import de.sample.aiarchitecture.inventory.application.getstockforproducts.GetStockForProductsInputPort;
import de.sample.aiarchitecture.inventory.application.getstockforproducts.GetStockForProductsQuery;
import de.sample.aiarchitecture.inventory.application.getstockforproducts.GetStockForProductsResult;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.strategic.OpenHostService;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Open Host Service for Inventory.
 *
 * <p>This is an incoming adapter that exposes Inventory context capabilities to other bounded
 * contexts. It delegates to use cases (input ports) and translates responses to OHS DTOs.
 *
 * <p>Consuming contexts should NOT use this service directly in their use cases - they should
 * define their own output ports and implement adapters that delegate to this service.
 *
 * <p><b>Hexagonal Architecture:</b> As an incoming adapter, this service calls input ports (use
 * cases), NOT output ports (repositories) directly.
 */
@OpenHostService(
    context = "Inventory",
    description = "Provides stock information for other bounded contexts")
@Service("inventoryContextOhs")
public class InventoryService {

  private final GetStockForProductsInputPort getStockForProductsInputPort;

  public InventoryService(GetStockForProductsInputPort getStockForProductsInputPort) {
    this.getStockForProductsInputPort = getStockForProductsInputPort;
  }

  /**
   * Stock information DTO for cross-context communication.
   *
   * @param productId the product ID
   * @param availableStock the quantity of stock available (unreserved)
   * @param isAvailable whether any stock is available for purchase
   */
  public record StockInfo(
      ProductId productId, int availableStock, boolean isAvailable) {

    public StockInfo {
      if (productId == null) {
        throw new IllegalArgumentException("ProductId cannot be null");
      }
      if (availableStock < 0) {
        throw new IllegalArgumentException("Available stock cannot be negative");
      }
    }
  }

  /**
   * Retrieves stock information for multiple products.
   *
   * @param productIds the collection of product IDs to get stock for
   * @return map of product IDs to their stock info
   */
  public Map<ProductId, StockInfo> getStock(Collection<ProductId> productIds) {
    if (productIds.isEmpty()) {
      return Collections.emptyMap();
    }

    GetStockForProductsResult result =
        getStockForProductsInputPort.execute(new GetStockForProductsQuery(productIds));

    return result.stocks().entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry ->
                    new StockInfo(
                        entry.getValue().productId(),
                        entry.getValue().availableStock(),
                        entry.getValue().isAvailable())));
  }

  /**
   * Retrieves stock information for a single product.
   *
   * @param productId the product ID
   * @return stock info if found
   */
  public Optional<StockInfo> getStock(ProductId productId) {
    Map<ProductId, StockInfo> stocks = getStock(Collections.singletonList(productId));
    return Optional.ofNullable(stocks.get(productId));
  }

  /**
   * Checks if sufficient stock is available for a product.
   *
   * @param productId the product ID
   * @param quantity the required quantity
   * @return true if the product has at least the specified quantity available
   */
  public boolean hasStock(ProductId productId, int quantity) {
    if (quantity <= 0) {
      return true;
    }

    return getStock(productId)
        .map(stockInfo -> stockInfo.availableStock() >= quantity)
        .orElse(false);
  }
}
