package de.sample.aiarchitecture.checkout.application.getshippingoptions;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Output model containing available shipping options.
 *
 * @param shippingOptions list of available shipping options
 */
public record GetShippingOptionsResponse(@NonNull List<ShippingOptionResponse> shippingOptions) {

  /**
   * Individual shipping option details.
   *
   * @param id the shipping option identifier
   * @param name the display name
   * @param estimatedDelivery the estimated delivery timeframe
   * @param cost the shipping cost amount
   * @param currencyCode the currency code (e.g., "EUR")
   */
  public record ShippingOptionResponse(
      @NonNull String id,
      @NonNull String name,
      @NonNull String estimatedDelivery,
      @NonNull BigDecimal cost,
      @NonNull String currencyCode) {}
}
