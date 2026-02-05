package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Factory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * Factory for assembling EnrichedCart instances from ShoppingCart and article data.
 *
 * <p>Encapsulates the complex assembly of enriched cart items by combining cart items
 * with their corresponding article data to create a fully hydrated EnrichedCart.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Validates that all cart items have corresponding article data</li>
 *   <li>Creates EnrichedCartItem instances by pairing cart items with articles</li>
 *   <li>Assembles the final EnrichedCart with all enriched items</li>
 * </ul>
 */
public final class EnrichedCartFactory implements Factory {

  /**
   * Creates an EnrichedCart from a ShoppingCart and article data.
   *
   * @param cart the shopping cart to enrich
   * @param articleData map of product IDs to their corresponding article data
   * @return a new EnrichedCart with enriched items
   * @throws IllegalArgumentException if article data is missing for any cart item
   */
  public EnrichedCart create(
      @NonNull final ShoppingCart cart,
      @NonNull final Map<ProductId, CartArticle> articleData) {

    validateArticleDataComplete(cart.items(), articleData);

    final List<EnrichedCartItem> enrichedItems = createEnrichedItems(cart.items(), articleData);

    return EnrichedCart.of(cart.id(), cart.customerId(), enrichedItems, cart.status());
  }

  private void validateArticleDataComplete(
      final List<CartItem> items,
      final Map<ProductId, CartArticle> articleData) {

    final List<ProductId> missingArticles = new ArrayList<>();

    for (final CartItem item : items) {
      if (!articleData.containsKey(item.productId())) {
        missingArticles.add(item.productId());
      }
    }

    if (!missingArticles.isEmpty()) {
      throw new IllegalArgumentException(
          "Missing article data for product IDs: " + missingArticles);
    }
  }

  private List<EnrichedCartItem> createEnrichedItems(
      final List<CartItem> items,
      final Map<ProductId, CartArticle> articleData) {

    final List<EnrichedCartItem> enrichedItems = new ArrayList<>();

    for (final CartItem item : items) {
      final CartArticle article = articleData.get(item.productId());
      enrichedItems.add(EnrichedCartItem.of(item, article));
    }

    return enrichedItems;
  }
}
