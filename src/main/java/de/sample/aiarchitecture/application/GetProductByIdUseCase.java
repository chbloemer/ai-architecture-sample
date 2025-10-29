package de.sample.aiarchitecture.application;

import org.jspecify.annotations.NonNull;

/**
 * Use Case for retrieving a product by its ID.
 *
 * <p>This is a query use case (read-only) that retrieves product details
 * without modifying state.
 *
 * <p><b>CQRS Note:</b>
 * In a full CQRS implementation, query use cases might read from a separate
 * read model. For this sample, we use the same domain model for simplicity.
 */
public interface GetProductByIdUseCase extends UseCase<GetProductByIdInput, GetProductByIdOutput> {

  /**
   * Retrieves a product by its ID.
   *
   * @param input the product ID
   * @return the product details, or empty output if not found
   */
  @Override
  @NonNull GetProductByIdOutput execute(@NonNull GetProductByIdInput input);
}
