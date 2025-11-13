package de.sample.aiarchitecture.product.application.reduceproductstock;

import de.sample.aiarchitecture.sharedkernel.application.port.UseCase;

/**
 * Input Port for reducing product stock.
 *
 * <p>This port defines the contract for the "Reduce Product Stock" use case,
 * typically triggered by cross-context events (e.g., when a cart is checked out).
 */
public interface ReduceProductStockInputPort extends UseCase<ReduceProductStockCommand, ReduceProductStockResponse> {}
