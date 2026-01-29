package de.sample.aiarchitecture.checkout.application.submitdelivery;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Output model for delivery submission.
 *
 * @param sessionId the checkout session ID
 * @param currentStep the current step after submission
 * @param status the session status
 * @param street the submitted street address
 * @param streetLine2 optional second address line
 * @param city the submitted city
 * @param postalCode the submitted postal code
 * @param country the submitted country
 * @param state optional state/province
 * @param shippingOptionId the selected shipping option ID
 * @param shippingOptionName the shipping option display name
 * @param estimatedDelivery the estimated delivery time
 * @param shippingCost the shipping cost
 * @param currencyCode the currency code for shipping cost
 */
public record SubmitDeliveryResponse(
    @NonNull String sessionId,
    @NonNull String currentStep,
    @NonNull String status,
    @NonNull String street,
    @Nullable String streetLine2,
    @NonNull String city,
    @NonNull String postalCode,
    @NonNull String country,
    @Nullable String state,
    @NonNull String shippingOptionId,
    @NonNull String shippingOptionName,
    @NonNull String estimatedDelivery,
    @NonNull BigDecimal shippingCost,
    @NonNull String currencyCode) {}
