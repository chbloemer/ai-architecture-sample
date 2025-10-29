package de.sample.aiarchitecture.application;

import org.jspecify.annotations.NonNull;

/**
 * Use Case for adding an item to a shopping cart.
 *
 * <p>This use case handles adding products to a customer's shopping cart.
 * It validates business rules like product availability and stock levels.
 *
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Cart must exist and be active</li>
 *   <li>Product must exist</li>
 *   <li>Product must have sufficient stock</li>
 *   <li>Quantity must be positive</li>
 * </ul>
 *
 * <p><b>Domain Events:</b>
 * Publishes {@link de.sample.aiarchitecture.domain.model.cart.CartItemAddedToCart} event.
 */
public interface AddItemToCartUseCase extends UseCase<AddItemToCartInput, AddItemToCartOutput> {

  /**
   * Adds an item to the shopping cart.
   *
   * @param input the item data to add
   * @return the updated cart details
   * @throws IllegalArgumentException if cart or product not found, or insufficient stock
   */
  @Override
  @NonNull AddItemToCartOutput execute(@NonNull AddItemToCartInput input);
}
