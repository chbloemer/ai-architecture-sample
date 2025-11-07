package de.sample.aiarchitecture.cart.adapter.outgoing.persistence;

import de.sample.aiarchitecture.cart.application.port.out.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.cart.domain.model.CartStatus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of ShoppingCartRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for shopping carts
 * using ConcurrentHashMap. In a production system, this would be replaced with
 * a database implementation.
 */
@Repository
public class InMemoryShoppingCartRepository implements ShoppingCartRepository {

  private final ConcurrentHashMap<CartId, ShoppingCart> carts = new ConcurrentHashMap<>();

  @Override
  public Optional<ShoppingCart> findById(@NonNull final CartId id) {
    return Optional.ofNullable(carts.get(id));
  }

  @Override
  public List<ShoppingCart> findByCustomerId(@NonNull final CustomerId customerId) {
    return carts.values().stream()
        .filter(cart -> cart.customerId().equals(customerId))
        .toList();
  }

  @Override
  public Optional<ShoppingCart> findActiveCartByCustomerId(@NonNull final CustomerId customerId) {
    return carts.values().stream()
        .filter(cart -> cart.customerId().equals(customerId))
        .filter(cart -> cart.status() == CartStatus.ACTIVE)
        .findFirst();
  }

  @Override
  public List<ShoppingCart> findAll() {
    return List.copyOf(carts.values());
  }

  @Override
  public ShoppingCart save(@NonNull final ShoppingCart cart) {
    carts.put(cart.id(), cart);
    return cart;
  }

  @Override
  public void deleteById(@NonNull final CartId id) {
    carts.remove(id);
  }
}
