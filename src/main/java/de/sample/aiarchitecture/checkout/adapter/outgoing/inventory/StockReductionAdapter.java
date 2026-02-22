package de.sample.aiarchitecture.checkout.adapter.outgoing.inventory;

import de.sample.aiarchitecture.checkout.application.shared.StockReductionPort;
import de.sample.aiarchitecture.inventory.api.InventoryService;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import org.springframework.stereotype.Component;

/** Adapter that delegates stock reduction to the Inventory context's API. */
@Component
public class StockReductionAdapter implements StockReductionPort {
  private final InventoryService inventoryService;

  public StockReductionAdapter(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Override
  public void reduceStock(ProductId productId, int quantity) {
    inventoryService.reduceStock(productId, quantity);
  }
}
