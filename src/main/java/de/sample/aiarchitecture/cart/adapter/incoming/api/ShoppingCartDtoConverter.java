package de.sample.aiarchitecture.cart.adapter.incoming.api;

import de.sample.aiarchitecture.cart.application.additemtocart.AddItemToCartResponse;
import de.sample.aiarchitecture.cart.application.checkoutcart.CheckoutCartResponse;
import de.sample.aiarchitecture.cart.application.createcart.CreateCartResponse;
import de.sample.aiarchitecture.cart.application.getallcarts.GetAllCartsResponse;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdResponse;
import de.sample.aiarchitecture.cart.application.removeitemfromcart.RemoveItemFromCartResponse;
import de.sample.aiarchitecture.cart.domain.model.CartItem;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
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
   * Converts CreateCartResponse to DTO.
   */
  public ShoppingCartDto toDto(final CreateCartResponse output) {
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
   * Converts GetCartByIdResponse to DTO.
   */
  public ShoppingCartDto toDto(final GetCartByIdResponse output) {
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
   * Converts AddItemToCartResponse to DTO.
   */
  public ShoppingCartDto toDto(final AddItemToCartResponse output) {
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
   * Converts RemoveItemFromCartResponse to DTO.
   */
  public ShoppingCartDto toDto(final RemoveItemFromCartResponse output) {
    final List<CartItemDto> items = output.items().stream()
        .map(this::toItemDto)
        .toList();

    return new ShoppingCartDto(
        output.cartId(),
        output.customerId(),
        items,
        "ACTIVE", // Always active when removing items
        output.totalAmount(),
        output.totalCurrency(),
        items.size()
    );
  }

  /**
   * Converts CheckoutCartResponse to DTO.
   */
  public ShoppingCartDto toDto(final CheckoutCartResponse output) {
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

  /**
   * Converts GetAllCartsResponse to list DTO.
   */
  public ShoppingCartListDto toListDto(final GetAllCartsResponse output) {
    final List<ShoppingCartListDto.CartSummaryDto> summaries = output.carts().stream()
        .map(cart -> new ShoppingCartListDto.CartSummaryDto(
            cart.cartId(),
            cart.customerId(),
            cart.status(),
            cart.itemCount(),
            cart.totalAmount(),
            cart.totalCurrency()
        ))
        .toList();

    return new ShoppingCartListDto(summaries);
  }

  private CartItemDto toItemDto(final CartItem item) {
    return new CartItemDto(
        item.id().value(),
        item.productId().value(),
        item.quantity().value(),
        item.priceAtAddition().value().amount(),
        item.priceAtAddition().value().currency().getCurrencyCode());
  }

  private CartItemDto toItemDto(final GetCartByIdResponse.CartItemSummary item) {
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        item.unitPriceAmount(),
        item.unitPriceCurrency()
    );
  }

  private CartItemDto toItemDto(final AddItemToCartResponse.CartItemSummary item) {
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        item.unitPriceAmount(),
        item.unitPriceCurrency()
    );
  }

  private CartItemDto toItemDto(final RemoveItemFromCartResponse.CartItemSummary item) {
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        item.unitPriceAmount(),
        item.unitPriceCurrency()
    );
  }

  private CartItemDto toItemDto(final CheckoutCartResponse.CartItemSummary item) {
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        item.unitPriceAmount(),
        item.unitPriceCurrency()
    );
  }
}
