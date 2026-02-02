package de.sample.aiarchitecture.checkout.application.getpaymentproviders;

import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Output model containing available payment providers.
 *
 * @param paymentProviders list of available payment providers
 */
public record GetPaymentProvidersResult(@NonNull List<PaymentProviderData> paymentProviders) {

  /**
   * Individual payment provider details.
   *
   * @param id the payment provider identifier
   * @param displayName the human-readable display name
   * @param available whether the provider is currently available
   */
  public record PaymentProviderData(
      @NonNull String id,
      @NonNull String displayName,
      boolean available) {}
}
