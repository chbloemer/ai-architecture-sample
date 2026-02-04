package de.sample.aiarchitecture.cart.application.getcartbyid;

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
public record GetCartByIdResult(
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
  public static GetCartByIdResult notFound() {
    return new GetCartByIdResult(false, null, null, null, null, null, null);
  }

  /**
   * Creates an output for a cart that was found.
   */
  public static GetCartByIdResult found(
      String cartId,
      String customerId,
      String status,
      List<CartItemSummary> items,
      BigDecimal totalAmount,
      String totalCurrency) {
    return new GetCartByIdResult(true, cartId, customerId, status, items, totalAmount, totalCurrency);
  }

  /**
   * Summary of a cart item with fresh pricing and availability data.
   *
   * @param itemId the cart item ID
   * @param productId the product ID
   * @param quantity the quantity
   * @param unitPriceAmount the unit price amount (price at addition)
   * @param unitPriceCurrency the unit price currency
   * @param currentPriceAmount the current price amount (fresh from pricing service)
   * @param currentPriceCurrency the current price currency
   * @param isAvailable whether the product is currently available
   * @param priceChanged true if currentPrice differs from priceAtAddition
   */
  public record CartItemSummary(
      @NonNull String itemId,
      @NonNull String productId,
      int quantity,
      @NonNull BigDecimal unitPriceAmount,
      @NonNull String unitPriceCurrency,
      @Nullable BigDecimal currentPriceAmount,
      @Nullable String currentPriceCurrency,
      boolean isAvailable,
      boolean priceChanged
  ) {}
}
