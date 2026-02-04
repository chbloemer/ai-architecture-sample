package de.sample.aiarchitecture.cart.application.checkoutcart;

import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort;
import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort.ArticleData;
import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.ArticlePrice;
import de.sample.aiarchitecture.cart.domain.model.ArticlePriceResolver;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CartValidationResult;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.DomainEventPublisher;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for checking out a shopping cart.
 *
 * <p>This use case orchestrates the checkout process by:
 * <ol>
 *   <li>Retrieving the cart</li>
 *   <li>Fetching fresh article data for all cart items</li>
 *   <li>Validating the cart with current availability and stock</li>
 *   <li>Checking out (business logic in aggregate validates rules)</li>
 *   <li>Persisting the updated cart</li>
 *   <li>Publishing domain events</li>
 * </ol>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link CheckoutCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class CheckoutCartUseCase implements CheckoutCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final ArticleDataPort articleDataPort;
  private final DomainEventPublisher eventPublisher;

  public CheckoutCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final ArticleDataPort articleDataPort,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.articleDataPort = articleDataPort;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public @NonNull CheckoutCartResult execute(@NonNull final CheckoutCartCommand input) {
    final CartId cartId = CartId.of(input.cartId());

    // Retrieve cart
    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + input.cartId()));

    // Fetch article data for all cart items
    final Set<ProductId> productIds = cart.items().stream()
        .map(item -> item.productId())
        .collect(Collectors.toSet());

    final Map<ProductId, ArticleData> articleDataMap = articleDataPort.getArticleData(productIds);

    // Build resolver from fetched data
    final ArticlePriceResolver resolver = buildResolver(articleDataMap);

    // Validate cart with fresh data
    final CartValidationResult validationResult = cart.validateForCheckout(resolver);
    if (!validationResult.isValid()) {
      throw new CartValidationException(validationResult);
    }

    // Checkout (business logic validates rules: cart not empty, cart is active)
    cart.checkout();

    // Persist
    shoppingCartRepository.save(cart);

    // Publish domain events
    eventPublisher.publishAndClearEvents(cart);

    // Map to output using fresh prices from resolver
    final List<CheckoutCartResult.CartItemSummary> items = cart.items().stream()
        .map(item -> {
          final ArticlePrice articlePrice = resolver.resolve(item.productId());
          return new CheckoutCartResult.CartItemSummary(
              item.id().value().toString(),
              item.productId().value().toString(),
              item.quantity().value(),
              articlePrice.price().amount(),
              articlePrice.price().currency().getCurrencyCode()
          );
        })
        .toList();

    final Money total = cart.calculateTotal(resolver);

    return new CheckoutCartResult(
        cart.id().value(),
        cart.customerId().value(),
        items,
        total.amount(),
        total.currency().getCurrencyCode(),
        Instant.now() // Note: In production, this should come from the aggregate or an event
    );
  }

  /**
   * Builds an ArticlePriceResolver from the fetched article data.
   *
   * @param articleDataMap the map of product IDs to article data
   * @return a resolver that provides pricing information
   */
  private ArticlePriceResolver buildResolver(final Map<ProductId, ArticleData> articleDataMap) {
    return productId -> {
      final ArticleData data = articleDataMap.get(productId);
      if (data == null) {
        // Product not found - treat as unavailable
        return new ArticlePrice(Money.euro(0.0), false, 0);
      }
      return new ArticlePrice(data.currentPrice(), data.isAvailable(), data.availableStock());
    };
  }

  /**
   * Exception thrown when cart validation fails during checkout.
   */
  public static class CartValidationException extends RuntimeException {
    private final CartValidationResult validationResult;

    public CartValidationException(final CartValidationResult validationResult) {
      super("Cart validation failed: " + validationResult.errors().size() + " error(s)");
      this.validationResult = validationResult;
    }

    public CartValidationResult getValidationResult() {
      return validationResult;
    }
  }
}
