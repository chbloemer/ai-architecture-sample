package de.sample.aiarchitecture.checkout.application.synccheckoutwithcart;

import de.sample.aiarchitecture.checkout.application.shared.CartData;
import de.sample.aiarchitecture.checkout.application.shared.CartDataPort;
import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItem;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItemId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for synchronizing a checkout session with current cart state.
 *
 * <p>This use case is triggered by cart change events (item added, removed, quantity changed)
 * to keep the active checkout session in sync with the cart contents.
 *
 * <p><b>Why This Matters:</b> Since carts remain ACTIVE during checkout, users can modify
 * their cart while in the checkout flow. This use case ensures the checkout session
 * reflects those changes.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SyncCheckoutWithCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class SyncCheckoutWithCartUseCase implements SyncCheckoutWithCartInputPort {

  private static final Logger logger = LoggerFactory.getLogger(SyncCheckoutWithCartUseCase.class);

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final CartDataPort cartDataPort;
  private final ProductRepository productRepository;

  public SyncCheckoutWithCartUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final CartDataPort cartDataPort,
      final ProductRepository productRepository) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.cartDataPort = cartDataPort;
    this.productRepository = productRepository;
  }

  @Override
  public @NonNull SyncCheckoutWithCartResponse execute(
      @NonNull final SyncCheckoutWithCartCommand command) {

    final CartId cartId = CartId.of(command.cartId());

    // Find active checkout session for this cart
    final Optional<CheckoutSession> activeSession =
        checkoutSessionRepository.findActiveByCartId(cartId);

    if (activeSession.isEmpty()) {
      logger.debug("No active checkout session for cart {}, skipping sync", command.cartId());
      return SyncCheckoutWithCartResponse.noActiveSession();
    }

    final CheckoutSession session = activeSession.get();

    // Load current cart data through ACL
    final CartData cart =
        cartDataPort
            .findById(cartId)
            .orElseThrow(
                () -> new IllegalStateException("Cart not found for active session: " + command.cartId()));

    // Handle empty cart - this shouldn't normally happen, but handle gracefully
    if (cart.items().isEmpty()) {
      logger.warn("Cart {} is empty but has active checkout session {}, skipping sync",
          command.cartId(), session.id().value());
      return SyncCheckoutWithCartResponse.noActiveSession();
    }

    // Build new line items from current cart state
    final List<CheckoutLineItem> newLineItems = new ArrayList<>();
    Money subtotal = Money.euro(0.0);

    for (final CartData.CartItemData cartItem : cart.items()) {
      // Load product to get current name
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
              cartItem.quantity());

      newLineItems.add(lineItem);
      subtotal = subtotal.add(lineItem.lineTotal());
    }

    // Sync line items to session
    session.syncLineItems(newLineItems, subtotal);

    // Persist updated session
    checkoutSessionRepository.save(session);

    logger.info("Synced checkout session {} with cart {} - {} items, subtotal: {}",
        session.id().value(), command.cartId(), newLineItems.size(), subtotal);

    return SyncCheckoutWithCartResponse.synced(
        session.id().value().toString(),
        newLineItems.size());
  }
}
