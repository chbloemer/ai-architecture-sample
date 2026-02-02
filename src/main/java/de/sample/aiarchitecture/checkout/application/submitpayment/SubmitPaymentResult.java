package de.sample.aiarchitecture.checkout.application.submitpayment;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Output model for payment submission.
 *
 * @param sessionId the checkout session ID
 * @param currentStep the current step after submission
 * @param status the session status
 * @param providerId the selected payment provider ID
 * @param providerName the payment provider display name
 * @param providerReference optional provider-specific reference
 */
public record SubmitPaymentResult(
    @NonNull String sessionId,
    @NonNull String currentStep,
    @NonNull String status,
    @NonNull String providerId,
    @NonNull String providerName,
    @Nullable String providerReference) {}
