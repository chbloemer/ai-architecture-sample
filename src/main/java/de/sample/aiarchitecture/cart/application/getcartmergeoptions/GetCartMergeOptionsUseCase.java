package de.sample.aiarchitecture.cart.application.getcartmergeoptions;

import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartItem;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for checking if cart merge options should be presented.
 *
 * <p>When a user logs in, this use case determines if both the anonymous session
 * and the registered account have active carts with items. If so, the user should
 * be presented with options to choose how to handle the cart conflict.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetCartMergeOptionsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetCartMergeOptionsUseCase implements GetCartMergeOptionsInputPort {

  private final ShoppingCartRepository shoppingCartRepository;

  public GetCartMergeOptionsUseCase(final ShoppingCartRepository shoppingCartRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
  }

  @Override
  public @NonNull GetCartMergeOptionsResult execute(@NonNull final GetCartMergeOptionsQuery input) {
    final CustomerId anonymousCustomerId = CustomerId.of(input.anonymousUserId());
    final CustomerId registeredCustomerId = CustomerId.of(input.registeredUserId());

    // If same user ID, no merge needed (identity continuity case)
    if (anonymousCustomerId.equals(registeredCustomerId)) {
      return GetCartMergeOptionsResult.noMergeRequired();
    }

    // Find anonymous user's active cart
    final Optional<ShoppingCart> anonymousCart =
        shoppingCartRepository.findActiveCartByCustomerId(anonymousCustomerId);

    // Find registered user's active cart
    final Optional<ShoppingCart> accountCart =
        shoppingCartRepository.findActiveCartByCustomerId(registeredCustomerId);

    // Check if both carts exist and have items
    final boolean anonymousHasItems = anonymousCart.isPresent() && !anonymousCart.get().isEmpty();
    final boolean accountHasItems = accountCart.isPresent() && !accountCart.get().isEmpty();

    if (anonymousHasItems && accountHasItems) {
      // Both carts have items - user must choose
      return GetCartMergeOptionsResult.mergeRequired(
          toCartSummary(anonymousCart.get()),
          toCartSummary(accountCart.get())
      );
    }

    // No merge required - either one or both carts are empty
    return GetCartMergeOptionsResult.noMergeRequired();
  }

  private GetCartMergeOptionsResult.CartSummary toCartSummary(final ShoppingCart cart) {
    final Money total = cart.calculateTotal();
    return new GetCartMergeOptionsResult.CartSummary(
        cart.id().value(),
        cart.itemCount(),
        cart.totalQuantity(),
        total.amount(),
        total.currency().getCurrencyCode(),
        cart.items().stream()
            .map(this::toItemSummary)
            .toList()
    );
  }

  private GetCartMergeOptionsResult.CartItemSummary toItemSummary(final CartItem item) {
    return new GetCartMergeOptionsResult.CartItemSummary(
        item.productId().value(),
        item.quantity().value(),
        item.priceAtAddition().value().amount(),
        item.priceAtAddition().value().currency().getCurrencyCode()
    );
  }
}
