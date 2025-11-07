package de.sample.aiarchitecture.cart.application.usecase.createcart;

import de.sample.aiarchitecture.sharedkernel.application.marker.InputPort;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.cart.application.port.out.ShoppingCartRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating a new shopping cart.
 *
 * <p>This use case creates a new empty shopping cart for a customer.
 */
@Service
@Transactional
public class CreateCartUseCase implements InputPort<CreateCartCommand, CreateCartResponse> {

  private final ShoppingCartRepository shoppingCartRepository;

  public CreateCartUseCase(final ShoppingCartRepository shoppingCartRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
  }

  @Override
  public @NonNull CreateCartResponse execute(@NonNull final CreateCartCommand input) {
    final CustomerId customerId = new CustomerId(input.customerId());
    final CartId cartId = CartId.generate();

    // Create cart aggregate
    final ShoppingCart cart = new ShoppingCart(cartId, customerId);

    // Persist
    shoppingCartRepository.save(cart);

    // Map to output
    return new CreateCartResponse(
        cart.id().value().toString(),
        cart.customerId().value(),
        cart.status().name()
    );
  }
}
