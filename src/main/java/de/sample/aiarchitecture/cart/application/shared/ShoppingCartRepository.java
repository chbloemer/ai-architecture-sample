package de.sample.aiarchitecture.cart.application.shared;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.Repository;
import de.sample.aiarchitecture.sharedkernel.domain.specification.CompositeSpecification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
  List<ShoppingCart> findByCustomerId(CustomerId customerId);

  /**
   * Finds the active cart for a customer.
   *
   * @param customerId the customer ID
   * @return the active cart if found, empty otherwise
   */
  Optional<ShoppingCart> findActiveCartByCustomerId(CustomerId customerId);

  /**
   * Retrieves all shopping carts.
   *
   * @return list of all carts
   */
  List<ShoppingCart> findAll();

  /**
   * Find carts matching the given specification using database-side filtering and pagination.
   *
   * <p>The specification is expressed in domain terms and translated by the persistence adapter
   * into native predicates (e.g., JPA criteria) to avoid loading and filtering in memory.
   *
   * <p>Default implementation falls back to in-memory filtering and manual paging, so secondary
   * adapters can opt-in to DB pushdown progressively.
   */
  default Page<ShoppingCart> findBy(CompositeSpecification<ShoppingCart> specification, Pageable pageable) {
    final List<ShoppingCart> filtered = findAll().stream()
        .filter(specification::isSatisfiedBy)
        .toList();
    final int start = (int) pageable.getOffset();
    final int end = Math.min(start + pageable.getPageSize(), filtered.size());
    final List<ShoppingCart> content = start >= filtered.size() ? List.of() : filtered.subList(start, end);
    return new PageImpl<>(content, pageable, filtered.size());
  }
}
