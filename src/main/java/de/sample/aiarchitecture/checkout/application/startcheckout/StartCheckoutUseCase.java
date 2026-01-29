package de.sample.aiarchitecture.checkout.application.startcheckout;

import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CartItem;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItem;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItemId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for starting a checkout session from a shopping cart.
 *
 * <p>This use case creates a new checkout session by:
 * <ul>
 *   <li>Loading the cart and validating it can be checked out</li>
 *   <li>Loading product information for line items</li>
 *   <li>Creating checkout line items with product details</li>
 *   <li>Creating and persisting the checkout session</li>
 *   <li>Marking the cart as checked out</li>
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link StartCheckoutInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class StartCheckoutUseCase implements StartCheckoutInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final ProductRepository productRepository;
  private final CheckoutSessionRepository checkoutSessionRepository;

  public StartCheckoutUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final ProductRepository productRepository,
      final CheckoutSessionRepository checkoutSessionRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.productRepository = productRepository;
    this.checkoutSessionRepository = checkoutSessionRepository;
  }

  @Override
  public @NonNull StartCheckoutResponse execute(@NonNull final StartCheckoutCommand command) {
    // Load cart
    final CartId cartId = CartId.of(command.cartId());
    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + command.cartId()));

    // Validate cart can be checked out
    if (!cart.isActive()) {
      throw new IllegalArgumentException("Cart is not active: " + command.cartId());
    }
    if (cart.isEmpty()) {
      throw new IllegalArgumentException("Cannot checkout empty cart: " + command.cartId());
    }

    // Create checkout line items from cart items
    final List<CheckoutLineItem> lineItems = new ArrayList<>();
    Money subtotal = Money.euro(0.0);

    for (final CartItem cartItem : cart.items()) {
      // Load product to get name
      final Product product =
          productRepository
              .findById(cartItem.productId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Product not found: " + cartItem.productId().value()));

      final CheckoutLineItem lineItem =
          CheckoutLineItem.of(
              CheckoutLineItemId.generate(),
              cartItem.productId(),
              product.name().value(),
              cartItem.priceAtAddition().value(),
              cartItem.quantity().value());

      lineItems.add(lineItem);
      subtotal = subtotal.add(lineItem.lineTotal());
    }

    // Create checkout session
    final CheckoutSession session =
        CheckoutSession.start(cart.id(), cart.customerId(), lineItems, subtotal);

    // Save checkout session
    checkoutSessionRepository.save(session);

    // Mark cart as checked out
    cart.checkout();
    shoppingCartRepository.save(cart);

    // Map to response
    return mapToResponse(session);
  }

  private StartCheckoutResponse mapToResponse(final CheckoutSession session) {
    final List<StartCheckoutResponse.LineItemResponse> lineItemResponses =
        session.lineItems().stream()
            .map(
                item ->
                    new StartCheckoutResponse.LineItemResponse(
                        item.id().value(),
                        item.productId().value().toString(),
                        item.productName(),
                        item.unitPrice().toString(),
                        item.quantity(),
                        item.lineTotal().toString()))
            .toList();

    return new StartCheckoutResponse(
        session.id().value().toString(),
        session.cartId().value().toString(),
        session.customerId().value(),
        session.currentStep().name(),
        session.status().name(),
        lineItemResponses,
        session.totals().subtotal().toString());
  }
}
