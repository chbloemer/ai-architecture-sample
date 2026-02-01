package de.sample.aiarchitecture.checkout.adapter.outgoing.payment;

import de.sample.aiarchitecture.checkout.application.shared.PaymentProvider;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.PaymentProviderId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of PaymentProvider for testing and development.
 *
 * <p>This adapter simulates payment processing without connecting to any real payment
 * gateway. All payments are automatically approved, making it suitable for local
 * development, testing, and demonstration purposes.
 *
 * <p>The provider generates mock transaction references in the format "mock-{uuid}"
 * to simulate real provider behavior.
 */
@Component
public class MockPaymentProvider implements PaymentProvider {

  public static final PaymentProviderId PROVIDER_ID = PaymentProviderId.of("mock");
  private static final String DISPLAY_NAME = "Mock Payment (Test)";

  private boolean available = true;

  @Override
  @NonNull
  public PaymentProviderId providerId() {
    return PROVIDER_ID;
  }

  @Override
  @NonNull
  public String displayName() {
    return DISPLAY_NAME;
  }

  @Override
  @NonNull
  public PaymentResult initiatePayment(
      @NonNull final CheckoutSessionId sessionId, @NonNull final Money amount) {
    if (!available) {
      return PaymentResult.failure("Mock payment provider is currently unavailable");
    }

    // Generate a mock payment reference
    final String reference = "mock-" + UUID.randomUUID().toString();
    return PaymentResult.success(reference);
  }

  @Override
  @NonNull
  public PaymentResult confirmPayment(@NonNull final String providerReference) {
    if (!available) {
      return PaymentResult.failure("Mock payment provider is currently unavailable");
    }

    if (!providerReference.startsWith("mock-")) {
      return PaymentResult.failure("Invalid mock payment reference: " + providerReference);
    }

    // Mock payments are always confirmed successfully
    return PaymentResult.success(providerReference);
  }

  @Override
  @NonNull
  public PaymentResult cancelPayment(@NonNull final String providerReference) {
    if (!available) {
      return PaymentResult.failure("Mock payment provider is currently unavailable");
    }

    if (!providerReference.startsWith("mock-")) {
      return PaymentResult.failure("Invalid mock payment reference: " + providerReference);
    }

    // Mock payments are always cancelled successfully
    return PaymentResult.success(providerReference);
  }

  @Override
  public boolean isAvailable() {
    return available;
  }

  /**
   * Sets the availability state of this mock provider.
   *
   * <p>This method is useful for testing error scenarios where the payment
   * provider becomes temporarily unavailable.
   *
   * @param available true to make the provider available, false to simulate unavailability
   */
  public void setAvailable(final boolean available) {
    this.available = available;
  }
}
