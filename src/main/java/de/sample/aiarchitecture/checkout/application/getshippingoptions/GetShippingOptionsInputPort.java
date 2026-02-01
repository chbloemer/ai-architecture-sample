package de.sample.aiarchitecture.checkout.application.getshippingoptions;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;
import org.jspecify.annotations.NonNull;

/**
 * Input port for retrieving available shipping options.
 *
 * <p>This port defines the contract for querying shipping methods.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for read operations.
 *
 * @see GetShippingOptionsUseCase
 */
public interface GetShippingOptionsInputPort
    extends UseCase<GetShippingOptionsQuery, GetShippingOptionsResponse> {

  /**
   * Retrieves all available shipping options.
   *
   * <p>Returns a list of shipping methods with their costs and estimated delivery times.
   *
   * @param query the query (marker, no parameters required)
   * @return response containing available shipping options
   */
  @Override
  @NonNull GetShippingOptionsResponse execute(@NonNull GetShippingOptionsQuery query);
}
