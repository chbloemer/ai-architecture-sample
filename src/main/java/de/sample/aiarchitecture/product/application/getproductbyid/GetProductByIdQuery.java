package de.sample.aiarchitecture.product.application.getproductbyid;

import org.jspecify.annotations.NonNull;

/**
 * Input model for retrieving a product by ID.
 *
 * @param productId the product ID
 */
public record GetProductByIdQuery(@NonNull String productId) {

  /**
   * Compact constructor with validation.
   */
  public GetProductByIdQuery {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
  }
}
