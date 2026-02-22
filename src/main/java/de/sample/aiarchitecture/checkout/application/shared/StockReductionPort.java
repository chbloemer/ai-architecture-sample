package de.sample.aiarchitecture.checkout.application.shared;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort;

/**
 * Output port for reducing stock after checkout confirmation.
 *
 * <p>This port enables the Checkout context to trigger stock reduction in the Inventory context
 * synchronously, replacing the previous event-driven approach that created a module cycle
 * (inventory→checkout→cart→inventory).
 */
public interface StockReductionPort extends OutputPort {
  void reduceStock(ProductId productId, int quantity);
}
