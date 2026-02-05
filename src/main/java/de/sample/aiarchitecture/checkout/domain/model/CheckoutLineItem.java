package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Value Object representing a line item in the checkout.
 *
 * <p>Contains product information, quantity, and calculated totals for a single
 * item in the checkout session.
 */
public record CheckoutLineItem(
    CheckoutLineItemId id,
    ProductId productId,
    String productName,
    Money unitPrice,
    int quantity)
    implements Value {

  public CheckoutLineItem {
    if (id == null) {
      throw new IllegalArgumentException("Line item ID cannot be null");
    }
    if (productId == null) {
      throw new IllegalArgumentException("Product ID cannot be null");
    }
    if (productName == null || productName.isBlank()) {
      throw new IllegalArgumentException("Product name cannot be null or blank");
    }
    if (unitPrice == null) {
      throw new IllegalArgumentException("Unit price cannot be null");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than zero");
    }
  }

  public static CheckoutLineItem of(
      final CheckoutLineItemId id,
      final ProductId productId,
      final String productName,
      final Money unitPrice,
      final int quantity) {
    return new CheckoutLineItem(id, productId, productName, unitPrice, quantity);
  }

  public Money lineTotal() {
    return unitPrice.multiply(quantity);
  }

  public CheckoutLineItem withQuantity(final int newQuantity) {
    return new CheckoutLineItem(id, productId, productName, unitPrice, newQuantity);
  }
}
