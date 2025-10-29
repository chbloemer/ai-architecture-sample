package de.sample.aiarchitecture.application;

import org.jspecify.annotations.NonNull;

/**
 * Use Case for creating a new shopping cart.
 *
 * <p>This use case creates a new empty shopping cart for a customer.
 * Each customer can have multiple carts, but typically only one active cart at a time.
 *
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Customer ID must be provided</li>
 *   <li>Cart starts in ACTIVE status</li>
 *   <li>Cart starts empty (no items)</li>
 * </ul>
 */
public interface CreateCartUseCase extends UseCase<CreateCartInput, CreateCartOutput> {

  /**
   * Creates a new shopping cart.
   *
   * @param input the cart creation data
   * @return the created cart details
   */
  @Override
  @NonNull CreateCartOutput execute(@NonNull CreateCartInput input);
}
