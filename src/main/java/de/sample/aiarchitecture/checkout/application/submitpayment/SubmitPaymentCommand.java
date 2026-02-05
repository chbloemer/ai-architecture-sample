package de.sample.aiarchitecture.checkout.application.submitpayment;
import org.jspecify.annotations.Nullable;

/**
 * Input model for submitting payment information during checkout.
 *
 * @param sessionId the checkout session ID
 * @param providerId the selected payment provider ID
 * @param providerReference optional provider-specific reference (e.g., payment intent ID)
 */
public record SubmitPaymentCommand(
    String sessionId,
    String providerId,
    @Nullable String providerReference) {

  /**
   * Compact constructor with validation.
   */
  public SubmitPaymentCommand {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("Session ID cannot be null or blank");
    }
    if (providerId == null || providerId.isBlank()) {
      throw new IllegalArgumentException("Provider ID cannot be null or blank");
    }
  }
}
