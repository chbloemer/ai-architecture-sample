package de.sample.aiarchitecture.cart.application.getcartbyid;

import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort;
import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort.ArticleData;
import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartId;
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

  public GetCartByIdUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final ArticleDataPort articleDataPort) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.articleDataPort = articleDataPort;
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

    final Map<ProductId, ArticleData> articleDataMap = articleDataPort.getArticleData(productIds);

    // Map cart items with fresh pricing and availability data
    final List<GetCartByIdResult.CartItemSummary> items = cart.items().stream()
        .map(item -> {
          final ArticleData articleData = articleDataMap.get(item.productId());
          final Money priceAtAddition = item.priceAtAddition().value();

          if (articleData != null) {
            final Money currentPrice = articleData.currentPrice();
            final boolean priceChanged = !currentPrice.equals(priceAtAddition);

            return new GetCartByIdResult.CartItemSummary(
                item.id().value().toString(),
                item.productId().value().toString(),
                item.quantity().value(),
                priceAtAddition.amount(),
                priceAtAddition.currency().getCurrencyCode(),
                currentPrice.amount(),
                currentPrice.currency().getCurrencyCode(),
                articleData.isAvailable(),
                priceChanged
            );
          } else {
            // Article data not available - return item with null current price
            return new GetCartByIdResult.CartItemSummary(
                item.id().value().toString(),
                item.productId().value().toString(),
                item.quantity().value(),
                priceAtAddition.amount(),
                priceAtAddition.currency().getCurrencyCode(),
                null,
                null,
                false,
                false
            );
          }
        })
        .toList();

    final Money total = cart.calculateTotal();

    return GetCartByIdResult.found(
        cart.id().value(),
        cart.customerId().value(),
        cart.status().name(),
        items,
        total.amount(),
        total.currency().getCurrencyCode()
    );
  }
}
