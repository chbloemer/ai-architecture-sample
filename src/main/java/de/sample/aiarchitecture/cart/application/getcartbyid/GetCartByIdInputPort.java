package de.sample.aiarchitecture.cart.application.getcartbyid;

import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdQuery;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdResponse;
import de.sample.aiarchitecture.sharedkernel.application.marker.InputPort;
import org.jspecify.annotations.NonNull;

/**
 * Input port for retrieving a cart by its ID.
 *
 * <p>This port defines the contract for querying a specific cart in the Cart bounded context.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for read operations.
 *
 * @see de.sample.aiarchitecture.cart.application.usecase.getcartbyid.GetCartByIdUseCase
 */
public interface GetCartByIdInputPort extends InputPort<GetCartByIdQuery, GetCartByIdResponse> {

  /**
   * Retrieves a shopping cart by its unique identifier.
   *
   * @param query the query containing the cart ID
   * @return response containing cart details or indication that cart was not found
   */
  @Override
  @NonNull GetCartByIdResponse execute(@NonNull GetCartByIdQuery query);
}
