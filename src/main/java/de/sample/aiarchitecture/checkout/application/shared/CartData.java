package de.sample.aiarchitecture.checkout.application.shared;

import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.common.Price;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Data transfer object representing cart data from the Cart bounded context.
 *
 * <p>This is part of the Anti-Corruption Layer (ACL) that translates Cart context
 * data into the Checkout context's language.
 */
public record CartData(
    @NonNull CartId cartId,
    @NonNull CustomerId customerId,
    @NonNull List<CartItemData> items,
    boolean active) {

  /**
   * Represents a cart item in terms the Checkout context understands.
   */
  public record CartItemData(
      @NonNull ProductId productId, @NonNull Price priceAtAddition, int quantity) {}
}
