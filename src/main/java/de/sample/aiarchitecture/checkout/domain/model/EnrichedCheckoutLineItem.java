package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import org.jspecify.annotations.NonNull;

/**
 * Value Object that combines a CheckoutLineItem with current CheckoutArticle data.
 *
 * <p>Enables domain logic like determining if the price has changed since the item
 * was added to the cart and whether there is sufficient stock for the requested quantity.
 */
public record EnrichedCheckoutLineItem(
    @NonNull CheckoutLineItem lineItem,
    @NonNull CheckoutArticle currentArticle)
    implements Value {

  public EnrichedCheckoutLineItem {
    if (lineItem == null) {
      throw new IllegalArgumentException("Line item cannot be null");
    }
    if (currentArticle == null) {
      throw new IllegalArgumentException("Current article cannot be null");
    }
    if (!lineItem.productId().equals(currentArticle.productId())) {
      throw new IllegalArgumentException(
          "Product ID must match between line item and current article");
    }
  }

  /**
   * Creates a new EnrichedCheckoutLineItem with the specified values.
   *
   * @param lineItem the checkout line item
   * @param currentArticle the current article data
   * @return a new EnrichedCheckoutLineItem instance
   */
  public static EnrichedCheckoutLineItem of(
      final CheckoutLineItem lineItem, final CheckoutArticle currentArticle) {
    return new EnrichedCheckoutLineItem(lineItem, currentArticle);
  }

  /**
   * Calculates the line total using the current article price.
   *
   * @return the total calculated as currentPrice * quantity
   */
  public Money currentLineTotal() {
    return currentArticle.currentPrice().multiply(lineItem.quantity());
  }

  /**
   * Returns the original line total from the line item.
   *
   * @return the original line total (unitPrice * quantity at time of adding to cart)
   */
  public Money originalLineTotal() {
    return lineItem.lineTotal();
  }

  /**
   * Checks if the price has changed since the item was added to the cart.
   *
   * @return true if the current price differs from the original unit price
   */
  public boolean hasPriceChanged() {
    return !currentArticle.currentPrice().equals(lineItem.unitPrice());
  }

  /**
   * Returns the absolute price difference between current and original unit price.
   *
   * @return the absolute difference between current price and original unit price
   */
  public Money priceDifference() {
    var currentPrice = currentArticle.currentPrice();
    var originalPrice = lineItem.unitPrice();

    if (currentPrice.isGreaterThan(originalPrice)) {
      return currentPrice.subtract(originalPrice);
    } else {
      return originalPrice.subtract(currentPrice);
    }
  }

  /**
   * Checks if there is sufficient stock for the requested quantity.
   *
   * @return true if the current article has enough stock for the line item quantity
   */
  public boolean hasSufficientStock() {
    return currentArticle.hasStockFor(lineItem.quantity());
  }

  /**
   * Checks if this line item is valid for checkout.
   *
   * <p>An item is valid for checkout if the product is available AND there is
   * sufficient stock for the requested quantity.
   *
   * @return true if the item can proceed to checkout
   */
  public boolean isValidForCheckout() {
    return currentArticle.isAvailable() && hasSufficientStock();
  }
}
