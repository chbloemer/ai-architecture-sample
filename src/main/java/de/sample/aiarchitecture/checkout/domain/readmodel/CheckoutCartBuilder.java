package de.sample.aiarchitecture.checkout.domain.readmodel;

import de.sample.aiarchitecture.checkout.domain.model.BuyerInfo;
import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItemId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionStatus;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutStateInterest;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutStep;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutTotals;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.checkout.domain.model.DeliveryAddress;
import de.sample.aiarchitecture.checkout.domain.model.PaymentSelection;
import de.sample.aiarchitecture.checkout.domain.model.ShippingOption;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.ReadModelBuilder;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Builder that constructs {@link CheckoutCartSnapshot} read models by implementing
 * {@link CheckoutStateInterest}.
 *
 * <p>This builder receives state from the {@link de.sample.aiarchitecture.checkout.domain.model.CheckoutSession}
 * aggregate via the Interest Interface Pattern. The aggregate calls the {@code receive*()} methods
 * to push its state, and then {@link #build()} is called to construct the immutable read model.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * CheckoutCartBuilder builder = new CheckoutCartBuilder();
 * checkoutSession.provideStateTo(builder);
 * CheckoutCartSnapshot snapshot = builder.build();
 * }</pre>
 *
 * <p>The builder is reusable. Call {@link #reset()} to clear internal state and build another
 * read model from a different aggregate.
 *
 * @see CheckoutStateInterest
 * @see CheckoutCartSnapshot
 * @see ReadModelBuilder
 */
public class CheckoutCartBuilder implements CheckoutStateInterest, ReadModelBuilder {

  private @Nullable CheckoutSessionId sessionId;
  private @Nullable CartId cartId;
  private @Nullable CustomerId customerId;
  private @Nullable CheckoutStep step;
  private @Nullable CheckoutSessionStatus status;
  private final List<LineItemSnapshot> lineItems = new ArrayList<>();
  private @Nullable Money subtotal;
  private @Nullable CheckoutTotals totals;
  private @Nullable BuyerInfo buyerInfo;
  private @Nullable DeliveryAddress deliveryAddress;
  private @Nullable ShippingOption shippingOption;
  private @Nullable PaymentSelection paymentSelection;
  private @Nullable String orderReference;

  /**
   * Creates a new CheckoutCartBuilder.
   */
  public CheckoutCartBuilder() {
    // Default constructor
  }

  @Override
  public void receiveSessionId(final CheckoutSessionId sessionId) {
    this.sessionId = sessionId;
  }

  @Override
  public void receiveCartId(final CartId cartId) {
    this.cartId = cartId;
  }

  @Override
  public void receiveCustomerId(final CustomerId customerId) {
    this.customerId = customerId;
  }

  @Override
  public void receiveStep(final CheckoutStep step) {
    this.step = step;
  }

  @Override
  public void receiveLineItem(
      final CheckoutLineItemId lineItemId,
      final ProductId productId,
      final String name,
      final Money price,
      final int quantity) {
    lineItems.add(new LineItemSnapshot(lineItemId, productId, name, price, quantity));
  }

  @Override
  public void receiveSubtotal(final Money subtotal) {
    this.subtotal = subtotal;
  }

  @Override
  public void receiveBuyerInfo(final @Nullable BuyerInfo buyerInfo) {
    this.buyerInfo = buyerInfo;
  }

  @Override
  public void receiveDeliveryAddress(final @Nullable DeliveryAddress deliveryAddress) {
    this.deliveryAddress = deliveryAddress;
  }

  @Override
  public void receiveShippingOption(final @Nullable ShippingOption shippingOption) {
    this.shippingOption = shippingOption;
  }

  @Override
  public void receivePaymentSelection(final @Nullable PaymentSelection paymentSelection) {
    this.paymentSelection = paymentSelection;
  }

  @Override
  public void receiveStatus(final CheckoutSessionStatus status) {
    this.status = status;
  }

  @Override
  public void receiveOrderReference(final @Nullable String orderReference) {
    this.orderReference = orderReference;
  }

  @Override
  public void receiveTotals(final CheckoutTotals totals) {
    this.totals = totals;
  }

  /**
   * Returns the received session status.
   *
   * @return the session status, or null if not received
   */
  public @Nullable CheckoutSessionStatus getStatus() {
    return status;
  }

  /**
   * Returns the received order reference.
   *
   * @return the order reference, or null if not received
   */
  public @Nullable String getOrderReference() {
    return orderReference;
  }

  /**
   * Returns the received checkout totals.
   *
   * @return the checkout totals, or null if not received
   */
  public @Nullable CheckoutTotals getTotals() {
    return totals;
  }

  /**
   * Builds the immutable {@link CheckoutCartSnapshot} from the received state.
   *
   * <p>All required state (sessionId, cartId, customerId, step, subtotal) must have been
   * received before calling this method.
   *
   * @return the constructed CheckoutCartSnapshot
   * @throws IllegalStateException if required state has not been received
   */
  public CheckoutCartSnapshot build() {
    if (sessionId == null) {
      throw new IllegalStateException("Session ID has not been received");
    }
    if (cartId == null) {
      throw new IllegalStateException("Cart ID has not been received");
    }
    if (customerId == null) {
      throw new IllegalStateException("Customer ID has not been received");
    }
    if (step == null) {
      throw new IllegalStateException("Step has not been received");
    }
    if (subtotal == null) {
      throw new IllegalStateException("Subtotal has not been received");
    }

    return new CheckoutCartSnapshot(
        sessionId,
        cartId,
        customerId,
        step,
        List.copyOf(lineItems),
        subtotal,
        buyerInfo,
        deliveryAddress,
        shippingOption,
        paymentSelection);
  }

  /**
   * Resets the builder to its initial state.
   *
   * <p>Call this method to reuse the builder for constructing another read model.
   */
  public void reset() {
    sessionId = null;
    cartId = null;
    customerId = null;
    step = null;
    status = null;
    lineItems.clear();
    subtotal = null;
    totals = null;
    buyerInfo = null;
    deliveryAddress = null;
    shippingOption = null;
    paymentSelection = null;
    orderReference = null;
  }
}
