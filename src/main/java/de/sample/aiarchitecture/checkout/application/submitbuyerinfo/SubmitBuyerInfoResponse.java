package de.sample.aiarchitecture.checkout.application.submitbuyerinfo;

import org.jspecify.annotations.NonNull;

/**
 * Output model for buyer info submission.
 *
 * @param sessionId the checkout session ID
 * @param currentStep the current step after submission
 * @param status the session status
 * @param email the submitted email address
 * @param firstName the submitted first name
 * @param lastName the submitted last name
 * @param phone the submitted phone number
 */
public record SubmitBuyerInfoResponse(
    @NonNull String sessionId,
    @NonNull String currentStep,
    @NonNull String status,
    @NonNull String email,
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull String phone) {}
