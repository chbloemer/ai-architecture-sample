package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.StateInterest;
import org.jspecify.annotations.Nullable;

/**
 * Interest interface for receiving state from {@link CheckoutSession} aggregate.
 *
 * <p>This interface defines the contract for consumers interested in CheckoutSession state.
 * Implementations receive state through the {@code receive*()} methods, which are called
 * when the aggregate exposes its state via {@code provideStateTo(CheckoutStateInterest)}.
 *
 * <p><b>Usage:</b>
 * <ul>
 *   <li>Read Model builders implement this interface to receive state updates</li>
 *   <li>Projection handlers implement this to build query-optimized views</li>
 *   <li>DTOs can implement this for direct state transfer</li>
 * </ul>
 *
 * <p><b>Optional State:</b> Methods for optional state (buyerInfo, deliveryAddress,
 * shippingOption, paymentSelection) are only called when the data is present.
 * Implementations should handle the case where these methods are never called.
 *
 * @see CheckoutSession
 * @see StateInterest
 */
public interface CheckoutStateInterest extends StateInterest {

  /**
   * Receives the checkout session identifier.
   *
   * @param sessionId the unique session identifier
   */
  void receiveSessionId(CheckoutSessionId sessionId);

  /**
   * Receives the cart identifier this checkout originated from.
   *
   * @param cartId the origin cart identifier
   */
  void receiveCartId(CartId cartId);

  /**
   * Receives the customer identifier.
   *
   * @param customerId the customer identifier
   */
  void receiveCustomerId(CustomerId customerId);

  /**
   * Receives the current checkout step.
   *
   * @param step the current step in the checkout flow
   */
  void receiveStep(CheckoutStep step);

  /**
   * Receives a line item from the checkout.
   *
   * <p>This method is called once for each line item in the checkout session.
   *
   * @param lineItemId the unique identifier for this line item
   * @param productId the product identifier
   * @param name the product name
   * @param price the unit price
   * @param quantity the quantity ordered
   */
  void receiveLineItem(
      CheckoutLineItemId lineItemId,
      ProductId productId,
      String name,
      Money price,
      int quantity);

  /**
   * Receives the checkout subtotal.
   *
   * @param subtotal the subtotal before shipping and tax
   */
  void receiveSubtotal(Money subtotal);

  /**
   * Receives the buyer information.
   *
   * <p>This method is only called if buyer info has been submitted.
   *
   * @param buyerInfo the buyer contact information, or null if not yet submitted
   */
  void receiveBuyerInfo(@Nullable BuyerInfo buyerInfo);

  /**
   * Receives the delivery address.
   *
   * <p>This method is only called if a delivery address has been submitted.
   *
   * @param deliveryAddress the delivery address, or null if not yet submitted
   */
  void receiveDeliveryAddress(@Nullable DeliveryAddress deliveryAddress);

  /**
   * Receives the shipping option selection.
   *
   * <p>This method is only called if a shipping option has been selected.
   *
   * @param shippingOption the selected shipping option, or null if not yet selected
   */
  void receiveShippingOption(@Nullable ShippingOption shippingOption);

  /**
   * Receives the payment selection.
   *
   * <p>This method is only called if a payment method has been selected.
   *
   * @param paymentSelection the payment selection, or null if not yet selected
   */
  void receivePaymentSelection(@Nullable PaymentSelection paymentSelection);
}
