package de.sample.aiarchitecture.cart.application.createcart;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;
import org.jspecify.annotations.NonNull;

/**
 * Input port for creating a new shopping cart.
 *
 * <p>This port defines the contract for creating shopping carts in the Cart bounded context.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see de.sample.aiarchitecture.cart.application.usecase.createcart.CreateCartUseCase
 */
public interface CreateCartInputPort extends UseCase<CreateCartCommand, CreateCartResponse> {

  /**
   * Creates a new shopping cart for a customer.
   *
   * @param command the command containing customer ID
   * @return response containing the created cart details
   */
  @Override
  @NonNull CreateCartResponse execute(@NonNull CreateCartCommand command);
}
