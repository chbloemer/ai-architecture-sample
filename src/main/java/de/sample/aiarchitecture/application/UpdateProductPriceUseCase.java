package de.sample.aiarchitecture.application;

import org.jspecify.annotations.NonNull;

/**
 * Use Case for updating a product's price.
 *
 * <p>This use case handles price changes for products in the catalog.
 * Price changes are important business events and are tracked via domain events.
 *
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Product must exist</li>
 *   <li>New price must be positive</li>
 * </ul>
 *
 * <p><b>Domain Events:</b>
 * Publishes {@link de.sample.aiarchitecture.domain.model.product.ProductPriceChanged} event.
 */
public interface UpdateProductPriceUseCase extends UseCase<UpdateProductPriceInput, UpdateProductPriceOutput> {

  /**
   * Updates a product's price.
   *
   * @param input the price update data
   * @return the updated product details
   * @throws IllegalArgumentException if product not found or price invalid
   */
  @Override
  @NonNull UpdateProductPriceOutput execute(@NonNull UpdateProductPriceInput input);
}
