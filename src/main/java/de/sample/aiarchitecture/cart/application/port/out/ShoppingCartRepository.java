package de.sample.aiarchitecture.cart.application.port.out;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.marker.Repository;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

/**
 * Repository interface for ShoppingCart aggregate.
 *
 * <p>Provides collection-like access to ShoppingCart aggregates using domain language.
 * Implementation resides in the secondary adapter layer.
 *
 * <p>Extends the base {@link Repository} interface which provides common methods:
 * <ul>
 *   <li>{@code findById(CartId)} - inherited from base interface
 *   <li>{@code save(ShoppingCart)} - inherited from base interface
 *   <li>{@code deleteById(CartId)} - inherited from base interface
 * </ul>
 */
public interface ShoppingCartRepository extends Repository<ShoppingCart, CartId> {

  /**
   * Finds all carts for a specific customer.
   *
   * @param customerId the customer ID
   * @return list of carts belonging to the customer
   */
  List<ShoppingCart> findByCustomerId(@NonNull CustomerId customerId);

  /**
   * Finds the active cart for a customer.
   *
   * @param customerId the customer ID
   * @return the active cart if found, empty otherwise
   */
  Optional<ShoppingCart> findActiveCartByCustomerId(@NonNull CustomerId customerId);

  /**
   * Retrieves all shopping carts.
   *
   * @return list of all carts
   */
  List<ShoppingCart> findAll();
}
