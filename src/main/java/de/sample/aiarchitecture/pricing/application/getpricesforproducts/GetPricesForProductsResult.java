package de.sample.aiarchitecture.pricing.application.getpricesforproducts;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.time.Instant;
import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * Output model for retrieving prices for multiple products.
 *
 * @param prices map of product IDs to their price data
 */
public record GetPricesForProductsResult(@NonNull Map<ProductId, PriceData> prices) {

  /**
   * Price data for a product.
   *
   * @param productId the product ID
   * @param currentPrice the current price
   * @param effectiveFrom when the price became effective
   */
  public record PriceData(
      @NonNull ProductId productId,
      @NonNull Money currentPrice,
      @NonNull Instant effectiveFrom) {}
}
