package de.sample.aiarchitecture.cart.application.getcartbyid;

import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort;
import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.ArticleInfo;
import de.sample.aiarchitecture.cart.domain.model.ArticleInfoResolver;
import de.sample.aiarchitecture.cart.domain.model.CartArticle;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.EnrichedCart;
import de.sample.aiarchitecture.cart.domain.model.EnrichedCartItem;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.cart.domain.readmodel.EnrichedCartBuilder;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a shopping cart by its ID.
 *
 * <p>This is a query use case that retrieves cart details without modifying state.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetCartByIdInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetCartByIdUseCase implements GetCartByIdInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final ArticleDataPort articleDataPort;

  public GetCartByIdUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final ArticleDataPort articleDataPort) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.articleDataPort = articleDataPort;
  }

  @Override
  public GetCartByIdResult execute(final GetCartByIdQuery input) {
    final CartId cartId = CartId.of(input.cartId());

    final Optional<ShoppingCart> cartOpt = shoppingCartRepository.findById(cartId);

    if (cartOpt.isEmpty()) {
      return GetCartByIdResult.notFound();
    }

    final ShoppingCart cart = cartOpt.get();

    // Create a caching resolver that lazily fetches article data
    final Map<ProductId, CartArticle> articleCache = new HashMap<>();
    final ArticleInfoResolver resolver = productId -> {
      final CartArticle article = articleCache.computeIfAbsent(productId,
          id -> articleDataPort.getArticleData(id).orElseThrow(
              () -> new IllegalStateException("Article data not found for product: " + id.value())));
      return new ArticleInfo(article.name(), article.currentPrice());
    };

    // Use builder pattern with Interest Interface
    final EnrichedCartBuilder builder = new EnrichedCartBuilder();
    cart.provideStateTo(builder, resolver);

    // Push current article data for each product
    for (final ProductId productId : builder.getCollectedProductIds()) {
      final CartArticle article = articleCache.get(productId);
      builder.receiveCurrentArticleData(productId, article);
    }

    // Build enriched cart
    final EnrichedCart enrichedCart = builder.build();

    // Map enriched cart items to result
    final List<GetCartByIdResult.CartItemSummary> items = enrichedCart.items().stream()
        .map(this::mapToCartItemSummary)
        .toList();

    final Money total = enrichedCart.calculateCurrentSubtotal();

    return GetCartByIdResult.found(
        cart.id().value(),
        enrichedCart.customerId().value(),
        enrichedCart.status().name(),
        items,
        total.amount(),
        total.currency().getCurrencyCode()
    );
  }

  private GetCartByIdResult.CartItemSummary mapToCartItemSummary(final EnrichedCartItem item) {
    final Money priceAtAddition = item.priceAtAddition().value();
    final Money currentPrice = item.currentArticle().currentPrice();

    return new GetCartByIdResult.CartItemSummary(
        item.cartItemId().value().toString(),
        item.productId().value().toString(),
        item.quantity().value(),
        priceAtAddition.amount(),
        priceAtAddition.currency().getCurrencyCode(),
        currentPrice.amount(),
        currentPrice.currency().getCurrencyCode(),
        item.currentArticle().isAvailable(),
        item.hasPriceChanged()
    );
  }
}
