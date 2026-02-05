package de.sample.aiarchitecture.product.application.getallproducts;

import de.sample.aiarchitecture.product.domain.model.EnrichedProduct;
import java.util.List;

/**
 * Output model for retrieving all products.
 *
 * <p>This result wraps a list of {@link EnrichedProduct} domain read models that combine
 * product state from the Product aggregate with external data from the Pricing and
 * Inventory bounded contexts.
 *
 * <p><b>Pattern:</b> Use Case → Result(EnrichedProduct list) → Controller → ViewModel → Template
 *
 * @param products the list of enriched products
 */
public record GetAllProductsResult(List<EnrichedProduct> products) {

  public GetAllProductsResult {
    if (products == null) {
      throw new IllegalArgumentException("Products list cannot be null");
    }
    // Make defensive copy
    products = List.copyOf(products);
  }
}
