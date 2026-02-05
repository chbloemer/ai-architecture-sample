package de.sample.aiarchitecture.product.application.getproductbyid;

import de.sample.aiarchitecture.product.domain.model.EnrichedProduct;
import org.jspecify.annotations.Nullable;

/**
 * Output model for product retrieval by ID.
 *
 * <p>This result wraps an {@link EnrichedProduct} domain read model that combines
 * product state from the Product aggregate with external data from the Pricing and
 * Inventory bounded contexts.
 *
 * <p>Check {@link #found()} to determine if the product exists before accessing
 * the enriched product.
 *
 * <p><b>Pattern:</b> Use Case → Result(EnrichedProduct) → Controller → ViewModel → Template
 *
 * @param found whether the product was found
 * @param product the enriched product (null if not found)
 */
public record GetProductByIdResult(
    boolean found,
    @Nullable EnrichedProduct product
) {

  /**
   * Creates a result for a product that was not found.
   *
   * @return a result indicating the product was not found
   */
  public static GetProductByIdResult notFound() {
    return new GetProductByIdResult(false, null);
  }

  /**
   * Creates a result for a product that was found.
   *
   * @param product the enriched product
   * @return a result containing the enriched product
   */
  public static GetProductByIdResult found(final EnrichedProduct product) {
    if (product == null) {
      throw new IllegalArgumentException("Product cannot be null when found");
    }
    return new GetProductByIdResult(true, product);
  }
}
