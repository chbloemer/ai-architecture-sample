package de.sample.aiarchitecture.cart.application.completecart;

import org.jspecify.annotations.NonNull;

/**
 * Output model for cart completion.
 *
 * @param cartId the cart ID
 * @param status the new cart status (COMPLETED)
 */
public record CompleteCartResult(
    @NonNull String cartId,
    @NonNull String status
) {}
