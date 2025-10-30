package de.sample.aiarchitecture.application.cart;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Output model for cart checkout.
 *
 * @param cartId the cart ID
 * @param customerId the customer ID
 * @param items the cart items at checkout
 * @param totalAmount the total amount
 * @param totalCurrency the total currency
 * @param checkedOutAt the checkout timestamp
 */
public record CheckoutCartOutput(
    @NonNull String cartId,
    @NonNull String customerId,
    @NonNull List<CartItemSummary> items,
    @NonNull BigDecimal totalAmount,
    @NonNull String totalCurrency,
    @NonNull Instant checkedOutAt
) {

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
