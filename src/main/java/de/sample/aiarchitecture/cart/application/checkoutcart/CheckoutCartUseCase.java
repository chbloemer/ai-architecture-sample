package de.sample.aiarchitecture.cart.application.checkoutcart;

import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort;
import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.ArticlePrice;
import de.sample.aiarchitecture.cart.domain.model.ArticlePriceResolver;
import de.sample.aiarchitecture.cart.domain.model.CartArticle;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CartValidationResult;
import de.sample.aiarchitecture.cart.domain.model.EnrichedCart;
import de.sample.aiarchitecture.cart.domain.model.EnrichedCartFactory;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.DomainEventPublisher;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
  private final EnrichedCartFactory enrichedCartFactory;
  private final DomainEventPublisher eventPublisher;

  public CheckoutCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final ArticleDataPort articleDataPort,
      final EnrichedCartFactory enrichedCartFactory,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.articleDataPort = articleDataPort;
    this.enrichedCartFactory = enrichedCartFactory;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public CheckoutCartResult execute(final CheckoutCartCommand input) {
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

    final Map<ProductId, CartArticle> articleDataMap = articleDataPort.getArticleData(productIds);

    // Create enriched cart using factory for validation
    final EnrichedCart enrichedCart = enrichedCartFactory.create(cart, articleDataMap);

    // Validate cart using EnrichedCart domain methods
    if (!enrichedCart.isValidForCheckout()) {
      // Build resolver from CartArticle data for legacy validation result
      final ArticlePriceResolver resolver = buildResolver(articleDataMap);
      final CartValidationResult validationResult = cart.validateForCheckout(resolver);
      throw new CartValidationException(validationResult);
    }

    // Checkout (business logic validates rules: cart not empty, cart is active)
    cart.checkout();

    // Persist
    shoppingCartRepository.save(cart);

    // Publish domain events
    eventPublisher.publishAndClearEvents(cart);

    // Map to output using enriched cart items
    final List<CheckoutCartResult.CartItemSummary> items = enrichedCart.items().stream()
        .map(item -> new CheckoutCartResult.CartItemSummary(
            item.cartItemId().value().toString(),
            item.productId().value().toString(),
            item.quantity().value(),
            item.currentArticle().currentPrice().amount(),
            item.currentArticle().currentPrice().currency().getCurrencyCode()
        ))
        .toList();

    final Money total = enrichedCart.calculateCurrentSubtotal();

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
   * Kept for backward compatibility with CartValidationResult.
   *
   * @param articleDataMap the map of product IDs to CartArticle
   * @return a resolver that provides pricing information
   */
  private ArticlePriceResolver buildResolver(final Map<ProductId, CartArticle> articleDataMap) {
    return productId -> {
      final CartArticle article = articleDataMap.get(productId);
      if (article == null) {
        // Product not found - treat as unavailable
        return new ArticlePrice(Money.euro(0.0), false, 0);
      }
      return new ArticlePrice(article.currentPrice(), article.isAvailable(), article.availableStock());
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
