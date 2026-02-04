package de.sample.aiarchitecture.pricing.application.getpricesforproducts;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;
import org.jspecify.annotations.NonNull;

/**
 * Input port for retrieving prices for multiple products.
 *
 * <p>This port defines the contract for bulk price lookup in the Pricing bounded context. Primary
 * adapters (REST controllers, other bounded contexts via Open Host Service) depend on this
 * interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for read operations.
 */
public interface GetPricesForProductsInputPort
    extends UseCase<GetPricesForProductsQuery, GetPricesForProductsResult> {

  /**
   * Retrieves prices for the specified products.
   *
   * @param query the query containing product IDs
   * @return result containing prices mapped by product ID
   */
  @Override
  @NonNull
  GetPricesForProductsResult execute(@NonNull GetPricesForProductsQuery query);
}
