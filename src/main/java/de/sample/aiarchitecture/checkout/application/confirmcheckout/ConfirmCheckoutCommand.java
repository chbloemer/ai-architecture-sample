package de.sample.aiarchitecture.checkout.application.confirmcheckout;

import org.jspecify.annotations.NonNull;

/**
 * Input model for confirming a checkout session.
 *
 * @param sessionId the checkout session ID to confirm
 */
public record ConfirmCheckoutCommand(@NonNull String sessionId) {

  /**
   * Compact constructor with validation.
   */
  public ConfirmCheckoutCommand {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("Session ID cannot be null or blank");
    }
  }
}
