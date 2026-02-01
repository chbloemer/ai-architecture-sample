package de.sample.aiarchitecture.cart.adapter.outgoing.persistence;

import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.cart.domain.model.CartStatus;
import de.sample.aiarchitecture.infrastructure.annotation.AsyncInitialize;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of ShoppingCartRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for shopping carts using
 * ConcurrentHashMap. In a production system, this would be replaced with a database implementation.
 *
 * <p><b>Async Initialization:</b> This repository uses {@link AsyncInitialize} to perform
 * non-blocking cache warmup. The {@code asyncInitialize()} method is invoked asynchronously after
 * bean initialization, allowing the application to start without waiting for initialization tasks.
 *
 * @see AsyncInitialize
 */
@org.springframework.context.annotation.Profile("inmemory")
@Repository
@AsyncInitialize(priority = 100, description = "Initialize shopping cart metrics")
public class InMemoryShoppingCartRepository implements ShoppingCartRepository {

  private static final Logger logger =
      LoggerFactory.getLogger(InMemoryShoppingCartRepository.class);

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

  /**
   * Asynchronous initialization method triggered by {@link AsyncInitialize}.
   *
   * <p>This method is invoked after bean construction to initialize shopping cart metrics and
   * perform background setup tasks. The {@code @Async} annotation ensures non-blocking execution,
   * allowing the application to start immediately.
   *
   * <p><b>Pattern:</b> This demonstrates the {@code @AsyncInitialize} pattern with lower priority
   * (100) than ProductRepository (50), ensuring products are initialized first.
   *
   * @see AsyncInitialize
   * @see de.sample.aiarchitecture.infrastructure.config.AsyncConfiguration
   * @see de.sample.aiarchitecture.infrastructure.config.AsyncInitializationProcessor
   */
  @Async
  public void asyncInitialize() {
    logger.info("Starting async initialization of ShoppingCartRepository...");

    try {
      // Simulate initialization delay
      Thread.sleep(1500);

      // In a real application, this would:
      // - Initialize metrics collectors
      // - Preload abandoned cart data
      // - Set up monitoring dashboards
      int cartCount = carts.size();

      logger.info(
          "ShoppingCartRepository initialization completed. Current cart count: {}", cartCount);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("ShoppingCartRepository async initialization interrupted", e);
    } catch (Exception e) {
      logger.error("Error during ShoppingCartRepository async initialization", e);
    }
  }
}
