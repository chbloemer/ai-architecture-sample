package de.sample.aiarchitecture.cart.adapter.incoming.api;

import de.sample.aiarchitecture.cart.application.usecase.additemtocart.AddItemToCartCommand;
import de.sample.aiarchitecture.cart.application.usecase.additemtocart.AddItemToCartResponse;
import de.sample.aiarchitecture.cart.application.usecase.additemtocart.AddItemToCartUseCase;
import de.sample.aiarchitecture.cart.application.usecase.checkoutcart.CheckoutCartCommand;
import de.sample.aiarchitecture.cart.application.usecase.checkoutcart.CheckoutCartResponse;
import de.sample.aiarchitecture.cart.application.usecase.checkoutcart.CheckoutCartUseCase;
import de.sample.aiarchitecture.cart.application.usecase.createcart.CreateCartCommand;
import de.sample.aiarchitecture.cart.application.usecase.createcart.CreateCartResponse;
import de.sample.aiarchitecture.cart.application.usecase.createcart.CreateCartUseCase;
import de.sample.aiarchitecture.cart.application.usecase.getallcarts.GetAllCartsQuery;
import de.sample.aiarchitecture.cart.application.usecase.getallcarts.GetAllCartsResponse;
import de.sample.aiarchitecture.cart.application.usecase.getallcarts.GetAllCartsUseCase;
import de.sample.aiarchitecture.cart.application.usecase.getcartbyid.GetCartByIdQuery;
import de.sample.aiarchitecture.cart.application.usecase.getcartbyid.GetCartByIdResponse;
import de.sample.aiarchitecture.cart.application.usecase.getcartbyid.GetCartByIdUseCase;
import de.sample.aiarchitecture.cart.application.usecase.removeitemfromcart.RemoveItemFromCartCommand;
import de.sample.aiarchitecture.cart.application.usecase.removeitemfromcart.RemoveItemFromCartResponse;
import de.sample.aiarchitecture.cart.application.usecase.removeitemfromcart.RemoveItemFromCartUseCase;
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
  private final GetAllCartsUseCase getAllCartsUseCase;
  private final GetCartByIdUseCase getCartByIdUseCase;
  private final AddItemToCartUseCase addItemToCartUseCase;
  private final RemoveItemFromCartUseCase removeItemFromCartUseCase;
  private final CheckoutCartUseCase checkoutCartUseCase;
  private final ShoppingCartDtoConverter converter;

  public ShoppingCartResource(
      final CreateCartUseCase createCartUseCase,
      final GetAllCartsUseCase getAllCartsUseCase,
      final GetCartByIdUseCase getCartByIdUseCase,
      final AddItemToCartUseCase addItemToCartUseCase,
      final RemoveItemFromCartUseCase removeItemFromCartUseCase,
      final CheckoutCartUseCase checkoutCartUseCase,
      final ShoppingCartDtoConverter converter) {
    this.createCartUseCase = createCartUseCase;
    this.getAllCartsUseCase = getAllCartsUseCase;
    this.getCartByIdUseCase = getCartByIdUseCase;
    this.addItemToCartUseCase = addItemToCartUseCase;
    this.removeItemFromCartUseCase = removeItemFromCartUseCase;
    this.checkoutCartUseCase = checkoutCartUseCase;
    this.converter = converter;
  }

  @PostMapping
  public ResponseEntity<ShoppingCartDto> createCart(@RequestParam final String customerId) {
    final CreateCartResponse output = createCartUseCase.execute(new CreateCartCommand(customerId));

    return ResponseEntity.status(HttpStatus.CREATED).body(converter.toDto(output));
  }

  @GetMapping
  public ResponseEntity<ShoppingCartListDto> getAllCarts() {
    final GetAllCartsResponse output = getAllCartsUseCase.execute(new GetAllCartsQuery());

    return ResponseEntity.ok(converter.toListDto(output));
  }

  @GetMapping("/{cartId}")
  public ResponseEntity<ShoppingCartDto> getCart(@PathVariable final String cartId) {
    final GetCartByIdResponse output = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));

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

    final AddItemToCartCommand input = new AddItemToCartCommand(
        cartId,
        request.productId(),
        request.quantity()
    );

    final AddItemToCartResponse output = addItemToCartUseCase.execute(input);

    return ResponseEntity.ok(converter.toDto(output));
  }

  @DeleteMapping("/{cartId}/items/{productId}")
  public ResponseEntity<ShoppingCartDto> removeItemFromCart(
      @PathVariable final String cartId, @PathVariable final String productId) {

    final RemoveItemFromCartCommand input = new RemoveItemFromCartCommand(cartId, productId);
    final RemoveItemFromCartResponse output = removeItemFromCartUseCase.execute(input);

    return ResponseEntity.ok(converter.toDto(output));
  }

  @PostMapping("/{cartId}/checkout")
  public ResponseEntity<ShoppingCartDto> checkout(@PathVariable final String cartId) {
    final CheckoutCartResponse output = checkoutCartUseCase.execute(new CheckoutCartCommand(cartId));

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
