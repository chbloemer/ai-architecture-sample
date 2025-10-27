package de.sample.aiarchitecture.portadapter.incoming.api.cart;

import de.sample.aiarchitecture.application.ShoppingCartApplicationService;
import de.sample.aiarchitecture.domain.model.cart.*;
import de.sample.aiarchitecture.domain.model.shared.ProductId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class ShoppingCartResource {

  private final ShoppingCartApplicationService cartApplicationService;
  private final ShoppingCartDtoConverter converter;

  public ShoppingCartResource(
      final ShoppingCartApplicationService cartApplicationService,
      final ShoppingCartDtoConverter converter) {
    this.cartApplicationService = cartApplicationService;
    this.converter = converter;
  }

  @PostMapping
  public ResponseEntity<ShoppingCartDto> createCart(@RequestParam final String customerId) {
    final ShoppingCart cart = cartApplicationService.createCart(CustomerId.of(customerId));

    return ResponseEntity.status(HttpStatus.CREATED).body(converter.toDto(cart));
  }

  @GetMapping("/{cartId}")
  public ResponseEntity<ShoppingCartDto> getCart(@PathVariable final String cartId) {
    return cartApplicationService
        .findCartById(CartId.of(cartId))
        .map(converter::toDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/customer/{customerId}/active")
  public ResponseEntity<ShoppingCartDto> getActiveCart(@PathVariable final String customerId) {
    final ShoppingCart cart =
        cartApplicationService.getOrCreateActiveCart(CustomerId.of(customerId));

    return ResponseEntity.ok(converter.toDto(cart));
  }

  @PostMapping("/{cartId}/items")
  public ResponseEntity<ShoppingCartDto> addItemToCart(
      @PathVariable final String cartId, @Valid @RequestBody final AddToCartRequest request) {

    cartApplicationService.addItemToCart(
        CartId.of(cartId),
        ProductId.of(request.productId()),
        Quantity.of(request.quantity()));

    return cartApplicationService
        .findCartById(CartId.of(cartId))
        .map(converter::toDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{cartId}/items/{itemId}")
  public ResponseEntity<ShoppingCartDto> removeItemFromCart(
      @PathVariable final String cartId, @PathVariable final String itemId) {

    cartApplicationService.removeItemFromCart(CartId.of(cartId), CartItemId.of(itemId));

    return cartApplicationService
        .findCartById(CartId.of(cartId))
        .map(converter::toDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{cartId}/checkout")
  public ResponseEntity<ShoppingCartDto> checkout(@PathVariable final String cartId) {
    final ShoppingCart cart = cartApplicationService.checkout(CartId.of(cartId));

    return ResponseEntity.ok(converter.toDto(cart));
  }

  @DeleteMapping("/{cartId}")
  public ResponseEntity<Void> deleteCart(@PathVariable final String cartId) {
    cartApplicationService.deleteCart(CartId.of(cartId));
    return ResponseEntity.noContent().build();
  }
}
