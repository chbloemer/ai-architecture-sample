package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.checkout.domain.event.BuyerInfoSubmitted;
import de.sample.aiarchitecture.checkout.domain.event.CheckoutAbandoned;
import de.sample.aiarchitecture.checkout.domain.event.CheckoutCompleted;
import de.sample.aiarchitecture.checkout.domain.event.CheckoutConfirmed;
import de.sample.aiarchitecture.checkout.domain.event.CheckoutExpired;
import de.sample.aiarchitecture.checkout.domain.event.CheckoutSessionStarted;
import de.sample.aiarchitecture.checkout.domain.event.DeliverySubmitted;
import de.sample.aiarchitecture.checkout.domain.event.PaymentSubmitted;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.marker.BaseAggregateRoot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * CheckoutSession Aggregate Root.
 *
 * <p>Represents a customer's checkout session with state management across
 * a 5-step checkout flow: Buyer Info → Delivery → Payment → Review → Confirmation.
 *
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Cannot skip steps - must complete each step in order</li>
 *   <li>Can go back to previous steps to modify data</li>
 *   <li>Cannot modify a confirmed, completed, abandoned, or expired session</li>
 *   <li>Must have at least one line item to start checkout</li>
 *   <li>Must complete all steps before confirmation</li>
 * </ul>
 *
 * <p><b>Domain Events:</b>
 * <ul>
 *   <li>{@link CheckoutSessionStarted} - when checkout begins from cart</li>
 *   <li>{@link BuyerInfoSubmitted} - when buyer info step is completed</li>
 *   <li>{@link DeliverySubmitted} - when delivery step is completed</li>
 *   <li>{@link PaymentSubmitted} - when payment step is completed</li>
 *   <li>{@link CheckoutConfirmed} - when order is confirmed at review</li>
 *   <li>{@link CheckoutCompleted} - when checkout is fully processed</li>
 *   <li>{@link CheckoutAbandoned} - when customer abandons checkout</li>
 *   <li>{@link CheckoutExpired} - when session expires due to inactivity</li>
 * </ul>
 */
public final class CheckoutSession extends BaseAggregateRoot<CheckoutSession, CheckoutSessionId> {

  private final CheckoutSessionId id;
  private final CartId cartId;
  private final CustomerId customerId;
  private final List<CheckoutLineItem> lineItems;
  private CheckoutTotals totals;
  private CheckoutStep currentStep;
  private CheckoutSessionStatus status;

  // Step data (nullable until step is completed)
  private BuyerInfo buyerInfo;
  private DeliveryAddress deliveryAddress;
  private ShippingOption shippingOption;
  private PaymentSelection paymentSelection;

  // Order reference after completion
  private String orderReference;

  private CheckoutSession(
      @NonNull final CheckoutSessionId id,
      @NonNull final CartId cartId,
      @NonNull final CustomerId customerId,
      @NonNull final List<CheckoutLineItem> lineItems,
      @NonNull final Money subtotal) {
    this.id = id;
    this.cartId = cartId;
    this.customerId = customerId;
    this.lineItems = new ArrayList<>(lineItems);
    this.totals = CheckoutTotals.of(
        subtotal,
        Money.zero(subtotal.currency()),
        Money.zero(subtotal.currency()),
        subtotal);
    this.currentStep = CheckoutStep.BUYER_INFO;
    this.status = CheckoutSessionStatus.ACTIVE;
  }

  /**
   * Factory method to start a new checkout session.
   *
   * <p>Raises a {@link CheckoutSessionStarted} domain event.
   *
   * @param cartId the cart ID this checkout originates from
   * @param customerId the customer ID (may be guest)
   * @param lineItems the line items from the cart
   * @param subtotal the subtotal of all line items
   * @return a new checkout session
   * @throws IllegalArgumentException if lineItems is empty
   */
  public static CheckoutSession start(
      @NonNull final CartId cartId,
      @NonNull final CustomerId customerId,
      @NonNull final List<CheckoutLineItem> lineItems,
      @NonNull final Money subtotal) {
    if (lineItems == null || lineItems.isEmpty()) {
      throw new IllegalArgumentException("Cannot start checkout with empty line items");
    }

    final CheckoutSessionId sessionId = CheckoutSessionId.generate();
    final CheckoutSession session =
        new CheckoutSession(sessionId, cartId, customerId, lineItems, subtotal);

    session.registerEvent(
        CheckoutSessionStarted.now(sessionId, cartId, customerId, subtotal, lineItems.size()));

    return session;
  }

