package de.sample.aiarchitecture.application.cart;

import de.sample.aiarchitecture.application.UseCase;

import de.sample.aiarchitecture.domain.model.cart.CartId;
import de.sample.aiarchitecture.domain.model.cart.Quantity;
import de.sample.aiarchitecture.domain.model.cart.ShoppingCart;
import de.sample.aiarchitecture.domain.model.cart.ShoppingCartRepository;
import de.sample.aiarchitecture.domain.model.product.Product;
import de.sample.aiarchitecture.domain.model.product.ProductRepository;
import de.sample.aiarchitecture.domain.model.shared.Money;
import de.sample.aiarchitecture.domain.model.shared.ProductId;
import de.sample.aiarchitecture.infrastructure.api.DomainEventPublisher;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for adding an item to a shopping cart.
 *
 * <p>This use case orchestrates adding an item to the cart by:
 * <ol>
 *   <li>Retrieving the cart and product</li>
 *   <li>Validating business rules (stock availability)</li>
 *   <li>Adding the item to cart (business logic in aggregate)</li>
 *   <li>Persisting the updated cart</li>
 *   <li>Publishing domain events</li>
 * </ol>
 */
@Service
@Transactional
public class AddItemToCartUseCase implements UseCase<AddItemToCartInput, AddItemToCartOutput> {

  private final ShoppingCartRepository shoppingCartRepository;
  private final ProductRepository productRepository;
  private final DomainEventPublisher eventPublisher;

  public AddItemToCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final ProductRepository productRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.productRepository = productRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public @NonNull AddItemToCartOutput execute(@NonNull final AddItemToCartInput input) {
    final CartId cartId = CartId.of(input.cartId());
    final ProductId productId = ProductId.of(input.productId());
    final Quantity quantity = new Quantity(input.quantity());

    // Retrieve cart
    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + input.cartId()));

    // Retrieve product
    final Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + input.productId()));

    // Business rule: Check if product has sufficient stock
    if (!product.hasStockFor(quantity.value())) {
      throw new IllegalArgumentException("Insufficient stock for product: " + input.productId());
    }

    // Add item to cart (business logic in aggregate)
    cart.addItem(productId, quantity, product.price());

    // Persist
    shoppingCartRepository.save(cart);

    // Publish domain events
    eventPublisher.publishAndClearEvents(cart);

    // Map to output
    final List<AddItemToCartOutput.CartItemSummary> items = cart.items().stream()
        .map(item -> new AddItemToCartOutput.CartItemSummary(
            item.id().value().toString(),
            item.productId().value().toString(),
            item.quantity().value(),
            item.priceAtAddition().value().amount(),
            item.priceAtAddition().value().currency().getCurrencyCode()
        ))
        .toList();

    final Money total = cart.calculateTotal();

    return new AddItemToCartOutput(
        cart.id().value(),
        cart.customerId().value(),
        items,
        total.amount(),
        total.currency().getCurrencyCode()
    );
  }
}
