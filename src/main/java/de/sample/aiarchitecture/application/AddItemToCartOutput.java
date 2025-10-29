package de.sample.aiarchitecture.application;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Output model for adding an item to cart.
 *
 * @param cartId the cart ID
 * @param customerId the customer ID
 * @param items the updated list of cart items
 * @param totalAmount the total cart amount
 * @param totalCurrency the total cart currency
 */
public record AddItemToCartOutput(
    @NonNull String cartId,
    @NonNull String customerId,
    @NonNull List<CartItemSummary> items,
    @NonNull BigDecimal totalAmount,
    @NonNull String totalCurrency
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
