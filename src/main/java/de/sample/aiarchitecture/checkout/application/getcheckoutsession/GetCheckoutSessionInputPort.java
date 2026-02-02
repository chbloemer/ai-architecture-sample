package de.sample.aiarchitecture.checkout.application.getcheckoutsession;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;
import org.jspecify.annotations.NonNull;

/**
 * Input port for retrieving a checkout session.
 *
 * <p>This port defines the contract for querying a checkout session by ID.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for read operations.
 *
 * @see GetCheckoutSessionUseCase
 */
public interface GetCheckoutSessionInputPort
    extends UseCase<GetCheckoutSessionQuery, GetCheckoutSessionResult> {

  /**
   * Retrieves a checkout session by ID.
   *
   * <p>Returns the session data if found, or a response with found=false otherwise.
   *
   * @param query the query containing the session ID
   * @return response containing the session data or found=false
   */
  @Override
  @NonNull GetCheckoutSessionResult execute(@NonNull GetCheckoutSessionQuery query);
}
