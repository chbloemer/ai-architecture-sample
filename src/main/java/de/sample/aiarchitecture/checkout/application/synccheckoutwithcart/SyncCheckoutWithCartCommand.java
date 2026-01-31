package de.sample.aiarchitecture.checkout.application.synccheckoutwithcart;

import org.jspecify.annotations.NonNull;

/**
 * Command for synchronizing a checkout session with current cart state.
 *
 * @param cartId the ID of the cart that changed
 */
public record SyncCheckoutWithCartCommand(@NonNull String cartId) {
}
