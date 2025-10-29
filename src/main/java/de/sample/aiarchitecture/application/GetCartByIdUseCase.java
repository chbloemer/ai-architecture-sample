package de.sample.aiarchitecture.application;

import de.sample.aiarchitecture.domain.model.cart.CartId;
import de.sample.aiarchitecture.domain.model.cart.ShoppingCart;
import de.sample.aiarchitecture.domain.model.cart.ShoppingCartRepository;
import de.sample.aiarchitecture.domain.model.shared.Money;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a shopping cart by its ID.
 *
 * <p>This is a query use case that retrieves cart details without modifying state.
 */
@Service
@Transactional(readOnly = true)
public class GetCartByIdUseCase implements UseCase<GetCartByIdInput, GetCartByIdOutput> {

  private final ShoppingCartRepository shoppingCartRepository;

  public GetCartByIdUseCase(final ShoppingCartRepository shoppingCartRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
  }

  @Override
  public @NonNull GetCartByIdOutput execute(@NonNull final GetCartByIdInput input) {
    final CartId cartId = CartId.of(input.cartId());

    final Optional<ShoppingCart> cartOpt = shoppingCartRepository.findById(cartId);

    if (cartOpt.isEmpty()) {
      return GetCartByIdOutput.notFound();
    }

    final ShoppingCart cart = cartOpt.get();

    final List<GetCartByIdOutput.CartItemSummary> items = cart.items().stream()
        .map(item -> new GetCartByIdOutput.CartItemSummary(
            item.id().value().toString(),
            item.productId().value().toString(),
            item.quantity().value(),
            item.priceAtAddition().value().amount(),
            item.priceAtAddition().value().currency().getCurrencyCode()
        ))
        .toList();

    final Money total = cart.calculateTotal();

    return GetCartByIdOutput.found(
        cart.id().value(),
        cart.customerId().value(),
        cart.status().name(),
        items,
        total.amount(),
        total.currency().getCurrencyCode()
    );
  }
}
