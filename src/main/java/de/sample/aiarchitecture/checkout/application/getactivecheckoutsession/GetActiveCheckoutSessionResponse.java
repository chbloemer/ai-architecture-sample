package de.sample.aiarchitecture.checkout.application.getactivecheckoutsession;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Output model for getting an active checkout session.
 *
 * @param found whether an active session was found
 * @param sessionId the checkout session ID (null if not found)
 * @param customerId the customer ID (null if not found)
 */
public record GetActiveCheckoutSessionResponse(
    boolean found,
    @Nullable String sessionId,
    @Nullable String customerId) {

  /**
   * Creates a response indicating no active session was found.
   */
  public static GetActiveCheckoutSessionResponse notFound() {
    return new GetActiveCheckoutSessionResponse(false, null, null);
  }

  /**
   * Creates a response with the found session.
   */
  public static GetActiveCheckoutSessionResponse of(
      @NonNull final String sessionId, @NonNull final String customerId) {
    return new GetActiveCheckoutSessionResponse(true, sessionId, customerId);
  }
}
