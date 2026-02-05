package de.sample.aiarchitecture.checkout.application.shared;

import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.PaymentProviderId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;

/**
 * Output port for payment processing operations.
 *
 * <p>Represents a payment provider that can process payments. Different implementations
 * can integrate with various payment systems (Stripe, PayPal, bank transfers, etc.).
 *
 * <p>This is a plugin interface following the Strategy pattern, allowing the checkout
 * system to work with multiple payment providers without coupling to specific implementations.
 */
public interface PaymentProvider extends OutputPort {

  /**
   * Returns the unique identifier of this payment provider.
   *
   * @return the payment provider ID
   */
  
  PaymentProviderId providerId();

  /**
   * Returns a human-readable name for this payment provider.
   *
   * @return the display name
   */
  
  String displayName();

  /**
   * Initiates a payment for the given checkout session.
   *
   * <p>This creates a payment intent or equivalent with the provider. The returned
   * result contains a provider-specific reference that can be used to track or
   * complete the payment.
   *
   * @param sessionId the checkout session to process payment for
   * @param amount the total amount to charge
   * @return the result of the payment initiation
   */
  
  PaymentResult initiatePayment(CheckoutSessionId sessionId, Money amount);

  /**
   * Confirms a previously initiated payment.
   *
   * <p>This is called after the customer has authorized the payment (if required
   * by the provider). Some providers may complete payment in the initiation step,
   * in which case this method may be a no-op.
   *
   * @param providerReference the reference returned from {@link #initiatePayment}
   * @return the result of the payment confirmation
   */
  
  PaymentResult confirmPayment(String providerReference);

  /**
   * Cancels a previously initiated payment.
   *
   * <p>This releases any held funds and marks the payment as cancelled with the provider.
   *
   * @param providerReference the reference returned from {@link #initiatePayment}
   * @return the result of the cancellation
   */
  
  PaymentResult cancelPayment(String providerReference);

  /**
   * Checks whether this provider is currently available for processing payments.
   *
   * @return true if the provider is available, false otherwise
   */
  boolean isAvailable();

  /**
   * Result of a payment operation.
   *
   * @param success whether the operation succeeded
   * @param providerReference unique reference from the provider for tracking
   * @param errorMessage error message if the operation failed, null otherwise
   */
  record PaymentResult(boolean success, String providerReference, String errorMessage) {

    public static PaymentResult success(final String providerReference) {
      return new PaymentResult(true, providerReference, null);
    }

    public static PaymentResult failure(final String errorMessage) {
      return new PaymentResult(false, null, errorMessage);
    }
  }
}