  @Override
  public CheckoutSessionId id() {
    return id;
  }

  public CartId cartId() {
    return cartId;
  }

  public CustomerId customerId() {
    return customerId;
  }

  public List<CheckoutLineItem> lineItems() {
    return Collections.unmodifiableList(lineItems);
  }

  public CheckoutTotals totals() {
    return totals;
  }

  public CheckoutStep currentStep() {
    return currentStep;
  }

  public CheckoutSessionStatus status() {
    return status;
  }

  @Nullable
  public BuyerInfo buyerInfo() {
    return buyerInfo;
  }

  @Nullable
  public DeliveryAddress deliveryAddress() {
    return deliveryAddress;
  }

  @Nullable
  public ShippingOption shippingOption() {
    return shippingOption;
  }

  @Nullable
  public PaymentSelection paymentSelection() {
    return paymentSelection;
  }

  @Nullable
  public String orderReference() {
    return orderReference;
  }

  /**
   * Submits buyer information for the checkout.
   *
   * <p>Raises a {@link BuyerInfoSubmitted} domain event.
   *
   * @param buyerInfo the buyer contact information
   * @throws IllegalStateException if session is not modifiable or if trying to skip steps
   */
  public void submitBuyerInfo(@NonNull final BuyerInfo buyerInfo) {
    ensureModifiable();
    ensureAtOrBeforeStep(CheckoutStep.BUYER_INFO);

    this.buyerInfo = buyerInfo;

    // Advance to next step if currently at buyer info step
    if (currentStep == CheckoutStep.BUYER_INFO) {
      this.currentStep = CheckoutStep.DELIVERY;
    }

    registerEvent(BuyerInfoSubmitted.now(this.id, buyerInfo));
  }

  /**
   * Submits delivery information for the checkout.
   *
   * <p>Raises a {@link DeliverySubmitted} domain event.
   *
   * @param address the delivery address
   * @param shippingOption the selected shipping option
   * @throws IllegalStateException if session is not modifiable or if trying to skip steps
   */
  public void submitDelivery(
      @NonNull final DeliveryAddress address, @NonNull final ShippingOption shippingOption) {
    ensureModifiable();
    ensureStepCompleted(CheckoutStep.BUYER_INFO);
    ensureAtOrBeforeStep(CheckoutStep.DELIVERY);

    this.deliveryAddress = address;
    this.shippingOption = shippingOption;

    // Update totals with shipping cost
    this.totals = this.totals.withShipping(shippingOption.cost());

    // Advance to next step if currently at delivery step
    if (currentStep == CheckoutStep.DELIVERY) {
      this.currentStep = CheckoutStep.PAYMENT;
    }

    registerEvent(DeliverySubmitted.now(this.id, address, shippingOption));
  }

  /**
   * Submits payment method selection for the checkout.
   *
   * <p>Raises a {@link PaymentSubmitted} domain event.
   *
   * @param payment the payment method selection
   * @throws IllegalStateException if session is not modifiable or if trying to skip steps
   */
  public void submitPayment(@NonNull final PaymentSelection payment) {
    ensureModifiable();
    ensureStepCompleted(CheckoutStep.BUYER_INFO);
    ensureStepCompleted(CheckoutStep.DELIVERY);
    ensureAtOrBeforeStep(CheckoutStep.PAYMENT);

    this.paymentSelection = payment;

    // Advance to next step if currently at payment step
    if (currentStep == CheckoutStep.PAYMENT) {
      this.currentStep = CheckoutStep.REVIEW;
    }

    registerEvent(PaymentSubmitted.now(this.id, payment));
  }

  /**
   * Confirms the checkout order after review.
   *
   * <p>Raises a {@link CheckoutConfirmed} domain event.
   *
   * @throws IllegalStateException if session is not confirmable or steps are incomplete
   */
  public void confirm() {
    ensureModifiable();
    ensureAllStepsCompleted();

    if (currentStep != CheckoutStep.REVIEW) {
      throw new IllegalStateException("Can only confirm from review step");
    }

    this.status = CheckoutSessionStatus.CONFIRMED;
    this.currentStep = CheckoutStep.CONFIRMATION;

    registerEvent(CheckoutConfirmed.now(this.id, this.cartId, this.customerId, this.totals.total()));
  }

