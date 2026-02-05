package de.sample.aiarchitecture.product.adapter.outgoing.inventory;

import de.sample.aiarchitecture.inventory.adapter.incoming.openhost.InventoryService;
import de.sample.aiarchitecture.inventory.adapter.incoming.openhost.InventoryService.StockInfo;
import de.sample.aiarchitecture.product.application.shared.ProductStockDataPort;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Adapter that fetches stock data from the Inventory context via its Open Host Service.
 *
 * <p>This adapter implements Product's {@link ProductStockDataPort} by delegating to
 * the Inventory context's {@link InventoryService} OHS.
 *
 * <p>This adapter is the ONLY place in Product context that imports from the Inventory
 * context, isolating cross-context coupling to the adapter layer.
 *
 * <p><b>Hexagonal Architecture:</b> This is an outgoing adapter that implements
 * an output port by delegating to an incoming adapter (OHS) of another context.
 */
@Component
public class InventoryStockDataAdapter implements ProductStockDataPort {

  private final InventoryService inventoryService;

  public InventoryStockDataAdapter(final InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Override
  public Optional<StockData> getStockData(final ProductId productId) {
    return inventoryService.getStock(productId)
        .map(this::toStockData);
  }

  @Override
  public Map<ProductId, StockData> getStockData(final Collection<ProductId> productIds) {
    return inventoryService.getStock(productIds).entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> toStockData(entry.getValue())));
  }

  private StockData toStockData(final StockInfo stockInfo) {
    return new StockData(
        stockInfo.productId(),
        stockInfo.availableStock(),
        stockInfo.isAvailable());
  }
}
