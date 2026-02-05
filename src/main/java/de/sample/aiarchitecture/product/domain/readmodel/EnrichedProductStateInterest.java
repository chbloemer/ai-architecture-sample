package de.sample.aiarchitecture.product.domain.readmodel;

import de.sample.aiarchitecture.product.domain.model.ProductArticle;
import de.sample.aiarchitecture.product.domain.model.ProductStateInterest;

/**
 * Extended interest interface for building enriched product read models.
 *
 * <p>This interface extends {@link ProductStateInterest} to add the ability to receive
 * external article data (pricing and stock) from the Pricing and Inventory contexts.
 * Builders implementing this interface can combine aggregate state with fresh external
 * data to build enriched read models.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * EnrichedProductBuilder builder = new EnrichedProductBuilder();
 * product.provideStateTo(builder);
 * builder.receiveArticleData(productArticle);
 * EnrichedProduct enrichedProduct = builder.build();
 * }</pre>
 *
 * @see ProductStateInterest
 * @see EnrichedProductBuilder
 */
public interface EnrichedProductStateInterest extends ProductStateInterest {

  /**
   * Receives external article data (pricing and stock) from other contexts.
   *
   * <p>This method is called to provide current pricing from the Pricing context
   * and stock/availability information from the Inventory context.
   *
   * @param productArticle the current article data including price, stock, and availability
   */
  void receiveArticleData(ProductArticle productArticle);
}
