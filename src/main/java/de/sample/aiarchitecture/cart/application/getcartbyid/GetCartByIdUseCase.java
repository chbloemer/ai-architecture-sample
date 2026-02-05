package de.sample.aiarchitecture.cart.application.getcartbyid;

import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort;
import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartArticle;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.cart.domain.readmodel.EnrichedCartBuilder;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a shopping cart by its ID.
 *
 * <p>This is a query use case that retrieves cart details without modifying state.
 * Returns a {@link GetCartByIdResult} containing an {@link de.sample.aiarchitecture.cart.domain.model.EnrichedCart}
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

    // Use builder pattern with Interest Interface
    final EnrichedCartBuilder builder = new EnrichedCartBuilder();
    cart.provideStateTo(builder);

    // Fetch and push current article data for each product
    for (final ProductId productId : builder.getCollectedProductIds()) {
      final CartArticle article = articleDataPort.getArticleData(productId)
          .orElseThrow(() -> new IllegalStateException(
              "Article data not found for product: " + productId.value()));
      builder.receiveCurrentArticleData(productId, article);
    }

    // Build enriched cart and wrap in result
    return GetCartByIdResult.found(builder.build());
  }
}
