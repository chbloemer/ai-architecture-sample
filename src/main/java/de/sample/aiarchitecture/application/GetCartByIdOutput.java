package de.sample.aiarchitecture.application;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Output model for cart retrieval by ID.
 *
 * <p>All fields (except found) are nullable to represent the case where cart is not found.
 * Check {@link #found()} to determine if the cart exists.
 *
 * @param found whether the cart was found
 * @param cartId the cart ID (null if not found)
 * @param customerId the customer ID (null if not found)
 * @param status the cart status (null if not found)
 * @param items the cart items (null if not found)
 * @param totalAmount the total amount (null if not found)
 * @param totalCurrency the total currency (null if not found)
 */
public record GetCartByIdOutput(
    boolean found,
    @Nullable String cartId,
    @Nullable String customerId,
    @Nullable String status,
    @Nullable List<CartItemSummary> items,
    @Nullable BigDecimal totalAmount,
    @Nullable String totalCurrency
) {

  /**
   * Creates an output for a cart that was not found.
   */
  public static GetCartByIdOutput notFound() {
    return new GetCartByIdOutput(false, null, null, null, null, null, null);
  }

  /**
   * Creates an output for a cart that was found.
   */
  public static GetCartByIdOutput found(
      String cartId,
      String customerId,
      String status,
      List<CartItemSummary> items,
      BigDecimal totalAmount,
      String totalCurrency) {
    return new GetCartByIdOutput(true, cartId, customerId, status, items, totalAmount, totalCurrency);
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
