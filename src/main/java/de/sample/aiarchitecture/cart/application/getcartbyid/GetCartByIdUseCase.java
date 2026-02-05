package de.sample.aiarchitecture.cart.application.getcartbyid;

import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort;
import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartArticle;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.EnrichedCart;
import de.sample.aiarchitecture.cart.domain.model.EnrichedCartFactory;
import de.sample.aiarchitecture.cart.domain.model.EnrichedCartItem;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
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
  private final EnrichedCartFactory enrichedCartFactory;

  public GetCartByIdUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final ArticleDataPort articleDataPort,
      final EnrichedCartFactory enrichedCartFactory) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.articleDataPort = articleDataPort;
    this.enrichedCartFactory = enrichedCartFactory;
  }

  @Override
  public @NonNull GetCartByIdResult execute(@NonNull final GetCartByIdQuery input) {
    final CartId cartId = CartId.of(input.cartId());

    final Optional<ShoppingCart> cartOpt = shoppingCartRepository.findById(cartId);

    if (cartOpt.isEmpty()) {
      return GetCartByIdResult.notFound();
    }

    final ShoppingCart cart = cartOpt.get();

    // Fetch fresh article data for all cart items
    final Set<ProductId> productIds = cart.items().stream()
        .map(item -> item.productId())
        .collect(Collectors.toSet());

    final Map<ProductId, CartArticle> articleDataMap = articleDataPort.getArticleData(productIds);

    // Create enriched cart using factory
    final EnrichedCart enrichedCart = enrichedCartFactory.create(cart, articleDataMap);

    // Map enriched cart items to result
    final List<GetCartByIdResult.CartItemSummary> items = enrichedCart.items().stream()
        .map(this::mapToCartItemSummary)
        .toList();

    final Money total = enrichedCart.calculateCurrentSubtotal();

    return GetCartByIdResult.found(
        cart.id().value(),
        cart.customerId().value(),
        cart.status().name(),
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
