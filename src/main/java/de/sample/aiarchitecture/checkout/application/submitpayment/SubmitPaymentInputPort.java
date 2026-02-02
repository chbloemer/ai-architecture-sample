package de.sample.aiarchitecture.checkout.application.submitpayment;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;
import org.jspecify.annotations.NonNull;

/**
 * Input port for submitting payment information during checkout.
 *
 * <p>This port defines the contract for the payment step of checkout.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see SubmitPaymentUseCase
 */
public interface SubmitPaymentInputPort
    extends UseCase<SubmitPaymentCommand, SubmitPaymentResult> {

  /**
   * Submits payment information for the checkout session.
   *
   * <p>This operation:
   * <ul>
   *   <li>Validates the session exists and is active</li>
   *   <li>Validates the buyer info and delivery steps are completed</li>
   *   <li>Validates the session is at or before the payment step</li>
   *   <li>Validates the payment provider exists and is available</li>
   *   <li>Updates the session with payment selection</li>
   *   <li>Advances to the review step if at payment step</li>
   * </ul>
   *
   * @param command the command containing session ID and payment provider selection
   * @return response containing the updated session state
   * @throws IllegalArgumentException if session is not found or provider does not exist
   * @throws IllegalStateException if session is not modifiable or step validation fails
   */
  @Override
  @NonNull SubmitPaymentResult execute(@NonNull SubmitPaymentCommand command);
}
