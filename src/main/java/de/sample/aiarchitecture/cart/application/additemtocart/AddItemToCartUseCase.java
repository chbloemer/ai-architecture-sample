package de.sample.aiarchitecture.cart.application.additemtocart;

import de.sample.aiarchitecture.cart.application.shared.ProductDataPort;
import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.Quantity;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.application.port.DomainEventPublisher;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for adding an item to a shopping cart.
 *
 * <p>This use case orchestrates adding an item to the cart by:
 * <ol>
 *   <li>Retrieving the cart and product data</li>
 *   <li>Validating business rules (stock availability)</li>
 *   <li>Adding the item to cart (business logic in aggregate)</li>
 *   <li>Persisting the updated cart</li>
 *   <li>Publishing domain events</li>
 * </ol>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link AddItemToCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 *
 * <p><b>Bounded Context Isolation:</b> This use case accesses product data through the
 * {@link ProductDataPort} output port, isolating the Cart context from direct coupling
 * to the Product context's domain model.
 */
@Service
@Transactional
public class AddItemToCartUseCase implements AddItemToCartInputPort {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductDataPort productDataPort;
    private final DomainEventPublisher eventPublisher;

    public AddItemToCartUseCase(
        final ShoppingCartRepository shoppingCartRepository,
        final ProductDataPort productDataPort,
        final DomainEventPublisher eventPublisher) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.productDataPort = productDataPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public @NonNull AddItemToCartResponse execute(@NonNull final AddItemToCartCommand input) {
        final CartId cartId = CartId.of(input.cartId());
        final ProductId productId = ProductId.of(input.productId());
        final Quantity quantity = new Quantity(input.quantity());

        // Retrieve cart
        final ShoppingCart cart =
            shoppingCartRepository
                .findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + input.cartId()));

        // Retrieve product data through output port
        final ProductDataPort.ProductData productData =
            productDataPort
                .getProductData(productId, quantity.value())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + input.productId()));

        // Business rule: Check if product has sufficient stock
        if (!productData.hasStock()) {
            throw new IllegalArgumentException("Insufficient stock for product: " + input.productId());
        }

        // Add item to cart (business logic in aggregate)
        cart.addItem(productId, quantity, productData.price());

        // Persist
        shoppingCartRepository.save(cart);

        // Publish domain events
        eventPublisher.publishAndClearEvents(cart);

        // Map to output
        final List<AddItemToCartResponse.CartItemSummary> items = cart.items().stream()
            .map(item -> new AddItemToCartResponse.CartItemSummary(
                item.id().value().toString(),
                item.productId().value().toString(),
                item.quantity().value(),
                item.priceAtAddition().value().amount(),
                item.priceAtAddition().value().currency().getCurrencyCode()
            ))
            .toList();

        final Money total = cart.calculateTotal();

        return new AddItemToCartResponse(
            cart.id().value(),
            cart.customerId().value(),
            items,
            total.amount(),
            total.currency().getCurrencyCode()
        );
    }
}
