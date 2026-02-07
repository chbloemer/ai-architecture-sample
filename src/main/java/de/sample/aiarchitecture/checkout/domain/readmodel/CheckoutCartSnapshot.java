package de.sample.aiarchitecture.checkout.domain.readmodel;

import de.sample.aiarchitecture.checkout.domain.model.BuyerInfo;
import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionStatus;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutStep;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutTotals;
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
 * <p>This immutable read model contains all state from the
 * {@link CheckoutSession} aggregate, providing a query-optimized view
 * for display purposes.
 *
 * <p>Use the {@link #from(CheckoutSession)} factory method to create a snapshot
 * from a checkout session aggregate.
 */
public record CheckoutCartSnapshot(
    CheckoutSessionId sessionId,
    CartId cartId,
    CustomerId customerId,
    CheckoutStep step,
    CheckoutSessionStatus status,
    List<LineItemSnapshot> lineItems,
    Money subtotal,
    @Nullable CheckoutTotals totals,
    @Nullable BuyerInfo buyerInfo,
    @Nullable DeliveryAddress deliveryAddress,
    @Nullable ShippingOption shippingOption,
    @Nullable PaymentSelection paymentSelection,
    @Nullable String orderReference)
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
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
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
   * Creates a CheckoutCartSnapshot from a CheckoutSession aggregate.
   *
   * <p>This factory method extracts all relevant state from the aggregate
   * to create an immutable read model for display purposes.
   *
   * @param session the checkout session aggregate
   * @return a snapshot of the session state
   */
  public static CheckoutCartSnapshot from(final CheckoutSession session) {
    if (session == null) {
      throw new IllegalArgumentException("Session cannot be null");
    }

    final List<LineItemSnapshot> lineItemSnapshots = session.lineItems().stream()
        .map(item -> new LineItemSnapshot(
            item.id(),
            item.productId(),
            item.productName(),
            item.unitPrice(),
            item.quantity(),
            item.imageUrl()))
        .toList();

    return new CheckoutCartSnapshot(
        session.id(),
        session.cartId(),
        session.customerId(),
        session.currentStep(),
        session.status(),
        lineItemSnapshots,
        session.totals().subtotal(),
        session.totals(),
        session.buyerInfo(),
        session.deliveryAddress(),
        session.shippingOption(),
        session.paymentSelection(),
        session.orderReference());
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

  /**
   * Checks if the checkout has totals calculated.
   *
   * @return true if totals are present
   */
  public boolean hasTotals() {
    return totals != null;
  }

  /**
   * Checks if the checkout has an order reference.
   *
   * @return true if order reference is present
   */
  public boolean hasOrderReference() {
    return orderReference != null;
  }

  /**
   * Checks if the session is active.
   *
   * @return true if status is ACTIVE
   */
  public boolean isActive() {
    return status == CheckoutSessionStatus.ACTIVE;
  }

  /**
   * Checks if the session is completed.
   *
   * @return true if status is COMPLETED
   */
  public boolean isCompleted() {
    return status == CheckoutSessionStatus.COMPLETED;
  }

  /**
   * Checks if the session is confirmed.
   *
   * @return true if status is CONFIRMED
   */
  public boolean isConfirmed() {
    return status == CheckoutSessionStatus.CONFIRMED;
  }
}
