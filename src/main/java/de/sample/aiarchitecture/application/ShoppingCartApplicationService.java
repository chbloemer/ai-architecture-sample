package de.sample.aiarchitecture.application;

import de.sample.aiarchitecture.domain.model.cart.*;
import de.sample.aiarchitecture.domain.model.product.*;
import de.sample.aiarchitecture.domain.model.shared.Price;
import de.sample.aiarchitecture.domain.model.shared.ProductId;
import de.sample.aiarchitecture.infrastructure.api.DomainEventPublisher;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service for Shopping Cart operations.
 *
 * <p>Orchestrates shopping cart use cases and coordinates domain objects.
 * This service is thin and delegates business logic to the domain model.
 *
 * <p><b>Transactional Boundary:</b>
 * This service defines the transactional boundary for shopping cart operations. Each public method
 * represents a use case and runs within a transaction. Domain events are published after
 * successful transaction commit.
 *
 * <p><b>Domain Events:</b>
 * This service publishes domain events after successfully persisting aggregates,
 * enabling eventual consistency and loose coupling between bounded contexts.
 */
@Service
@Transactional
public class ShoppingCartApplicationService {

  private final ShoppingCartRepository shoppingCartRepository;
  private final ProductRepository productRepository;
  private final DomainEventPublisher eventPublisher;

  public ShoppingCartApplicationService(
      final ShoppingCartRepository shoppingCartRepository,
      final ProductRepository productRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.productRepository = productRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Creates a new shopping cart for a customer.
   *
   * @param customerId the customer ID
   * @return the created cart
   */
  public ShoppingCart createCart(@NonNull final CustomerId customerId) {
    final CartId cartId = CartId.generate();
    final ShoppingCart cart = new ShoppingCart(cartId, customerId);

    shoppingCartRepository.save(cart);

    return cart;
  }

  /**
   * Finds a cart by its ID.
   *
   * @param cartId the cart ID
   * @return the cart if found
   */
  @Transactional(readOnly = true)
  public Optional<ShoppingCart> findCartById(@NonNull final CartId cartId) {
    return shoppingCartRepository.findById(cartId);
  }

  /**
   * Finds the active cart for a customer, creating one if it doesn't exist.
   *
   * <p>Note: This method may write (create cart), so it's not marked as readOnly.
   *
   * @param customerId the customer ID
   * @return the active cart
   */
  public ShoppingCart getOrCreateActiveCart(@NonNull final CustomerId customerId) {
    return shoppingCartRepository
        .findActiveCartByCustomerId(customerId)
        .orElseGet(() -> createCart(customerId));
  }

  /**
   * Finds all carts for a customer.
   *
   * @param customerId the customer ID
   * @return list of carts
   */
  @Transactional(readOnly = true)
  public List<ShoppingCart> findCartsByCustomerId(@NonNull final CustomerId customerId) {
    return shoppingCartRepository.findByCustomerId(customerId);
  }

  /**
   * Adds an item to a cart.
   *
   * <p>Publishes a {@link de.sample.aiarchitecture.domain.model.cart.CartItemAddedToCart} event.
   *
   * @param cartId the cart ID
   * @param productId the product ID
   * @param quantity the quantity to add
   * @throws IllegalArgumentException if cart or product not found, or product unavailable
   */
  public void addItemToCart(
      @NonNull final CartId cartId,
      @NonNull final ProductId productId,
      @NonNull final Quantity quantity) {

    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId.value()));

    final Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId.value()));

    // Business rule: Check if product has sufficient stock
    if (!product.hasStockFor(quantity.value())) {
      throw new IllegalArgumentException(
          "Insufficient stock for product: " + productId.value());
    }

    // Add item to cart with current price
    cart.addItem(productId, quantity, product.price());

    shoppingCartRepository.save(cart);

    // Publish domain events
    eventPublisher.publishAndClearEvents(cart);
  }

  /**
   * Removes an item from a cart.
   *
   * @param cartId the cart ID
   * @param itemId the item ID
   * @throws IllegalArgumentException if cart or item not found
   */
  public void removeItemFromCart(@NonNull final CartId cartId, @NonNull final CartItemId itemId) {
    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId.value()));

    cart.removeItem(itemId);

    shoppingCartRepository.save(cart);
  }

  /**
   * Updates the quantity of an item in the cart.
   *
   * @param cartId the cart ID
   * @param itemId the item ID
   * @param newQuantity the new quantity
   * @throws IllegalArgumentException if cart or item not found
   */
  public void updateItemQuantity(
      @NonNull final CartId cartId,
      @NonNull final CartItemId itemId,
      @NonNull final Quantity newQuantity) {

    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId.value()));

    cart.updateItemQuantity(itemId, newQuantity);

    shoppingCartRepository.save(cart);
  }

  /**
   * Clears all items from a cart.
   *
   * @param cartId the cart ID
   * @throws IllegalArgumentException if cart not found
   */
  public void clearCart(@NonNull final CartId cartId) {
    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId.value()));

    cart.clear();

    shoppingCartRepository.save(cart);
  }

  /**
   * Checks out a cart.
   *
   * <p>Publishes a {@link de.sample.aiarchitecture.domain.model.cart.CartCheckedOut} event.
   *
   * @param cartId the cart ID
   * @return the checked out cart
   * @throws IllegalArgumentException if cart not found
   * @throws IllegalStateException if cart is empty or already checked out
   */
  public ShoppingCart checkout(@NonNull final CartId cartId) {
    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId.value()));

    cart.checkout();

    shoppingCartRepository.save(cart);

    // Publish domain events
    eventPublisher.publishAndClearEvents(cart);

    return cart;
  }

  /**
   * Deletes a cart.
   *
   * @param cartId the cart ID
   */
  public void deleteCart(@NonNull final CartId cartId) {
    shoppingCartRepository.deleteById(cartId);
  }
}
