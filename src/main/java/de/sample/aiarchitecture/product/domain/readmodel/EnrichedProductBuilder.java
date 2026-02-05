package de.sample.aiarchitecture.product.domain.readmodel;

import de.sample.aiarchitecture.product.domain.model.EnrichedProduct;
import de.sample.aiarchitecture.product.domain.model.ProductArticle;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.ReadModelBuilder;
import org.jspecify.annotations.Nullable;

/**
 * Builder that constructs {@link EnrichedProduct} read models by implementing
 * {@link EnrichedProductStateInterest}.
 *
 * <p>This builder combines state from the {@link de.sample.aiarchitecture.product.domain.model.Product}
 * aggregate with external article data (pricing and stock). The aggregate calls the
 * {@code receive*()} methods from {@link de.sample.aiarchitecture.product.domain.model.ProductStateInterest}
 * to push its state, and then article data is provided via {@link #receiveArticleData}.
 * Finally, {@link #build()} is called to construct the immutable read model.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * EnrichedProductBuilder builder = new EnrichedProductBuilder();
 * product.provideStateTo(builder);
 * builder.receiveArticleData(new ProductArticle(price, stock, isAvailable));
 * EnrichedProduct enrichedProduct = builder.build();
 * }</pre>
 *
 * <p>The builder is reusable. Call {@link #reset()} to clear internal state and build another
 * read model from a different aggregate.
 *
 * @see EnrichedProductStateInterest
 * @see EnrichedProduct
 * @see ReadModelBuilder
 */
public class EnrichedProductBuilder implements EnrichedProductStateInterest, ReadModelBuilder {

  private @Nullable ProductId productId;
  private @Nullable String sku;
  private @Nullable String name;
  private @Nullable String description;
  private @Nullable String category;
  private @Nullable ProductArticle articleData;

  /**
   * Creates a new EnrichedProductBuilder.
   */
  public EnrichedProductBuilder() {
    // Default constructor
  }

  @Override
  public void receiveProductId(final ProductId productId) {
    this.productId = productId;
  }

  @Override
  public void receiveSku(final String sku) {
    this.sku = sku;
  }

  @Override
  public void receiveName(final String name) {
    this.name = name;
  }

  @Override
  public void receiveDescription(final String description) {
    this.description = description;
  }

  @Override
  public void receiveCategory(final String category) {
    this.category = category;
  }

  @Override
  public void receiveArticleData(final ProductArticle productArticle) {
    if (productArticle == null) {
      throw new IllegalArgumentException("Product article cannot be null");
    }
    this.articleData = productArticle;
  }

  /**
   * Builds the immutable {@link EnrichedProduct} from the received state.
   *
   * <p>All required state must have been received before calling this method,
   * including the article data from external contexts.
   *
   * @return the constructed EnrichedProduct
   * @throws IllegalStateException if required state has not been received
   */
  public EnrichedProduct build() {
    if (productId == null) {
      throw new IllegalStateException("Product ID has not been received");
    }
    if (sku == null) {
      throw new IllegalStateException("SKU has not been received");
    }
    if (name == null) {
      throw new IllegalStateException("Name has not been received");
    }
    if (category == null) {
      throw new IllegalStateException("Category has not been received");
    }
    if (articleData == null) {
      throw new IllegalStateException("Article data has not been received");
    }

    return EnrichedProduct.of(
        productId,
        sku,
        name,
        description != null ? description : "",
        category,
        articleData);
  }

  /**
   * Returns the product ID that was received.
   *
   * <p>Use this method after calling {@code provideStateTo()} to retrieve the product ID
   * for which article data should be fetched.
   *
   * @return the product ID, or null if not yet received
   */
  public @Nullable ProductId getProductId() {
    return productId;
  }

  /**
   * Resets the builder to its initial state.
   *
   * <p>Call this method to reuse the builder for constructing another read model.
   */
  public void reset() {
    productId = null;
    sku = null;
    name = null;
    description = null;
    category = null;
    articleData = null;
  }
}
