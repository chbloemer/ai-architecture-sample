package de.sample.aiarchitecture.cart.application.usecase.getcartbyid;

import de.sample.aiarchitecture.cart.application.port.in.GetCartByIdInputPort;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.cart.application.port.out.ShoppingCartRepository;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a shopping cart by its ID.
 *
 * <p>This is a query use case that retrieves cart details without modifying state.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetCartByIdInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetCartByIdUseCase implements GetCartByIdInputPort {

  private final ShoppingCartRepository shoppingCartRepository;

  public GetCartByIdUseCase(final ShoppingCartRepository shoppingCartRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
  }

  @Override
  public @NonNull GetCartByIdResponse execute(@NonNull final GetCartByIdQuery input) {
    final CartId cartId = CartId.of(input.cartId());

    final Optional<ShoppingCart> cartOpt = shoppingCartRepository.findById(cartId);

    if (cartOpt.isEmpty()) {
      return GetCartByIdResponse.notFound();
    }

    final ShoppingCart cart = cartOpt.get();

    final List<GetCartByIdResponse.CartItemSummary> items = cart.items().stream()
        .map(item -> new GetCartByIdResponse.CartItemSummary(
            item.id().value().toString(),
            item.productId().value().toString(),
            item.quantity().value(),
            item.priceAtAddition().value().amount(),
            item.priceAtAddition().value().currency().getCurrencyCode()
        ))
        .toList();

    final Money total = cart.calculateTotal();

    return GetCartByIdResponse.found(
        cart.id().value(),
        cart.customerId().value(),
        cart.status().name(),
        items,
        total.amount(),
        total.currency().getCurrencyCode()
    );
  }
}
