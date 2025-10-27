package de.sample.aiarchitecture.portadapter.incoming.api.cart;

import de.sample.aiarchitecture.domain.model.cart.CartItem;
import de.sample.aiarchitecture.domain.model.cart.ShoppingCart;
import de.sample.aiarchitecture.domain.model.shared.Money;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class ShoppingCartDtoConverter {

  public ShoppingCartDto toDto(final ShoppingCart cart) {
    final List<CartItemDto> itemDtos = cart.items().stream().map(this::toItemDto).toList();

    final Money total = cart.calculateTotal();

    return new ShoppingCartDto(
        cart.id().value(),
        cart.customerId().value(),
        itemDtos,
        cart.status().name(),
        total.amount(),
        total.currency().getCurrencyCode(),
        cart.itemCount());
  }

  private CartItemDto toItemDto(final CartItem item) {
    return new CartItemDto(
        item.id().value(),
        item.productId().value(),
        item.quantity().value(),
        item.priceAtAddition().value().amount(),
        item.priceAtAddition().value().currency().getCurrencyCode());
  }
}
