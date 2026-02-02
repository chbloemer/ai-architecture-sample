package de.sample.aiarchitecture.checkout.application.confirmcheckout;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;
import org.jspecify.annotations.NonNull;

/**
 * Input port for confirming a checkout session.
 *
 * <p>This port defines the contract for completing the checkout review step
 * and confirming the order. Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see ConfirmCheckoutUseCase
 */
public interface ConfirmCheckoutInputPort
    extends UseCase<ConfirmCheckoutCommand, ConfirmCheckoutResult> {

  /**
   * Confirms the checkout session after customer review.
   *
   * <p>This operation:
   * <ul>
   *   <li>Validates the session exists and is active</li>
   *   <li>Validates all required steps are completed (buyer info, delivery, payment)</li>
   *   <li>Validates the session is at the review step</li>
   *   <li>Transitions the session to CONFIRMED status</li>
   *   <li>Publishes a CheckoutConfirmed integration event</li>
   * </ul>
   *
   * @param command the command containing the session ID
   * @return response containing the confirmed session state
   * @throws IllegalArgumentException if session is not found
   * @throws IllegalStateException if session is not modifiable, steps are incomplete,
   *         or not at review step
   */
  @Override
  @NonNull ConfirmCheckoutResult execute(@NonNull ConfirmCheckoutCommand command);
}
