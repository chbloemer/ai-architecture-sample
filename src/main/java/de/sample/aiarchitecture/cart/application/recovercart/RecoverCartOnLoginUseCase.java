package de.sample.aiarchitecture.cart.application.recovercart;

import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CartItem;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.DomainEventPublisher;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for recovering cart on login.
 *
 * <p>When a registered user logs in from a new device/browser:
 * <ol>
 *   <li>Find cart for registered user's UserId</li>
 *   <li>Find cart for anonymous user's UserId (if different)</li>
 *   <li>If both exist, merge items from anonymous cart into registered cart</li>
 *   <li>Delete the anonymous cart after merge</li>
 * </ol>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link RecoverCartOnLoginInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class RecoverCartOnLoginUseCase implements RecoverCartOnLoginInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;

  public RecoverCartOnLoginUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public @NonNull RecoverCartOnLoginResponse execute(@NonNull final RecoverCartOnLoginCommand input) {
    final CustomerId anonymousCustomerId = CustomerId.of(input.anonymousUserId());
    final CustomerId registeredCustomerId = CustomerId.of(input.registeredUserId());

    // If same user ID, no merge needed
    if (anonymousCustomerId.equals(registeredCustomerId)) {
      return RecoverCartOnLoginResponse.noRecoveryNeeded(registeredCustomerId.value());
    }

    // Find anonymous user's active cart
    final Optional<ShoppingCart> anonymousCart =
        shoppingCartRepository.findActiveCartByCustomerId(anonymousCustomerId);

    // If no anonymous cart exists, no recovery needed
    if (anonymousCart.isEmpty() || anonymousCart.get().isEmpty()) {
      return RecoverCartOnLoginResponse.noRecoveryNeeded(registeredCustomerId.value());
    }

    // Find or create registered user's active cart
    final ShoppingCart registeredCart = shoppingCartRepository
        .findActiveCartByCustomerId(registeredCustomerId)
        .orElseGet(() -> {
          final ShoppingCart newCart = new ShoppingCart(CartId.generate(), registeredCustomerId);
          return shoppingCartRepository.save(newCart);
        });

    // Merge items from anonymous cart into registered cart
    final ShoppingCart anonCart = anonymousCart.get();
    int itemsMerged = 0;

    for (final CartItem item : anonCart.items()) {
      registeredCart.addItem(item.productId(), item.quantity(), item.priceAtAddition());
      itemsMerged++;
    }

    // Save the merged cart
    shoppingCartRepository.save(registeredCart);

    // Publish domain events from the registered cart
    eventPublisher.publishAndClearEvents(registeredCart);

    // Delete the anonymous cart
    shoppingCartRepository.deleteById(anonCart.id());

    // Map to output
    final List<RecoverCartOnLoginResponse.CartItemSummary> items = registeredCart.items().stream()
        .map(item -> new RecoverCartOnLoginResponse.CartItemSummary(
            item.id().value(),
            item.productId().value(),
            item.quantity().value(),
            item.priceAtAddition().value().amount(),
            item.priceAtAddition().value().currency().getCurrencyCode()
        ))
        .toList();

    final Money total = registeredCart.calculateTotal();

    return new RecoverCartOnLoginResponse(
        registeredCart.id().value(),
        registeredCart.customerId().value(),
        items,
        total.amount(),
        total.currency().getCurrencyCode(),
        itemsMerged,
        true
    );
  }
}
