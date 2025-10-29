package de.sample.aiarchitecture.application;

import de.sample.aiarchitecture.domain.model.cart.CartId;
import de.sample.aiarchitecture.domain.model.cart.ShoppingCart;
import de.sample.aiarchitecture.domain.model.cart.ShoppingCartRepository;
import de.sample.aiarchitecture.domain.model.shared.Money;
import de.sample.aiarchitecture.infrastructure.api.DomainEventPublisher;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for checking out a shopping cart.
 *
 * <p>This use case orchestrates the checkout process by:
 * <ol>
 *   <li>Retrieving the cart</li>
 *   <li>Checking out (business logic in aggregate validates rules)</li>
 *   <li>Persisting the updated cart</li>
 *   <li>Publishing domain events</li>
 * </ol>
 */
@Service
@Transactional
public class CheckoutCartUseCase implements UseCase<CheckoutCartInput, CheckoutCartOutput> {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;

  public CheckoutCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public @NonNull CheckoutCartOutput execute(@NonNull final CheckoutCartInput input) {
    final CartId cartId = CartId.of(input.cartId());

    // Retrieve cart
    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + input.cartId()));

    // Checkout (business logic validates rules: cart not empty, cart is active)
    cart.checkout();

    // Persist
    shoppingCartRepository.save(cart);

    // Publish domain events
    eventPublisher.publishAndClearEvents(cart);

    // Map to output
    final List<CheckoutCartOutput.CartItemSummary> items = cart.items().stream()
        .map(item -> new CheckoutCartOutput.CartItemSummary(
            item.id().value().toString(),
            item.productId().value().toString(),
            item.quantity().value(),
            item.priceAtAddition().value().amount(),
            item.priceAtAddition().value().currency().getCurrencyCode()
        ))
        .toList();

    final Money total = cart.calculateTotal();

    return new CheckoutCartOutput(
        cart.id().value(),
        cart.customerId().value(),
        items,
        total.amount(),
        total.currency().getCurrencyCode(),
        Instant.now() // Note: In production, this should come from the aggregate or an event
    );
  }
}
