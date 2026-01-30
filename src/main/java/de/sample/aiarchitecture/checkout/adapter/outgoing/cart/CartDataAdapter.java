package de.sample.aiarchitecture.checkout.adapter.outgoing.cart;

import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.checkout.application.shared.CartData;
import de.sample.aiarchitecture.checkout.application.shared.CartDataPort;
import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adapter that implements the CartDataPort by accessing the Cart bounded context.
 *
 * <p>This is the Anti-Corruption Layer (ACL) that translates between the Cart context's
 * domain model and the Checkout context's data structures. All coupling to the Cart
 * context is isolated here.
 */
@Component
public class CartDataAdapter implements CartDataPort {

  private final ShoppingCartRepository shoppingCartRepository;

  public CartDataAdapter(final ShoppingCartRepository shoppingCartRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
  }

  @Override
  public Optional<CartData> findById(final CartId cartId) {
    // Convert checkout CartId to cart CartId
    final de.sample.aiarchitecture.cart.domain.model.CartId cartContextId =
        de.sample.aiarchitecture.cart.domain.model.CartId.of(cartId.value());

    return shoppingCartRepository.findById(cartContextId).map(this::toCartData);
  }

  @Override
  public void markAsCheckedOut(final CartId cartId) {
    final de.sample.aiarchitecture.cart.domain.model.CartId cartContextId =
        de.sample.aiarchitecture.cart.domain.model.CartId.of(cartId.value());

    shoppingCartRepository
        .findById(cartContextId)
        .ifPresent(
            cart -> {
              cart.checkout();
              shoppingCartRepository.save(cart);
            });
  }

  private CartData toCartData(final ShoppingCart cart) {
    final java.util.List<CartData.CartItemData> items =
        cart.items().stream()
            .map(
                item ->
                    new CartData.CartItemData(
                        item.productId(), item.priceAtAddition(), item.quantity().value()))
            .toList();

    return new CartData(
        CartId.of(cart.id().value()),
        CustomerId.of(cart.customerId().value()),
        items,
        cart.isActive());
  }
}
