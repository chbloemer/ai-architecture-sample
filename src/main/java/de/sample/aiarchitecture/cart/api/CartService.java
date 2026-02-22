package de.sample.aiarchitecture.cart.api;

import de.sample.aiarchitecture.cart.application.checkoutcart.CheckoutCartCommand;
import de.sample.aiarchitecture.cart.application.checkoutcart.CheckoutCartInputPort;
import de.sample.aiarchitecture.cart.application.completecart.CompleteCartCommand;
import de.sample.aiarchitecture.cart.application.completecart.CompleteCartInputPort;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdInputPort;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdQuery;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdResult;
import de.sample.aiarchitecture.cart.domain.model.CartStatus;
import de.sample.aiarchitecture.sharedkernel.domain.model.Price;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.strategic.OpenHostService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Open Host Service for Shopping Cart.
 *
 * <p>Exposes cart data and checkout operations for cross-module access. The Checkout context uses
 * this service instead of directly accessing Cart internals (repository, domain model).
 *
 * <p><b>Hexagonal Architecture:</b> As an incoming adapter, this service calls input ports (use
 * cases), NOT output ports (repositories) directly.
 */
@OpenHostService(
    context = "Shopping Cart",
    description =
        "Provides cart data and checkout operations for other bounded contexts (primarily Checkout)")
@Service
public class CartService {

  private final GetCartByIdInputPort getCartByIdInputPort;
  private final CheckoutCartInputPort checkoutCartInputPort;
  private final CompleteCartInputPort completeCartInputPort;

  public CartService(
      GetCartByIdInputPort getCartByIdInputPort,
      CheckoutCartInputPort checkoutCartInputPort,
      CompleteCartInputPort completeCartInputPort) {
    this.getCartByIdInputPort = getCartByIdInputPort;
    this.checkoutCartInputPort = checkoutCartInputPort;
    this.completeCartInputPort = completeCartInputPort;
  }

  /**
   * Snapshot of a shopping cart for cross-context communication.
   *
   * @param cartId the cart ID
   * @param customerId the customer ID
   * @param items the cart items
   * @param active whether the cart is active
   */
  public record CartSnapshot(
      UUID cartId, String customerId, List<CartItemSnapshot> items, boolean active) {}

  /**
   * Snapshot of a cart item for cross-context communication.
   *
   * @param productId the product ID
   * @param priceAtAddition the price when item was added
   * @param quantity the item quantity
   */
  public record CartItemSnapshot(ProductId productId, Price priceAtAddition, int quantity) {}

  /**
   * Retrieves cart data by ID.
   *
   * @param cartId the cart ID
   * @return cart snapshot if found
   */
  public Optional<CartSnapshot> findCartById(UUID cartId) {
    GetCartByIdResult result =
        getCartByIdInputPort.execute(new GetCartByIdQuery(cartId.toString()));

    if (!result.found()) {
      return Optional.empty();
    }

    var enrichedCart = result.cart().orElseThrow();

    List<CartItemSnapshot> items =
        enrichedCart.items().stream()
            .map(
                item ->
                    new CartItemSnapshot(
                        item.productId(), item.priceAtAddition(), item.quantity().value()))
            .toList();

    return Optional.of(
        new CartSnapshot(
            UUID.fromString(enrichedCart.cartId().value()),
            enrichedCart.customerId().value(),
            items,
            enrichedCart.status() == CartStatus.ACTIVE));
  }

  /**
   * Marks a cart as checked out.
   *
   * @param cartId the cart ID to check out
   */
  public void markAsCheckedOut(UUID cartId) {
    checkoutCartInputPort.execute(new CheckoutCartCommand(cartId.toString()));
  }

  /**
   * Marks a cart as completed after checkout confirmation.
   *
   * @param cartId the cart ID to complete
   */
  public void completeCart(UUID cartId) {
    completeCartInputPort.execute(new CompleteCartCommand(cartId.toString()));
  }
}
