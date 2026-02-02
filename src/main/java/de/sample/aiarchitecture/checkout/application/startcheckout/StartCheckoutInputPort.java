package de.sample.aiarchitecture.checkout.application.startcheckout;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;
import org.jspecify.annotations.NonNull;

/**
 * Input port for starting a checkout session from a cart.
 *
 * <p>This port defines the contract for initiating the checkout process.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see StartCheckoutUseCase
 */
public interface StartCheckoutInputPort extends UseCase<StartCheckoutCommand, StartCheckoutResult> {

  /**
   * Starts a checkout session from the specified cart.
   *
   * <p>This operation:
   * <ul>
   *   <li>Loads the cart and validates it can be checked out</li>
   *   <li>Creates a new checkout session with line items from the cart</li>
   *   <li>Marks the cart as checked out</li>
   *   <li>Persists the checkout session</li>
   * </ul>
   *
   * @param command the command containing the cart ID
   * @return response containing the created checkout session details
   * @throws IllegalArgumentException if cart is not found or cannot be checked out
   */
  @Override
  @NonNull StartCheckoutResult execute(@NonNull StartCheckoutCommand command);
}
