package de.sample.aiarchitecture.domain.model.product;

import de.sample.aiarchitecture.domain.model.shared.ddd.Specification;
import org.jspecify.annotations.NonNull;

/**
 * Specification to check if a product is available for purchase.
 *
 * <p>A product is available if it has stock quantity greater than zero.
 */
public final class ProductAvailabilitySpecification implements Specification {

  /**
   * Checks if the product satisfies the availability criteria.
   *
   * @param product the product to check
   * @return true if product is available for purchase
   */
  public boolean isSatisfiedBy(@NonNull final Product product) {
    return product.isAvailable();
  }

  /**
   * Checks if the product has sufficient stock for the requested quantity.
   *
   * @param product the product to check
   * @param requestedQuantity the quantity requested
   * @return true if product has enough stock
   */
  public boolean isSatisfiedBy(@NonNull final Product product, final int requestedQuantity) {
    return product.hasStockFor(requestedQuantity);
  }
}
