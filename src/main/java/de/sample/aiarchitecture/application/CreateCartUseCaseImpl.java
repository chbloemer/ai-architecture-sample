package de.sample.aiarchitecture.application;

import de.sample.aiarchitecture.domain.model.cart.CartId;
import de.sample.aiarchitecture.domain.model.cart.CustomerId;
import de.sample.aiarchitecture.domain.model.cart.ShoppingCart;
import de.sample.aiarchitecture.domain.model.cart.ShoppingCartRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link CreateCartUseCase}.
 *
 * <p>This use case creates a new empty shopping cart for a customer.
 */
@Service
@Transactional
class CreateCartUseCaseImpl implements CreateCartUseCase {

  private final ShoppingCartRepository shoppingCartRepository;

  CreateCartUseCaseImpl(final ShoppingCartRepository shoppingCartRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
  }

  @Override
  public @NonNull CreateCartOutput execute(@NonNull final CreateCartInput input) {
    final CustomerId customerId = new CustomerId(input.customerId());
    final CartId cartId = CartId.generate();

    // Create cart aggregate
    final ShoppingCart cart = new ShoppingCart(cartId, customerId);

    // Persist
    shoppingCartRepository.save(cart);

    // Map to output
    return new CreateCartOutput(
        cart.id().value().toString(),
        cart.customerId().value(),
        cart.status().name()
    );
  }
}
