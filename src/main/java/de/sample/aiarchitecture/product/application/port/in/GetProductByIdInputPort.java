package de.sample.aiarchitecture.product.application.port.in;

import de.sample.aiarchitecture.product.application.usecase.getproductbyid.GetProductByIdQuery;
import de.sample.aiarchitecture.product.application.usecase.getproductbyid.GetProductByIdResponse;
import de.sample.aiarchitecture.sharedkernel.application.marker.InputPort;
import org.jspecify.annotations.NonNull;

/**
 * Input port for retrieving a product by its ID.
 *
 * <p>This port defines the contract for querying a specific product in the Product bounded context.
 * Primary adapters (REST controllers, MCP tools, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for read operations.
 *
 * @see de.sample.aiarchitecture.product.application.usecase.getproductbyid.GetProductByIdUseCase
 */
public interface GetProductByIdInputPort extends InputPort<GetProductByIdQuery, GetProductByIdResponse> {

  /**
   * Retrieves a product by its unique identifier.
   *
   * @param query the query containing the product ID
   * @return response containing product details or indication that product was not found
   */
  @Override
  @NonNull GetProductByIdResponse execute(@NonNull GetProductByIdQuery query);
}
