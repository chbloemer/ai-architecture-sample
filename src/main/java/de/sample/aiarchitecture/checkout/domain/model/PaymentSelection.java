package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import org.jspecify.annotations.Nullable;

/**
 * Value Object representing the selected payment method for checkout.
 *
 * <p>Contains the payment provider identifier and optional provider-specific
 * reference data (e.g., payment intent ID, saved card token).
 */
public record PaymentSelection(
    PaymentProviderId providerId, @Nullable String providerReference)
    implements Value {

  public PaymentSelection {
    if (providerId == null) {
      throw new IllegalArgumentException("Payment provider ID cannot be null");
    }
  }

  public static PaymentSelection of(final PaymentProviderId providerId) {
    return new PaymentSelection(providerId, null);
  }

  public static PaymentSelection of(
      final PaymentProviderId providerId, final String providerReference) {
    return new PaymentSelection(providerId, providerReference);
  }

  public PaymentSelection withReference(final String reference) {
    return new PaymentSelection(this.providerId, reference);
  }

  public boolean hasReference() {
    return providerReference != null && !providerReference.isBlank();
  }
}
