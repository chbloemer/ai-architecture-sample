package de.sample.aiarchitecture.checkout.application.submitdelivery;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;
import org.jspecify.annotations.NonNull;

/**
 * Input port for submitting delivery information during checkout.
 *
 * <p>This port defines the contract for the delivery step of checkout.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see SubmitDeliveryUseCase
 */
public interface SubmitDeliveryInputPort
    extends UseCase<SubmitDeliveryCommand, SubmitDeliveryResult> {

  /**
   * Submits delivery information for the checkout session.
   *
   * <p>This operation:
   * <ul>
   *   <li>Validates the session exists and is active</li>
   *   <li>Validates the buyer info step is completed</li>
   *   <li>Validates the session is at or before the delivery step</li>
   *   <li>Updates the session with delivery address and shipping option</li>
   *   <li>Updates totals with shipping cost</li>
   *   <li>Advances to the payment step if at delivery step</li>
   * </ul>
   *
   * @param command the command containing session ID, delivery address, and shipping option
   * @return response containing the updated session state
   * @throws IllegalArgumentException if session is not found
   * @throws IllegalStateException if session is not modifiable, step validation fails,
   *         or buyer info step is not completed
   */
  @Override
  @NonNull SubmitDeliveryResult execute(@NonNull SubmitDeliveryCommand command);
}
