package de.sample.aiarchitecture.pricing.application.setproductprice;

import java.math.BigDecimal;
import java.time.Instant;
import org.jspecify.annotations.NonNull;

/**
 * Output model for setting or updating a product's price.
 *
 * @param priceId the unique identifier of the price record
 * @param productId the product ID
 * @param priceAmount the current price amount
 * @param priceCurrency the price currency code
 * @param effectiveFrom when the price became effective
 * @param created true if a new price was created, false if an existing price was updated
 */
public record SetProductPriceResult(
    @NonNull String priceId,
    @NonNull String productId,
    @NonNull BigDecimal priceAmount,
    @NonNull String priceCurrency,
    @NonNull Instant effectiveFrom,
    boolean created) {}
