package de.sample.aiarchitecture.cart.adapter.incoming.api;

import de.sample.aiarchitecture.cart.application.additemtocart.AddItemToCartCommand;
import de.sample.aiarchitecture.cart.application.additemtocart.AddItemToCartResult;
import de.sample.aiarchitecture.cart.application.additemtocart.AddItemToCartUseCase;
import de.sample.aiarchitecture.cart.application.checkoutcart.CheckoutCartCommand;
import de.sample.aiarchitecture.cart.application.checkoutcart.CheckoutCartResult;
import de.sample.aiarchitecture.cart.application.checkoutcart.CheckoutCartUseCase;
import de.sample.aiarchitecture.cart.application.createcart.CreateCartCommand;
import de.sample.aiarchitecture.cart.application.createcart.CreateCartResult;
import de.sample.aiarchitecture.cart.application.createcart.CreateCartUseCase;
import de.sample.aiarchitecture.cart.application.getallcarts.GetAllCartsQuery;
import de.sample.aiarchitecture.cart.application.getallcarts.GetAllCartsResult;
import de.sample.aiarchitecture.cart.application.getallcarts.GetAllCartsUseCase;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdQuery;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdResult;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdUseCase;
import de.sample.aiarchitecture.cart.application.removeitemfromcart.RemoveItemFromCartCommand;
import de.sample.aiarchitecture.cart.application.removeitemfromcart.RemoveItemFromCartResult;
import de.sample.aiarchitecture.cart.application.removeitemfromcart.RemoveItemFromCartUseCase;
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
  private final de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartUseCase getOrCreateActiveCartUseCase;
  private final AddItemToCartUseCase addItemToCartUseCase;
  private final RemoveItemFromCartUseCase removeItemFromCartUseCase;
  private final CheckoutCartUseCase checkoutCartUseCase;
  private final ShoppingCartDtoConverter converter;

  public ShoppingCartResource(
      final CreateCartUseCase createCartUseCase,
      final GetAllCartsUseCase getAllCartsUseCase,
      final GetCartByIdUseCase getCartByIdUseCase,
      final de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartUseCase getOrCreateActiveCartUseCase,
      final AddItemToCartUseCase addItemToCartUseCase,
      final RemoveItemFromCartUseCase removeItemFromCartUseCase,
      final CheckoutCartUseCase checkoutCartUseCase,
      final ShoppingCartDtoConverter converter) {
    this.createCartUseCase = createCartUseCase;
    this.getAllCartsUseCase = getAllCartsUseCase;
    this.getCartByIdUseCase = getCartByIdUseCase;
    this.getOrCreateActiveCartUseCase = getOrCreateActiveCartUseCase;
    this.addItemToCartUseCase = addItemToCartUseCase;
    this.removeItemFromCartUseCase = removeItemFromCartUseCase;
    this.checkoutCartUseCase = checkoutCartUseCase;
    this.converter = converter;
  }

  @PostMapping
  public ResponseEntity<ShoppingCartDto> createCart(@RequestParam final String customerId) {
    final CreateCartResult output = createCartUseCase.execute(new CreateCartCommand(customerId));

    return ResponseEntity.status(HttpStatus.CREATED).body(converter.toDto(output));
  }

  @GetMapping
  public ResponseEntity<ShoppingCartListDto> getAllCarts() {
    final GetAllCartsResult output = getAllCartsUseCase.execute(new GetAllCartsQuery());

    return ResponseEntity.ok(converter.toListDto(output));
  }

  @GetMapping("/{cartId}")
  public ResponseEntity<ShoppingCartDto> getCart(@PathVariable final String cartId) {
    final GetCartByIdResult result = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));

    if (!result.found()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(converter.toDto(result.cart()));
  }

  @GetMapping("/customer/{customerId}/active")
  public ResponseEntity<ShoppingCartDto> getOrCreateActiveCart(@PathVariable final String customerId) {
    if (customerId == null || customerId.isBlank()) {
      return ResponseEntity.badRequest().build();
    }

    final var response = getOrCreateActiveCartUseCase
        .execute(new de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartCommand(customerId));

    // Fetch full cart with enriched data
    final GetCartByIdResult result = getCartByIdUseCase.execute(new GetCartByIdQuery(response.cartId()));
    return ResponseEntity.ok(converter.toDto(result.cart()));
  }

  @PostMapping("/{cartId}/items")
  public ResponseEntity<ShoppingCartDto> addItemToCart(
      @PathVariable final String cartId, @Valid @RequestBody final AddToCartRequest request) {

    final AddItemToCartCommand input = new AddItemToCartCommand(
        cartId,
        request.productId(),
        request.quantity()
    );

    try {
      final AddItemToCartResult output = addItemToCartUseCase.execute(input);
      return ResponseEntity.ok(converter.toDto(output));
    } catch (IllegalArgumentException ex) {
      final String msg = ex.getMessage() != null ? ex.getMessage() : "Invalid request";
      if (msg.startsWith("Cart not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
      if (msg.startsWith("Product not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
      if (msg.startsWith("Insufficient stock")) {
        return ResponseEntity.badRequest().build();
      }
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{cartId}/items/{productId}")
  public ResponseEntity<ShoppingCartDto> removeItemFromCart(
      @PathVariable final String cartId, @PathVariable final String productId) {

    final RemoveItemFromCartCommand input = new RemoveItemFromCartCommand(cartId, productId);
    final RemoveItemFromCartResult output = removeItemFromCartUseCase.execute(input);

    return ResponseEntity.ok(converter.toDto(output));
  }

  @PostMapping("/{cartId}/checkout")
  public ResponseEntity<ShoppingCartDto> checkout(@PathVariable final String cartId) {
    final CheckoutCartResult output = checkoutCartUseCase.execute(new CheckoutCartCommand(cartId));

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
