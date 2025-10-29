package de.sample.aiarchitecture.portadapter.incoming.api.cart;

import de.sample.aiarchitecture.application.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Resource for Shopping Cart operations.
 *
 * <p>This is a primary adapter (incoming) in Hexagonal Architecture that exposes
 * shopping cart functionality via REST API using Clean Architecture use cases.
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * for all cart operations, following the Dependency Inversion Principle.
 */
@RestController
@RequestMapping("/api/carts")
public class ShoppingCartResource {

  private final CreateCartUseCase createCartUseCase;
  private final GetCartByIdUseCase getCartByIdUseCase;
  private final AddItemToCartUseCase addItemToCartUseCase;
  private final CheckoutCartUseCase checkoutCartUseCase;
  private final ShoppingCartDtoConverter converter;

  public ShoppingCartResource(
      final CreateCartUseCase createCartUseCase,
      final GetCartByIdUseCase getCartByIdUseCase,
      final AddItemToCartUseCase addItemToCartUseCase,
      final CheckoutCartUseCase checkoutCartUseCase,
      final ShoppingCartDtoConverter converter) {
    this.createCartUseCase = createCartUseCase;
    this.getCartByIdUseCase = getCartByIdUseCase;
    this.addItemToCartUseCase = addItemToCartUseCase;
    this.checkoutCartUseCase = checkoutCartUseCase;
    this.converter = converter;
  }

  @PostMapping
  public ResponseEntity<ShoppingCartDto> createCart(@RequestParam final String customerId) {
    final CreateCartOutput output = createCartUseCase.execute(new CreateCartInput(customerId));

    return ResponseEntity.status(HttpStatus.CREATED).body(converter.toDto(output));
  }

  @GetMapping("/{cartId}")
  public ResponseEntity<ShoppingCartDto> getCart(@PathVariable final String cartId) {
    final GetCartByIdOutput output = getCartByIdUseCase.execute(new GetCartByIdInput(cartId));

    if (!output.found()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(converter.toDto(output));
  }

  // Note: The following endpoint doesn't have a corresponding use case yet:
  // - GetOrCreateActiveCartUseCase
  // Temporarily commented out until use case is created:
  /*
  @GetMapping("/customer/{customerId}/active")
  public ResponseEntity<ShoppingCartDto> getActiveCart(@PathVariable final String customerId) {
    // TODO: Implement GetOrCreateActiveCartUseCase
    return ResponseEntity.notFound().build();
  }
  */

  @PostMapping("/{cartId}/items")
  public ResponseEntity<ShoppingCartDto> addItemToCart(
      @PathVariable final String cartId, @Valid @RequestBody final AddToCartRequest request) {

    final AddItemToCartInput input = new AddItemToCartInput(
        cartId,
        request.productId(),
        request.quantity()
    );

    final AddItemToCartOutput output = addItemToCartUseCase.execute(input);

    return ResponseEntity.ok(converter.toDto(output));
  }

  // Note: The following endpoint doesn't have a corresponding use case yet:
  // - RemoveItemFromCartUseCase
  // Temporarily commented out until use case is created:
  /*
  @DeleteMapping("/{cartId}/items/{itemId}")
  public ResponseEntity<ShoppingCartDto> removeItemFromCart(
      @PathVariable final String cartId, @PathVariable final String itemId) {
    // TODO: Implement RemoveItemFromCartUseCase
    return ResponseEntity.notFound().build();
  }
  */

  @PostMapping("/{cartId}/checkout")
  public ResponseEntity<ShoppingCartDto> checkout(@PathVariable final String cartId) {
    final CheckoutCartOutput output = checkoutCartUseCase.execute(new CheckoutCartInput(cartId));

    return ResponseEntity.ok(converter.toDto(output));
  }

  // Note: The following endpoint doesn't have a corresponding use case yet:
  // - DeleteCartUseCase
  // Temporarily commented out until use case is created:
  /*
  @DeleteMapping("/{cartId}")
  public ResponseEntity<Void> deleteCart(@PathVariable final String cartId) {
    // TODO: Implement DeleteCartUseCase
    return ResponseEntity.noContent().build();
  }
  */
}
