package de.sample.aiarchitecture.checkout.application.confirmcheckout;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Output model for checkout confirmation.
 *
 * @param sessionId the checkout session ID
 * @param currentStep the current step after confirmation (CONFIRMATION)
 * @param status the session status (CONFIRMED)
 * @param cartId the original cart ID
 * @param customerId the customer ID
 * @param totalAmount the total amount in the smallest currency unit
 * @param currency the currency code
 * @param orderReference optional order reference if available
 */
public record ConfirmCheckoutResponse(
    @NonNull String sessionId,
    @NonNull String currentStep,
    @NonNull String status,
    @NonNull String cartId,
    @NonNull String customerId,
    @NonNull String totalAmount,
    @NonNull String currency,
    @Nullable String orderReference) {}
