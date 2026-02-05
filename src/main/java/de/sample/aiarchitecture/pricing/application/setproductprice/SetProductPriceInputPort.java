package de.sample.aiarchitecture.pricing.application.setproductprice;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;

/**
 * Input port for setting or updating a product's price.
 *
 * <p>This port defines the contract for managing product prices in the Pricing bounded context.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see SetProductPriceUseCase
 */
public interface SetProductPriceInputPort
    extends UseCase<SetProductPriceCommand, SetProductPriceResult> {

  /**
   * Sets or updates a product's price.
   *
   * <p>If no price exists for the product, a new price record is created. If a price already
   * exists, it is updated to the new value.
   *
   * @param command the command containing product ID and price details
   * @return result containing the price record details and whether it was created or updated
   * @throws IllegalArgumentException if price amount is not greater than zero
   */
  @Override
  
  SetProductPriceResult execute(SetProductPriceCommand command);
}
