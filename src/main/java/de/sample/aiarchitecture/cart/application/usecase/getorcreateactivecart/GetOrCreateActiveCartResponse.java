package de.sample.aiarchitecture.cart.application.usecase.getorcreateactivecart;

import org.jspecify.annotations.NonNull;

/**
 * Output model for getting or creating an active cart.
 *
 * @param cartId the cart ID
 * @param customerId the customer ID
 * @param wasCreated whether a new cart was created (true) or existing was found (false)
 */
public record GetOrCreateActiveCartResponse(
    @NonNull String cartId,
    @NonNull String customerId,
    boolean wasCreated
) {}
