package de.sample.aiarchitecture.product.application.updateproductprice;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

/**
 * Output model for product price update.
 *
 * @param productId the product ID
 * @param oldPriceAmount the previous price amount
 * @param oldPriceCurrency the previous price currency
 * @param newPriceAmount the new price amount
 * @param newPriceCurrency the new price currency
 */
public record UpdateProductPriceResult(
    @NonNull String productId,
    @NonNull BigDecimal oldPriceAmount,
    @NonNull String oldPriceCurrency,
    @NonNull BigDecimal newPriceAmount,
    @NonNull String newPriceCurrency
) {}
