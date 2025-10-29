package de.sample.aiarchitecture.application;

import org.jspecify.annotations.NonNull;

/**
 * Use Case for creating a new product.
 *
 * <p>This use case represents the "Create Product" feature in the Product Catalog.
 * It validates business rules, creates the product aggregate, and publishes domain events.
 *
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>SKU must be unique</li>
 *   <li>Price must be positive</li>
 *   <li>Stock cannot be negative</li>
 * </ul>
 *
 * <p><b>Domain Events:</b>
 * Publishes {@link de.sample.aiarchitecture.domain.model.product.ProductCreated} event.
 */
public interface CreateProductUseCase extends UseCase<CreateProductInput, CreateProductOutput> {

  /**
   * Creates a new product.
   *
   * @param input the product creation data
   * @return the created product details
   * @throws IllegalArgumentException if SKU already exists or validation fails
   */
  @Override
  @NonNull CreateProductOutput execute(@NonNull CreateProductInput input);
}
