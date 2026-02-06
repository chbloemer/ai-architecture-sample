package de.sample.aiarchitecture.cart.application.getcartbyid;

import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort;
import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartArticle;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.EnrichedCart;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a shopping cart by its ID.
 *
 * <p>This is a query use case that retrieves cart details without modifying state.
 * Returns a {@link GetCartByIdResult} containing an {@link EnrichedCart}
 * read model that combines cart state with current article data (pricing, availability).
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

    // Collect product IDs and fetch article data in batch
    final Set<ProductId> productIds = cart.items().stream()
        .map(item -> item.productId())
        .collect(Collectors.toSet());

    final Map<ProductId, CartArticle> articleData = articleDataPort.getArticleData(productIds);

    // Create enriched cart using factory method
    final EnrichedCart enrichedCart = EnrichedCart.from(cart, articleData);

    return GetCartByIdResult.found(enrichedCart);
  }
}
