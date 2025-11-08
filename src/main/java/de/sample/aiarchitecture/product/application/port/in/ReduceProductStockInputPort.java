package de.sample.aiarchitecture.product.application.port.in;

import de.sample.aiarchitecture.product.application.usecase.reduceproductstock.ReduceProductStockCommand;
import de.sample.aiarchitecture.product.application.usecase.reduceproductstock.ReduceProductStockResponse;
import de.sample.aiarchitecture.sharedkernel.application.marker.InputPort;

/**
 * Input Port for reducing product stock.
 *
 * <p>This port defines the contract for the "Reduce Product Stock" use case,
 * typically triggered by cross-context events (e.g., when a cart is checked out).
 */
public interface ReduceProductStockInputPort extends InputPort<ReduceProductStockCommand, ReduceProductStockResponse> {}
