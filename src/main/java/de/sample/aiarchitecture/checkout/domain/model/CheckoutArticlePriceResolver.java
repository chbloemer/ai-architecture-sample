package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Functional interface for resolving current pricing and availability information for articles
 * during checkout operations. This resolver provides a domain-level abstraction for obtaining
 * fresh pricing data that may come from external systems.
 */
@FunctionalInterface
public interface CheckoutArticlePriceResolver {

  /**
   * Resolves the current price and availability for an article.
   *
   * @param productId the product identifier to resolve pricing for
   * @return the article price information including availability status
   */
  ArticlePrice resolve(ProductId productId);

  /**
   * Value object containing pricing and availability information for an article.
   *
   * @param price the current price of the article
   * @param isAvailable whether the article is currently available for purchase
   * @param availableStock the quantity currently available in stock
   */
  record ArticlePrice(Money price, boolean isAvailable, int availableStock)
      implements Value {

    public ArticlePrice {
      if (availableStock < 0) {
        throw new IllegalArgumentException("Available stock cannot be negative");
      }
    }
  }
}
