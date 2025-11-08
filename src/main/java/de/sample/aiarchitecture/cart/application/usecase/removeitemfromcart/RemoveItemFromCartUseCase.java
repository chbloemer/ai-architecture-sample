package de.sample.aiarchitecture.cart.application.usecase.removeitemfromcart;

import de.sample.aiarchitecture.cart.application.port.in.RemoveItemFromCartInputPort;
import de.sample.aiarchitecture.cart.application.port.out.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.infrastructure.api.DomainEventPublisher;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for removing an item from a shopping cart.
 *
 * <p>This use case orchestrates removing an item from the cart by:
 * <ol>
 *   <li>Retrieving the cart</li>
 *   <li>Removing the item (business logic in aggregate)</li>
 *   <li>Persisting the updated cart</li>
 *   <li>Publishing domain events</li>
 * </ol>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link RemoveItemFromCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class RemoveItemFromCartUseCase implements RemoveItemFromCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;

  public RemoveItemFromCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public @NonNull RemoveItemFromCartResponse execute(@NonNull final RemoveItemFromCartCommand input) {
    final CartId cartId = CartId.of(input.cartId());
    final ProductId productId = ProductId.of(input.productId());

    // Retrieve cart
    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + input.cartId()));

    // Remove item from cart (business logic in aggregate)
    cart.removeItemByProductId(productId);

    // Persist
    shoppingCartRepository.save(cart);

    // Publish domain events
    eventPublisher.publishAndClearEvents(cart);

    // Map to output
    final List<RemoveItemFromCartResponse.CartItemSummary> items = cart.items().stream()
        .map(item -> new RemoveItemFromCartResponse.CartItemSummary(
            item.id().value().toString(),
            item.productId().value().toString(),
            item.quantity().value(),
            item.priceAtAddition().value().amount(),
            item.priceAtAddition().value().currency().getCurrencyCode()
        ))
        .toList();

    final Money total = cart.calculateTotal();

    return new RemoveItemFromCartResponse(
        cart.id().value(),
        cart.customerId().value(),
        items,
        total.amount(),
        total.currency().getCurrencyCode()
    );
  }
}
