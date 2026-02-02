package de.sample.aiarchitecture.product.application.reduceproductstock;

import org.jspecify.annotations.NonNull;

/**
 * Response for reducing product stock.
 *
 * @param productId the product ID
 * @param previousStock the stock quantity before reduction
 * @param newStock the stock quantity after reduction
 */
public record ReduceProductStockResult(
    @NonNull String productId,
    int previousStock,
    int newStock
) {}
