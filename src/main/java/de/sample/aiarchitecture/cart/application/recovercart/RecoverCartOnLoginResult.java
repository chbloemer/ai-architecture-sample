package de.sample.aiarchitecture.cart.application.recovercart;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Output model for recovering cart on login.
 *
 * @param cartId the resulting cart ID (registered user's cart)
 * @param customerId the registered user's customer ID
 * @param items the merged list of cart items
 * @param totalAmount the total cart amount after merge
 * @param totalCurrency the total cart currency
 * @param itemsMerged number of items merged from anonymous cart
 * @param anonymousCartDeleted whether the anonymous cart was deleted
 */
public record RecoverCartOnLoginResult(
    @Nullable String cartId,
    @NonNull String customerId,
    @NonNull List<CartItemSummary> items,
    @NonNull BigDecimal totalAmount,
    @NonNull String totalCurrency,
    int itemsMerged,
    boolean anonymousCartDeleted
) {

  /**
   * Creates a response when no cart recovery was needed (no anonymous cart existed).
   */
  public static RecoverCartOnLoginResult noRecoveryNeeded(String customerId) {
    return new RecoverCartOnLoginResult(
        null,
        customerId,
        List.of(),
        BigDecimal.ZERO,
        "EUR",
        0,
        false
    );
  }

  /**
   * Summary of a cart item.
   *
   * @param itemId the cart item ID
   * @param productId the product ID
   * @param quantity the quantity
   * @param unitPriceAmount the unit price amount
   * @param unitPriceCurrency the unit price currency
   */
  public record CartItemSummary(
      @NonNull String itemId,
      @NonNull String productId,
      int quantity,
      @NonNull BigDecimal unitPriceAmount,
      @NonNull String unitPriceCurrency
  ) {}
}
