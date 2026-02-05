package de.sample.aiarchitecture.inventory.application.reducestock;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;

/**
 * Input port for reducing stock of a product.
 *
 * <p>This use case is typically invoked when an order is confirmed
 * to reduce the available stock for the ordered products.
 */
public interface ReduceStockInputPort extends UseCase<ReduceStockCommand, ReduceStockResult> {
}
