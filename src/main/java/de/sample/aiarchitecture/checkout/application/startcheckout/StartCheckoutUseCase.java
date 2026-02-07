package de.sample.aiarchitecture.checkout.application.startcheckout;

import de.sample.aiarchitecture.checkout.application.shared.CartData;
import de.sample.aiarchitecture.checkout.application.shared.CartDataPort;
import de.sample.aiarchitecture.checkout.application.shared.CheckoutArticleDataPort;
import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutArticle;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItem;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItemId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.DomainEventPublisher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for starting a checkout session from a shopping cart.
 *
 * <p>This use case creates a new checkout session by:
 * <ul>
 *   <li>Loading the cart data through the Anti-Corruption Layer</li>
 *   <li>Fetching article data (name, price, availability) via CheckoutArticleDataPort</li>
 *   <li>Creating checkout line items with current product details</li>
 *   <li>Creating and persisting the checkout session</li>
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link StartCheckoutInputPort}
 * interface, which is a primary/driving port in the application layer.
 *
 * <p><b>Bounded Context Isolation:</b> This use case accesses:
 * <ul>
 *   <li>Cart data through {@link CartDataPort} output port</li>
 *   <li>Article data (name, price, availability) through {@link CheckoutArticleDataPort} output port</li>
 * </ul>
 * This isolates the Checkout context from direct coupling to other contexts' domain models.
 */
@Service
@Transactional
public class StartCheckoutUseCase implements StartCheckoutInputPort {

    private final CartDataPort cartDataPort;
    private final CheckoutArticleDataPort checkoutArticleDataPort;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final DomainEventPublisher domainEventPublisher;

    public StartCheckoutUseCase(
        final CartDataPort cartDataPort,
        final CheckoutArticleDataPort checkoutArticleDataPort,
        final CheckoutSessionRepository checkoutSessionRepository,
        final DomainEventPublisher domainEventPublisher) {
        this.cartDataPort = cartDataPort;
        this.checkoutArticleDataPort = checkoutArticleDataPort;
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public StartCheckoutResult execute(final StartCheckoutCommand command) {
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

        // Collect product IDs from cart items
        final List<ProductId> productIds = cart.items().stream()
            .map(CartData.CartItemData::productId)
            .toList();

        // Fetch article data (name, current price, availability) for all line items
        final Map<ProductId, CheckoutArticle> articleDataMap =
            checkoutArticleDataPort.getArticleData(productIds);

        // Create checkout line items from cart items with fresh pricing
        final List<CheckoutLineItem> lineItems = new ArrayList<>();
        Money subtotal = Money.euro(0.0);

        for (final CartData.CartItemData cartItem : cart.items()) {
            final CheckoutArticle article = articleDataMap.get(cartItem.productId());
            if (article == null) {
                throw new IllegalArgumentException(
                    "Product not found: " + cartItem.productId().value());
            }

            final CheckoutLineItem lineItem =
                CheckoutLineItem.of(
                    CheckoutLineItemId.generate(),
                    cartItem.productId(),
                    article.name(),
                    article.currentPrice(),
                    cartItem.quantity(),
                    article.imageUrl());

            lineItems.add(lineItem);
            subtotal = subtotal.add(lineItem.lineTotal());
        }

        // Create checkout session
        final CheckoutSession session =
            CheckoutSession.start(cart.cartId(), cart.customerId(), lineItems, subtotal);

        // Save checkout session
        checkoutSessionRepository.save(session);

        // Publish domain events (e.g., CheckoutSessionStarted)
        domainEventPublisher.publishAndClearEvents(session);

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
