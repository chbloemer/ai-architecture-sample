package de.sample.aiarchitecture.cart.domain.readmodel;

import de.sample.aiarchitecture.cart.domain.model.CartArticle;
import de.sample.aiarchitecture.cart.domain.model.CartStateInterest;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;

/**
 * Extended interest interface for building enriched cart read models.
 *
 * <p>This interface extends {@link CartStateInterest} to add the ability to receive
 * current article data from external services. Builders implementing this interface
 * can combine snapshot state from the aggregate with fresh article data to build
 * enriched read models.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * EnrichedCartBuilder builder = new EnrichedCartBuilder();
 * shoppingCart.provideStateTo(builder, articleInfoResolver);
 * builder.receiveCurrentArticleData(productId, cartArticle);
 * EnrichedCart enrichedCart = builder.build();
 * }</pre>
 *
 * @see CartStateInterest
 * @see EnrichedCartBuilder
 */
public interface EnrichedCartStateInterest extends CartStateInterest {

  /**
   * Receives current article data from external services.
   *
   * <p>This method is called to provide current pricing, availability, and stock
   * information for a product in the cart. The data can be used to enrich the
   * read model with up-to-date article information.
   *
   * @param productId the product identifier
   * @param cartArticle the current article data including price, stock, and availability
   */
  void receiveCurrentArticleData(ProductId productId, CartArticle cartArticle);
}
