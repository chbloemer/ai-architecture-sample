package de.sample.aiarchitecture.checkout.application.startcheckout;

import de.sample.aiarchitecture.checkout.application.shared.CartData;
import de.sample.aiarchitecture.checkout.application.shared.CartDataPort;
import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.application.shared.ProductInfoPort;
import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItem;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItemId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
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
 *   <li>Loading the cart data through the Anti-Corruption Layer</li>
 *   <li>Loading product names through the ProductInfoPort</li>
 *   <li>Creating checkout line items with product details</li>
 *   <li>Creating and persisting the checkout session</li>
 *   <li>Marking the cart as checked out</li>
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link StartCheckoutInputPort}
 * interface, which is a primary/driving port in the application layer.
 *
 * <p><b>Bounded Context Isolation:</b> This use case accesses:
 * <ul>
 *   <li>Cart data through {@link CartDataPort} output port</li>
 *   <li>Product names through {@link ProductInfoPort} output port</li>
 * </ul>
 * This isolates the Checkout context from direct coupling to other contexts' domain models.
 */
@Service
@Transactional
public class StartCheckoutUseCase implements StartCheckoutInputPort {

    private final CartDataPort cartDataPort;
    private final ProductInfoPort productInfoPort;
    private final CheckoutSessionRepository checkoutSessionRepository;

    public StartCheckoutUseCase(
        final CartDataPort cartDataPort,
        final ProductInfoPort productInfoPort,
        final CheckoutSessionRepository checkoutSessionRepository) {
        this.cartDataPort = cartDataPort;
        this.productInfoPort = productInfoPort;
        this.checkoutSessionRepository = checkoutSessionRepository;
    }

    @Override
    public @NonNull StartCheckoutResult execute(@NonNull final StartCheckoutCommand command) {
        // Load cart through ACL
        final CartId cartId = CartId.of(command.cartId());
        final CartData cart =
            cartDataPort
                .findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + command.cartId()));

        // Validate cart can be checked out
        if (!cart.active()) {
            throw new IllegalArgumentException("Cart is not active: " + command.cartId());
        }
        if (cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout empty cart: " + command.cartId());
        }

        // Create checkout line items from cart items
        final List<CheckoutLineItem> lineItems = new ArrayList<>();
        Money subtotal = Money.euro(0.0);

        for (final CartData.CartItemData cartItem : cart.items()) {
            // Load product name through output port
            final String productName =
                productInfoPort
                    .getProductName(cartItem.productId())
                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Product not found: " + cartItem.productId().value()));

            final CheckoutLineItem lineItem =
                CheckoutLineItem.of(
                    CheckoutLineItemId.generate(),
                    cartItem.productId(),
                    productName,
                    cartItem.priceAtAddition().value(),
                    cartItem.quantity());

            lineItems.add(lineItem);
            subtotal = subtotal.add(lineItem.lineTotal());
        }

        // Create checkout session
        final CheckoutSession session =
            CheckoutSession.start(cart.cartId(), cart.customerId(), lineItems, subtotal);

        // Save checkout session
        checkoutSessionRepository.save(session);

        // Cart remains ACTIVE during checkout - user can still modify it
        // Cart only transitions to COMPLETED when checkout is confirmed

        // Map to result
        return mapToResult(session);
    }

    private StartCheckoutResult mapToResult(final CheckoutSession session) {
        final List<StartCheckoutResult.LineItemData> lineItemData =
            session.lineItems().stream()
                .map(
                    item ->
                        new StartCheckoutResult.LineItemData(
                            item.id().value(),
                            item.productId().value().toString(),
                            item.productName(),
                            item.unitPrice().toString(),
                            item.quantity(),
                            item.lineTotal().toString()))
                .toList();

        return new StartCheckoutResult(
            session.id().value().toString(),
            session.cartId().value(),
            session.customerId().value(),
            session.currentStep().name(),
            session.status().name(),
            lineItemData,
            session.totals().subtotal().toString());
    }
}
