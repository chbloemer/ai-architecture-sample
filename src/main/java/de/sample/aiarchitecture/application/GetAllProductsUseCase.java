package de.sample.aiarchitecture.application;

import org.jspecify.annotations.NonNull;

/**
 * Use Case for retrieving all products.
 *
 * <p>This is a query use case (read-only) that retrieves all products
 * in the catalog without modifying state.
 *
 * <p><b>Note:</b>
 * In production systems, this should support pagination to handle large datasets.
 */
public interface GetAllProductsUseCase extends UseCase<GetAllProductsInput, GetAllProductsOutput> {

  /**
   * Retrieves all products in the catalog.
   *
   * @param input the query parameters (currently empty, could add filters/pagination)
   * @return the list of products
   */
  @Override
  @NonNull GetAllProductsOutput execute(@NonNull GetAllProductsInput input);
}
