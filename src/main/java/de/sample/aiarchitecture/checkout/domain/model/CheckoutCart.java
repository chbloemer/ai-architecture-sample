package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.Currency;
import java.util.List;

/**
 * Value Object representing a 'smart shopping cart' that contains enriched line items.
 *
 * <p>Provides business methods for validation and calculations, displaying running totals,
 * current prices, and alerts for price changes. This is like a shopping cart with a display
 * that shows all relevant checkout information.
 */
public record CheckoutCart(
    CartId cartId,
    CustomerId customerId,
    List<EnrichedCheckoutLineItem> items)
    implements Value {

  private static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

  public CheckoutCart {
    if (cartId == null) {
      throw new IllegalArgumentException("Cart ID cannot be null");
    }
    if (customerId == null) {
      throw new IllegalArgumentException("Customer ID cannot be null");
    }
    if (items == null) {
      throw new IllegalArgumentException("Items cannot be null");
    }
    // Make defensive copy to ensure immutability
    items = List.copyOf(items);
  }

  /**
   * Creates a new CheckoutCart with the specified values.
   *
   * @param cartId the cart identifier
   * @param customerId the customer identifier
   * @param items the list of enriched checkout line items
   * @return a new CheckoutCart instance
   */
  public static CheckoutCart of(
      final CartId cartId,
      final CustomerId customerId,
      final List<EnrichedCheckoutLineItem> items) {
    return new CheckoutCart(cartId, customerId, items);
  }

  /**
   * Calculates the subtotal using current article prices.
   *
   * @return the sum of all current line totals
   */
  public Money calculateCurrentSubtotal() {
    return items.stream()
        .map(EnrichedCheckoutLineItem::currentLineTotal)
        .reduce(Money.zero(DEFAULT_CURRENCY), Money::add);
  }

  /**
   * Calculates the subtotal using original prices from when items were added.
   *
   * @return the sum of all original line totals
   */
  public Money calculateOriginalSubtotal() {
    return items.stream()
        .map(EnrichedCheckoutLineItem::originalLineTotal)
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
    return items.stream().anyMatch(EnrichedCheckoutLineItem::hasPriceChanged);
  }

  /**
   * Returns all items that have price changes since they were added.
   *
   * @return list of items with price changes
   */
  public List<EnrichedCheckoutLineItem> itemsWithPriceChanges() {
    return items.stream()
        .filter(EnrichedCheckoutLineItem::hasPriceChanged)
        .toList();
  }

  /**
   * Checks if all items in the cart are valid for checkout.
   *
   * <p>A cart is valid for checkout if all items are available and have sufficient stock.
   *
   * @return true if all items can proceed to checkout
   */
  public boolean isValidForCheckout() {
    return !items.isEmpty() && items.stream().allMatch(EnrichedCheckoutLineItem::isValidForCheckout);
  }

  /**
   * Returns all items that are not valid for checkout.
   *
   * @return list of items that cannot proceed to checkout
   */
  public List<EnrichedCheckoutLineItem> invalidItems() {
    return items.stream()
        .filter(item -> !item.isValidForCheckout())
        .toList();
  }

  /**
   * Returns all items that are unavailable.
   *
   * @return list of items where the product is not available
   */
  public List<EnrichedCheckoutLineItem> unavailableItems() {
    return items.stream()
        .filter(item -> !item.currentArticle().isAvailable())
        .toList();
  }

  /**
   * Returns all items with insufficient stock.
   *
   * @return list of items where stock is less than requested quantity
   */
  public List<EnrichedCheckoutLineItem> itemsWithInsufficientStock() {
    return items.stream()
        .filter(item -> !item.hasSufficientStock())
        .toList();
  }

  /**
   * Returns the number of distinct line items in the cart.
   *
   * @return the count of line items
   */
  public int itemCount() {
    return items.size();
  }

  /**
   * Returns the total quantity of all items in the cart.
   *
   * @return the sum of all item quantities
   */
  public int totalQuantity() {
    return items.stream()
        .mapToInt(item -> item.lineItem().quantity())
        .sum();
  }
}
