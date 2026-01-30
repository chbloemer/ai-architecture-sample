package de.sample.aiarchitecture.checkout.application.shared;

import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.sharedkernel.application.port.OutputPort;
import java.util.Optional;

/**
 * Output port for accessing cart data from the Cart bounded context.
 *
 * <p>This is part of the Anti-Corruption Layer (ACL) that isolates the Checkout context
 * from direct coupling to the Cart context's domain model. The adapter implementation
 * translates Cart domain objects into Checkout-specific data structures.
 *
 * <p><b>Hexagonal Architecture:</b> This is a secondary/driven port that defines what
 * the Checkout application layer needs from the Cart context.
 */
public interface CartDataPort extends OutputPort {

  /**
   * Finds cart data by cart ID.
   *
   * @param cartId the cart identifier
   * @return the cart data if found, empty otherwise
   */
  Optional<CartData> findById(CartId cartId);

  /**
   * Marks a cart as checked out after a successful checkout.
   *
   * @param cartId the cart identifier to mark as checked out
   */
  void markAsCheckedOut(CartId cartId);
}
