package de.sample.aiarchitecture.application;

import org.jspecify.annotations.NonNull;

/**
 * Use Case for retrieving a shopping cart by its ID.
 *
 * <p>This is a query use case (read-only) that retrieves cart details
 * without modifying state.
 */
public interface GetCartByIdUseCase extends UseCase<GetCartByIdInput, GetCartByIdOutput> {

  /**
   * Retrieves a shopping cart by its ID.
   *
   * @param input the cart ID
   * @return the cart details, or empty output if not found
   */
  @Override
  @NonNull GetCartByIdOutput execute(@NonNull GetCartByIdInput input);
}