  /**
   * Completes the checkout after successful payment processing.
   *
   * <p>Raises a {@link CheckoutCompleted} domain event.
   *
   * @param orderReference optional order reference from order system
   * @throws IllegalStateException if session is not in confirmed status
   */
  public void complete(@Nullable final String orderReference) {
    if (!status.canComplete()) {
      throw new IllegalStateException("Cannot complete checkout with status: " + status);
    }

    this.orderReference = orderReference;
    this.status = CheckoutSessionStatus.COMPLETED;

    registerEvent(CheckoutCompleted.now(this.id, this.totals.total(), orderReference));
  }

  /**
   * Abandons the checkout session.
   *
   * <p>Raises a {@link CheckoutAbandoned} domain event.
   *
   * @throws IllegalStateException if session is already in a terminal state
   */
  public void abandon() {
    if (status.isTerminal()) {
      throw new IllegalStateException("Cannot abandon checkout with status: " + status);
    }

    final CheckoutStep abandonedAt = this.currentStep;
    this.status = CheckoutSessionStatus.ABANDONED;

    registerEvent(CheckoutAbandoned.now(this.id, abandonedAt));
  }

  /**
   * Expires the checkout session due to inactivity.
   *
   * <p>Raises a {@link CheckoutExpired} domain event.
   *
   * @throws IllegalStateException if session is already in a terminal state
   */
  public void expire() {
    if (status.isTerminal()) {
      throw new IllegalStateException("Cannot expire checkout with status: " + status);
    }

    final CheckoutStep expiredAt = this.currentStep;
    this.status = CheckoutSessionStatus.EXPIRED;

    registerEvent(CheckoutExpired.now(this.id, expiredAt));
  }

  /**
   * Navigates back to a previous step.
   *
   * @param step the step to navigate back to
   * @throws IllegalStateException if session is not modifiable
   * @throws IllegalArgumentException if trying to go forward or to confirmation step
   */
  public void goBackTo(@NonNull final CheckoutStep step) {
    ensureModifiable();

    if (step == CheckoutStep.CONFIRMATION) {
      throw new IllegalArgumentException("Cannot navigate directly to confirmation step");
    }
    if (step.isAfter(currentStep)) {
      throw new IllegalArgumentException(
          "Cannot skip forward to step " + step + " from " + currentStep);
    }

    this.currentStep = step;
  }

  /**
   * Checks if a specific step has been completed with data.
   *
   * @param step the step to check
   * @return true if the step data has been submitted
   */
  public boolean isStepCompleted(@NonNull final CheckoutStep step) {
    return switch (step) {
      case BUYER_INFO -> buyerInfo != null;
      case DELIVERY -> deliveryAddress != null && shippingOption != null;
      case PAYMENT -> paymentSelection != null;
      case REVIEW -> status == CheckoutSessionStatus.CONFIRMED
          || status == CheckoutSessionStatus.COMPLETED;
      case CONFIRMATION -> status == CheckoutSessionStatus.COMPLETED;
    };
  }

  /**
   * Checks if the session is active and can be modified.
   *
   * @return true if status is ACTIVE
   */
  public boolean isActive() {
    return status == CheckoutSessionStatus.ACTIVE;
  }

  /**
   * Checks if the session has been completed successfully.
   *
   * @return true if status is COMPLETED
   */
  public boolean isCompleted() {
    return status == CheckoutSessionStatus.COMPLETED;
  }

  private void ensureModifiable() {
    if (!status.isModifiable()) {
      throw new IllegalStateException("Cannot modify checkout with status: " + status);
    }
  }

  private void ensureStepCompleted(final CheckoutStep step) {
    if (!isStepCompleted(step)) {
      throw new IllegalStateException("Step " + step + " must be completed first");
    }
  }

  private void ensureAtOrBeforeStep(final CheckoutStep step) {
    // Allow modifying current step or going back to modify previous steps
    // Cannot skip forward (e.g., submit payment before delivery)
    if (currentStep.isBefore(step)) {
      throw new IllegalStateException(
          "Cannot skip to step " + step + " - currently at " + currentStep);
    }
  }

  private void ensureAllStepsCompleted() {
    if (!isStepCompleted(CheckoutStep.BUYER_INFO)) {
      throw new IllegalStateException("Buyer info not submitted");
    }
    if (!isStepCompleted(CheckoutStep.DELIVERY)) {
      throw new IllegalStateException("Delivery not submitted");
    }
    if (!isStepCompleted(CheckoutStep.PAYMENT)) {
      throw new IllegalStateException("Payment not submitted");
    }
  }
}
