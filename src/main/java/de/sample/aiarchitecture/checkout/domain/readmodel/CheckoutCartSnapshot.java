package de.sample.aiarchitecture.checkout.domain.readmodel;

import de.sample.aiarchitecture.checkout.domain.model.BuyerInfo;
import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutStep;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.checkout.domain.model.DeliveryAddress;
import de.sample.aiarchitecture.checkout.domain.model.PaymentSelection;
import de.sample.aiarchitecture.checkout.domain.model.ShippingOption;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Read Model representing a snapshot of checkout session state.
 *
 * <p>This immutable read model is built by {@link CheckoutCartBuilder} using the
 * Interest Interface Pattern. It contains all state pushed from the
 * {@link de.sample.aiarchitecture.checkout.domain.model.CheckoutSession} aggregate
 * via the {@link de.sample.aiarchitecture.checkout.domain.model.CheckoutStateInterest}
 * interface.
 *
 * <p>Unlike the enriched {@link de.sample.aiarchitecture.checkout.domain.model.CheckoutCart},
 * this read model represents the aggregate's internal state snapshot without
 * additional enrichment from external services.
 *
 * @see CheckoutCartBuilder
 */
public record CheckoutCartSnapshot(
    CheckoutSessionId sessionId,
    CartId cartId,
    CustomerId customerId,
    CheckoutStep step,
    List<LineItemSnapshot> lineItems,
    Money subtotal,
    @Nullable BuyerInfo buyerInfo,
    @Nullable DeliveryAddress deliveryAddress,
    @Nullable ShippingOption shippingOption,
    @Nullable PaymentSelection paymentSelection)
    implements Value {

  public CheckoutCartSnapshot {
    if (sessionId == null) {
      throw new IllegalArgumentException("Session ID cannot be null");
    }
    if (cartId == null) {
      throw new IllegalArgumentException("Cart ID cannot be null");
    }
    if (customerId == null) {
      throw new IllegalArgumentException("Customer ID cannot be null");
    }
    if (step == null) {
      throw new IllegalArgumentException("Step cannot be null");
    }
    if (lineItems == null) {
      throw new IllegalArgumentException("Line items cannot be null");
    }
    if (subtotal == null) {
      throw new IllegalArgumentException("Subtotal cannot be null");
    }
    // Make defensive copy
    lineItems = List.copyOf(lineItems);
  }

  /**
   * Returns the number of distinct line items.
   *
   * @return the count of line items
   */
  public int itemCount() {
    return lineItems.size();
  }

  /**
   * Returns the total quantity across all items.
   *
   * @return the sum of all item quantities
   */
  public int totalQuantity() {
    return lineItems.stream()
        .mapToInt(LineItemSnapshot::quantity)
        .sum();
  }

  /**
   * Checks if buyer info has been submitted.
   *
   * @return true if buyer info is present
   */
  public boolean hasBuyerInfo() {
    return buyerInfo != null;
  }

  /**
   * Checks if delivery address has been submitted.
   *
   * @return true if delivery address is present
   */
  public boolean hasDeliveryAddress() {
    return deliveryAddress != null;
  }

  /**
   * Checks if shipping option has been selected.
   *
   * @return true if shipping option is present
   */
  public boolean hasShippingOption() {
    return shippingOption != null;
  }

  /**
   * Checks if payment has been selected.
   *
   * @return true if payment selection is present
   */
  public boolean hasPaymentSelection() {
    return paymentSelection != null;
  }
}
