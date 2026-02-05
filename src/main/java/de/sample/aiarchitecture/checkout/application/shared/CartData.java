package de.sample.aiarchitecture.checkout.application.shared;

import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Price;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.List;

/**
 * Data transfer object representing cart data from the Cart bounded context.
 *
 * <p>This is part of the Anti-Corruption Layer (ACL) that translates Cart context
 * data into the Checkout context's language.
 */
public record CartData(
    CartId cartId,
    CustomerId customerId,
    List<CartItemData> items,
    boolean active) {

  /**
   * Represents a cart item in terms the Checkout context understands.
   */
  public record CartItemData(
      ProductId productId, Price priceAtAddition, int quantity) {}
}
