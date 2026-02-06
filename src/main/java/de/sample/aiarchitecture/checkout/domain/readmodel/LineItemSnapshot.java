package de.sample.aiarchitecture.checkout.domain.readmodel;

import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItemId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Read Model representing a line item snapshot from checkout state.
 *
 * <p>Contains the line item data extracted from the checkout session aggregate.
 */
public record LineItemSnapshot(
    CheckoutLineItemId lineItemId,
    ProductId productId,
    String name,
    Money price,
    int quantity)
    implements Value {

  public LineItemSnapshot {
    if (lineItemId == null) {
      throw new IllegalArgumentException("Line item ID cannot be null");
    }
    if (productId == null) {
      throw new IllegalArgumentException("Product ID cannot be null");
    }
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    if (price == null) {
      throw new IllegalArgumentException("Price cannot be null");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than zero");
    }
  }

  /**
   * Calculates the line total.
   *
   * @return price multiplied by quantity
   */
  public Money lineTotal() {
    return price.multiply(quantity);
  }
}
