package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.Currency;
import java.util.List;
import java.util.Map;

/**
 * Enriched Domain Model representing a shopping cart with current article data.
 *
 * <p>This domain concept combines cart state from the ShoppingCart aggregate with
 * external data from the Pricing and Inventory contexts. It owns business logic
 * that requires cross-context data, such as checkout eligibility and price change detection.
 *
 * <p><b>Responsibility Split:</b>
 * <ul>
 *   <li>ShoppingCart aggregate: owns cart mutations (add/remove items, checkout)</li>
 *   <li>EnrichedCart: owns cross-context business rules (validation, calculations)</li>
 * </ul>
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
   * Creates an EnrichedCart from a ShoppingCart aggregate and current article data.
   *
   * <p>This factory method combines the cart's state with fresh article data (pricing,
   * availability, stock) to create an enriched read model suitable for display.
   *
   * @param cart the shopping cart aggregate
   * @param articleData map of product IDs to current article data
   * @return an enriched cart with current pricing and availability
   * @throws IllegalArgumentException if article data is missing for any cart item
   */
  public static EnrichedCart from(
      final ShoppingCart cart,
      final Map<ProductId, CartArticle> articleData) {
    if (cart == null) {
      throw new IllegalArgumentException("Cart cannot be null");
    }
    if (articleData == null) {
      throw new IllegalArgumentException("Article data cannot be null");
    }

    final List<EnrichedCartItem> enrichedItems = cart.items().stream()
        .map(item -> {
          final CartArticle article = articleData.get(item.productId());
          if (article == null) {
            throw new IllegalArgumentException(
                "Article data not found for product: " + item.productId().value());
          }
          return EnrichedCartItem.of(item, article);
        })
        .toList();

    return new EnrichedCart(cart.id(), cart.customerId(), enrichedItems, cart.status());
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
