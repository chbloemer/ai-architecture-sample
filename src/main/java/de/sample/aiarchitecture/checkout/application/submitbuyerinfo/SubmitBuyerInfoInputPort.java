package de.sample.aiarchitecture.checkout.application.submitbuyerinfo;

import de.sample.aiarchitecture.sharedkernel.application.port.UseCase;
import org.jspecify.annotations.NonNull;

/**
 * Input port for submitting buyer contact information during checkout.
 *
 * <p>This port defines the contract for the buyer info step of checkout.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see SubmitBuyerInfoUseCase
 */
public interface SubmitBuyerInfoInputPort
    extends UseCase<SubmitBuyerInfoCommand, SubmitBuyerInfoResponse> {

  /**
   * Submits buyer contact information for the checkout session.
   *
   * <p>This operation:
   * <ul>
   *   <li>Validates the session exists and is active</li>
   *   <li>Validates the session is at or before the buyer info step</li>
   *   <li>Updates the session with buyer contact information</li>
   *   <li>Advances to the delivery step if at buyer info step</li>
   * </ul>
   *
   * @param command the command containing session ID and buyer info
   * @return response containing the updated session state
   * @throws IllegalArgumentException if session is not found
   * @throws IllegalStateException if session is not modifiable or step validation fails
   */
  @Override
  @NonNull SubmitBuyerInfoResponse execute(@NonNull SubmitBuyerInfoCommand command);
}
