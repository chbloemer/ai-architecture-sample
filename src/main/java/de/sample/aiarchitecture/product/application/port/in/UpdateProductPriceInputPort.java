package de.sample.aiarchitecture.product.application.port.in;

import de.sample.aiarchitecture.product.application.usecase.updateproductprice.UpdateProductPriceCommand;
import de.sample.aiarchitecture.product.application.usecase.updateproductprice.UpdateProductPriceResponse;
import de.sample.aiarchitecture.sharedkernel.application.marker.InputPort;
import org.jspecify.annotations.NonNull;

/**
 * Input port for updating a product's price.
 *
 * <p>This port defines the contract for changing product prices in the Product bounded context.
 * Primary adapters (REST controllers, MCP tools, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see de.sample.aiarchitecture.product.application.usecase.updateproductprice.UpdateProductPriceUseCase
 */
public interface UpdateProductPriceInputPort extends InputPort<UpdateProductPriceCommand, UpdateProductPriceResponse> {

  /**
   * Updates the price of an existing product.
   *
   * @param command the command containing product ID and new price
   * @return response containing updated product details
   * @throws IllegalArgumentException if product not found or price is invalid
   */
  @Override
  @NonNull UpdateProductPriceResponse execute(@NonNull UpdateProductPriceCommand command);
}
