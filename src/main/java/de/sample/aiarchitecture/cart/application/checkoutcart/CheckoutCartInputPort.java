package de.sample.aiarchitecture.cart.application.checkoutcart;

import de.sample.aiarchitecture.sharedkernel.application.port.UseCase;
import org.jspecify.annotations.NonNull;

/**
 * Input port for checking out a shopping cart.
 *
 * <p>This port defines the contract for cart checkout operations in the Cart bounded context.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see de.sample.aiarchitecture.cart.application.usecase.checkoutcart.CheckoutCartUseCase
 */
public interface CheckoutCartInputPort extends UseCase<CheckoutCartCommand, CheckoutCartResponse> {

  /**
   * Checks out a shopping cart, finalizing the purchase.
   *
   * @param command the command containing cart ID
   * @return response containing checkout confirmation details
   * @throws IllegalArgumentException if cart not found, cart is empty, or cart already checked out
   */
  @Override
  @NonNull CheckoutCartResponse execute(@NonNull CheckoutCartCommand command);
}
