package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;

/**
 * Functional interface for resolving article information (name and price).
 *
 * <p>This interface allows the cart domain to receive product names and pricing
 * when exposing state through the Interest Interface pattern, without depending
 * on external infrastructure. Implementations can fetch data from product services,
 * caching layers, or other sources.
 *
 * @see ArticleInfo
 * @see CartStateInterest
 */
@FunctionalInterface
public interface ArticleInfoResolver {

  /**
   * Resolves the article information for a product.
   *
   * @param productId the product identifier to resolve information for
   * @return the article information including name and price
   */
  ArticleInfo resolve(ProductId productId);
}
