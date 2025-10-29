package de.sample.aiarchitecture.application;

import org.jspecify.annotations.NonNull;

/**
 * Output model for cart creation.
 *
 * @param cartId the generated cart ID
 * @param customerId the customer ID
 * @param status the cart status (always "ACTIVE" for new carts)
 */
public record CreateCartOutput(
    @NonNull String cartId,
    @NonNull String customerId,
    @NonNull String status
) {}
