package de.sample.aiarchitecture.portadapter.incoming.api.cart;

import de.sample.aiarchitecture.application.*;
import de.sample.aiarchitecture.domain.model.cart.CartItem;
import de.sample.aiarchitecture.domain.model.cart.ShoppingCart;
import de.sample.aiarchitecture.domain.model.shared.Money;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Converter for transforming between domain/use case models and Cart DTOs.
 *
 * <p>This converter supports both:
 * <ul>
 *   <li>Domain entities (ShoppingCart) - for legacy code
 *   <li>Use case outputs - for Clean Architecture pattern
 * </ul>
 */
@Component
public final class ShoppingCartDtoConverter {

  /**
   * Converts domain ShoppingCart entity to DTO.
   *
   * @param cart the domain cart
   * @return cart DTO
   * @deprecated Use use case output converters instead
   */
  @Deprecated
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

  /**
   * Converts CreateCartOutput to DTO.
   */
  public ShoppingCartDto toDto(final CreateCartOutput output) {
    return new ShoppingCartDto(
        output.cartId(),
        output.customerId(),
        List.of(), // New cart has no items
        output.status(),
        null,
        null,
        0
    );
  }

  /**
   * Converts GetCartByIdOutput to DTO.
   */
  public ShoppingCartDto toDto(final GetCartByIdOutput output) {
    final List<CartItemDto> items = output.items() != null
        ? output.items().stream().map(this::toItemDto).toList()
        : List.of();

    return new ShoppingCartDto(
        output.cartId(),
        output.customerId(),
        items,
        output.status(),
        output.totalAmount(),
        output.totalCurrency(),
        items.size()
    );
  }

  /**
   * Converts AddItemToCartOutput to DTO.
   */
  public ShoppingCartDto toDto(final AddItemToCartOutput output) {
    final List<CartItemDto> items = output.items().stream()
        .map(this::toItemDto)
        .toList();

    return new ShoppingCartDto(
        output.cartId(),
        output.customerId(),
        items,
        "ACTIVE", // Always active when adding items
        output.totalAmount(),
        output.totalCurrency(),
        items.size()
    );
  }

  /**
   * Converts CheckoutCartOutput to DTO.
   */
  public ShoppingCartDto toDto(final CheckoutCartOutput output) {
    final List<CartItemDto> items = output.items().stream()
        .map(this::toItemDto)
        .toList();

    return new ShoppingCartDto(
        output.cartId(),
        output.customerId(),
        items,
        "CHECKED_OUT",
        output.totalAmount(),
        output.totalCurrency(),
        items.size()
    );
  }

  private CartItemDto toItemDto(final CartItem item) {
    return new CartItemDto(
        item.id().value(),
        item.productId().value(),
        item.quantity().value(),
        item.priceAtAddition().value().amount(),
        item.priceAtAddition().value().currency().getCurrencyCode());
  }

  private CartItemDto toItemDto(final GetCartByIdOutput.CartItemSummary item) {
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        item.unitPriceAmount(),
        item.unitPriceCurrency()
    );
  }

  private CartItemDto toItemDto(final AddItemToCartOutput.CartItemSummary item) {
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        item.unitPriceAmount(),
        item.unitPriceCurrency()
    );
  }

  private CartItemDto toItemDto(final CheckoutCartOutput.CartItemSummary item) {
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        item.unitPriceAmount(),
        item.unitPriceCurrency()
    );
  }
}
