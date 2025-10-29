package de.sample.aiarchitecture.application;

import org.jspecify.annotations.NonNull;

/**
 * Use Case for checking out a shopping cart.
 *
 * <p>This use case completes the shopping process by checking out the cart.
 * It transitions the cart from ACTIVE to CHECKED_OUT status.
 *
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Cart must exist</li>
 *   <li>Cart must be active (not already checked out)</li>
 *   <li>Cart cannot be empty</li>
 * </ul>
 *
 * <p><b>Domain Events:</b>
 * Publishes {@link de.sample.aiarchitecture.domain.model.cart.CartCheckedOut} event.
 */
public interface CheckoutCartUseCase extends UseCase<CheckoutCartInput, CheckoutCartOutput> {

  /**
   * Checks out the shopping cart.
   *
   * @param input the checkout data
   * @return the checkout result
   * @throws IllegalArgumentException if cart not found
   * @throws IllegalStateException if cart is empty or already checked out
   */
  @Override
  @NonNull CheckoutCartOutput execute(@NonNull CheckoutCartInput input);
}
