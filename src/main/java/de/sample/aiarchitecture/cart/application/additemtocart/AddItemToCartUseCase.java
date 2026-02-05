package de.sample.aiarchitecture.cart.application.additemtocart;

import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort;
import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartArticle;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.Quantity;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.DomainEventPublisher;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.Price;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for adding an item to a shopping cart.
 *
 * <p>This use case orchestrates adding an item to the cart by:
 * <ol>
 *   <li>Retrieving the cart and article data</li>
 *   <li>Validating business rules (product existence, stock availability)</li>
 *   <li>Adding the item to cart (business logic in aggregate)</li>
 *   <li>Persisting the updated cart</li>
 *   <li>Publishing domain events</li>
 * </ol>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link AddItemToCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 *
 * <p><b>Bounded Context Isolation:</b> This use case accesses article data through a single
 * output port that aggregates data from multiple contexts:
 * <ul>
 *   <li>{@link ArticleDataPort} - provides name (from Product), price (from Pricing),
 *       and stock (from Inventory) information
 * </ul>
 */
@Service
@Transactional
public class AddItemToCartUseCase implements AddItemToCartInputPort {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ArticleDataPort articleDataPort;
    private final DomainEventPublisher eventPublisher;

    public AddItemToCartUseCase(
        final ShoppingCartRepository shoppingCartRepository,
        final ArticleDataPort articleDataPort,
        final DomainEventPublisher eventPublisher) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.articleDataPort = articleDataPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AddItemToCartResult execute(final AddItemToCartCommand input) {
        final CartId cartId = CartId.of(input.cartId());
        final ProductId productId = ProductId.of(input.productId());
        final Quantity quantity = new Quantity(input.quantity());

        // Retrieve cart
        final ShoppingCart cart =
            shoppingCartRepository
                .findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + input.cartId()));

        // Retrieve article data through output port (includes product existence, pricing, and stock)
        final CartArticle cartArticle =
            articleDataPort
                .getArticleData(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + input.productId()));

        // Business rule: Check if product has sufficient stock using CartArticle domain method
        if (!cartArticle.hasStockFor(quantity.value())) {
            throw new IllegalArgumentException("Insufficient stock for product: " + input.productId());
        }

        final Price priceAtAddition = Price.of(cartArticle.currentPrice());

        // Add item to cart (business logic in aggregate)
        cart.addItem(productId, quantity, priceAtAddition);

        // Persist
        shoppingCartRepository.save(cart);

        // Publish domain events
        eventPublisher.publishAndClearEvents(cart);

        // Map to output
        final List<AddItemToCartResult.CartItemSummary> items = cart.items().stream()
            .map(item -> new AddItemToCartResult.CartItemSummary(
                item.id().value().toString(),
                item.productId().value().toString(),
                item.quantity().value(),
                item.priceAtAddition().value().amount(),
                item.priceAtAddition().value().currency().getCurrencyCode()
            ))
            .toList();

        final Money total = cart.calculateTotal();

        return new AddItemToCartResult(
            cart.id().value(),
            cart.customerId().value(),
            items,
            total.amount(),
            total.currency().getCurrencyCode()
        );
    }
}
