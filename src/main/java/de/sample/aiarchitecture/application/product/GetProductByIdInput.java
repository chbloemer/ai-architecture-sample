package de.sample.aiarchitecture.application.product;

import org.jspecify.annotations.NonNull;

/**
 * Input model for retrieving a product by ID.
 *
 * @param productId the product ID
 */
public record GetProductByIdInput(@NonNull String productId) {

  /**
   * Compact constructor with validation.
   */
  public GetProductByIdInput {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
  }
}
