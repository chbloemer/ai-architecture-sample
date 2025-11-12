package de.sample.aiarchitecture.cart.application.getallcarts;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Output model for retrieving all shopping carts.
 *
 * @param carts the list of cart summaries
 */
public record GetAllCartsResponse(@NonNull List<CartSummary> carts) {

  /**
   * Summary of a shopping cart.
   *
   * @param cartId the cart ID
   * @param customerId the customer ID
   * @param status the cart status
   * @param itemCount the number of items in the cart
   * @param totalAmount the total amount
   * @param totalCurrency the total currency
   */
  public record CartSummary(
      @NonNull String cartId,
      @NonNull String customerId,
      @NonNull String status,
      int itemCount,
      @NonNull BigDecimal totalAmount,
      @NonNull String totalCurrency
  ) {}
}
