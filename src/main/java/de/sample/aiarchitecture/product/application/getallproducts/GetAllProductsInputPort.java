package de.sample.aiarchitecture.product.application.getallproducts;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;

/**
 * Input port for retrieving all products.
 *
 * <p>This port defines the contract for querying all products in the Product bounded context.
 * Primary adapters (REST controllers, MCP tools, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for read operations.
 *
 * @see de.sample.aiarchitecture.product.application.usecase.getallproducts.GetAllProductsUseCase
 */
public interface GetAllProductsInputPort extends UseCase<GetAllProductsQuery, GetAllProductsResult> {

  /**
   * Retrieves all products from the catalog.
   *
   * @param query the query (currently empty, for future filtering)
   * @return response containing all products
   */
  @Override
  GetAllProductsResult execute(GetAllProductsQuery query);
}
