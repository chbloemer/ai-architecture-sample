package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.Currency;
import java.util.List;

/**
 * Value Object representing a 'smart shopping cart' with enriched items and business methods.
 *
 * <p>Provides business methods for validation and calculations, including running totals,
 * current prices, and alerts for price changes. This is like a shopping cart display
 * that shows all relevant information for the customer.
 */
public record EnrichedCart(
    CartId cartId,
    CustomerId customerId,
    List<EnrichedCartItem> items,
    CartStatus status)
    implements Value {

  private static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

  public EnrichedCart {
    if (cartId == null) {
      throw new IllegalArgumentException("Cart ID cannot be null");
    }
    if (customerId == null) {
      throw new IllegalArgumentException("Customer ID cannot be null");
    }
    if (items == null) {
      throw new IllegalArgumentException("Items cannot be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
    // Make defensive copy to ensure immutability
    items = List.copyOf(items);
  }

  /**
   * Creates a new EnrichedCart with the specified values.
   *
   * @param cartId the cart identifier
   * @param customerId the customer identifier
   * @param items the list of enriched cart items
   * @param status the cart status
   * @return a new EnrichedCart instance
   */
  public static EnrichedCart of(
      final CartId cartId,
      final CustomerId customerId,
      final List<EnrichedCartItem> items,
      final CartStatus status) {
    return new EnrichedCart(cartId, customerId, items, status);
  }

  /**
   * Calculates the subtotal using current article prices.
   *
   * @return the sum of all current line totals
   */
  public Money calculateCurrentSubtotal() {
    return items.stream()
        .map(EnrichedCartItem::currentLineTotal)
        .reduce(Money.zero(DEFAULT_CURRENCY), Money::add);
  }

  /**
   * Calculates the subtotal using original prices from when items were added.
   *
   * @return the sum of all original line totals
   */
  public Money calculateOriginalSubtotal() {
    return items.stream()
        .map(EnrichedCartItem::originalLineTotal)
        .reduce(Money.zero(DEFAULT_CURRENCY), Money::add);
  }

  /**
   * Calculates the total price difference between current and original subtotals.
   *
   * @return the absolute difference between current and original subtotals
   */
  public Money totalPriceDifference() {
    var current = calculateCurrentSubtotal();
    var original = calculateOriginalSubtotal();

    if (current.isGreaterThan(original)) {
      return current.subtract(original);
    } else {
      return original.subtract(current);
    }
  }

  /**
   * Checks if any items have price changes since they were added to the cart.
   *
   * @return true if at least one item has a price change
   */
  public boolean hasAnyPriceChanges() {
    return items.stream().anyMatch(EnrichedCartItem::hasPriceChanged);
  }

  /**
   * Returns all items that have price changes since they were added.
   *
   * @return list of items with price changes
   */
  public List<EnrichedCartItem> itemsWithPriceChanges() {
    return items.stream()
        .filter(EnrichedCartItem::hasPriceChanged)
        .toList();
  }

  /**
   * Checks if all items in the cart are valid for checkout.
   *
   * <p>A cart is valid for checkout if it is active, not empty, and all items
   * are available with sufficient stock.
   *
   * @return true if the cart can proceed to checkout
   */
  public boolean isValidForCheckout() {
    return status == CartStatus.ACTIVE
        && !items.isEmpty()
        && items.stream().allMatch(EnrichedCartItem::isValidForCheckout);
  }

  /**
   * Returns all items that are not valid for checkout.
   *
   * @return list of items that cannot proceed to checkout
   */
  public List<EnrichedCartItem> invalidItems() {
    return items.stream()
        .filter(item -> !item.isValidForCheckout())
        .toList();
  }
}
