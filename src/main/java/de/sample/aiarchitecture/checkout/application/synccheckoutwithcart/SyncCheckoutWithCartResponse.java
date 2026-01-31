package de.sample.aiarchitecture.checkout.application.synccheckoutwithcart;

import org.jspecify.annotations.NonNull;

/**
 * Response from synchronizing a checkout session with cart.
 *
 * @param synced true if an active checkout session was found and synced
 * @param sessionId the checkout session ID if synced, null otherwise
 * @param itemCount the new number of line items after sync
 */
public record SyncCheckoutWithCartResponse(
    boolean synced,
    String sessionId,
    int itemCount) {

  public static SyncCheckoutWithCartResponse noActiveSession() {
    return new SyncCheckoutWithCartResponse(false, null, 0);
  }

  public static SyncCheckoutWithCartResponse synced(
      @NonNull final String sessionId,
      final int itemCount) {
    return new SyncCheckoutWithCartResponse(true, sessionId, itemCount);
  }
}
