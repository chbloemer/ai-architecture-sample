package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.StateInterest;

/**
 * Interest interface for receiving state from {@link ShoppingCart} aggregate.
 *
 * <p>This interface defines the contract for consumers interested in ShoppingCart state.
 * Implementations receive state through the {@code receive*()} methods, which are called
 * when the aggregate exposes its state via {@code provideStateTo(CartStateInterest)}.
 *
 * <p><b>Usage:</b>
 * <ul>
 *   <li>Read Model builders implement this interface to receive state updates</li>
 *   <li>Projection handlers implement this to build query-optimized views</li>
 *   <li>DTOs can implement this for direct state transfer</li>
 * </ul>
 *
 * @see ShoppingCart
 * @see StateInterest
 */
public interface CartStateInterest extends StateInterest {

  /**
   * Receives the cart identifier.
   *
   * @param cartId the unique cart identifier
   */
  void receiveCartId(CartId cartId);

  /**
   * Receives the customer identifier.
   *
   * @param customerId the customer identifier
   */
  void receiveCustomerId(CustomerId customerId);

  /**
   * Receives a line item from the cart.
   *
   * <p>This method is called once for each line item in the shopping cart.
   *
   * @param lineItemId the unique identifier for this line item
   * @param productId the product identifier
   * @param name the product name
   * @param price the unit price
   * @param quantity the quantity in cart
   */
  void receiveLineItem(
      CartItemId lineItemId,
      ProductId productId,
      String name,
      Money price,
      int quantity);

  /**
   * Receives the total count of items in the cart.
   *
   * @param itemCount the number of distinct line items
   */
  void receiveItemCount(int itemCount);

  /**
   * Receives the cart subtotal.
   *
   * @param subtotal the subtotal before any discounts or taxes
   */
  void receiveSubtotal(Money subtotal);

  /**
   * Receives the cart status.
   *
   * @param status the current cart status
   */
  void receiveStatus(CartStatus status);
}
