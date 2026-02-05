package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.cart.domain.event.CartCheckedOut;
import de.sample.aiarchitecture.cart.domain.event.CartCleared;
import de.sample.aiarchitecture.cart.domain.event.CartItemAddedToCart;
import de.sample.aiarchitecture.cart.domain.event.CartItemQuantityChanged;
import de.sample.aiarchitecture.cart.domain.event.ProductRemovedFromCart;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.BaseAggregateRoot;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.Price;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * ShoppingCart Aggregate Root.
 *
 * <p>Represents a customer's shopping cart containing items they intend to purchase.
 * This aggregate enforces cart rules and ensures consistency of all cart operations.
 *
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Cannot add items to a checked-out cart
 *   <li>Cannot modify items in a checked-out cart
 *   <li>Quantity must always be positive
 *   <li>Each product can appear only once in the cart (quantities are combined)
 * </ul>
 *
 * <p><b>Domain Events:</b>
 * <ul>
 *   <li>{@link CartItemAddedToCart} - when an item is added to the cart
 *   <li>{@link ProductRemovedFromCart} - when a product is removed from the cart
 *   <li>{@link CartItemQuantityChanged} - when an item's quantity is updated
 *   <li>{@link CartCleared} - when all items are removed from the cart
 *   <li>{@link CartCheckedOut} - when the cart is checked out
 * </ul>
 */
public final class ShoppingCart extends BaseAggregateRoot<ShoppingCart, CartId> {

  private final CartId id;
  private final CustomerId customerId;
  private final List<CartItem> items;
  private CartStatus status;

  public ShoppingCart(final CartId id, final CustomerId customerId) {
    this.id = id;
    this.customerId = customerId;
    this.items = new ArrayList<>();
    this.status = CartStatus.ACTIVE;
  }

  @Override
  public CartId id() {
    return id;
  }

  public CustomerId customerId() {
    return customerId;
  }

  public List<CartItem> items() {
    return Collections.unmodifiableList(items);
  }

  public CartStatus status() {
    return status;
  }

  /**
   * Adds an item to the cart or increases quantity if product already exists.
   *
   * <p>Raises a {@link CartItemAddedToCart} domain event.
   *
   * @param productId the product to add
   * @param quantity the quantity to add
   * @param price the current price of the product
   * @throws IllegalStateException if cart is checked out
   */
  public void addItem(
      final ProductId productId,
      final Quantity quantity,
      final Price price) {
    ensureCartIsActive();

    // Check if product already in cart
    final Optional<CartItem> existingItem = findItemByProductId(productId);

    if (existingItem.isPresent()) {
      // Increase quantity of existing item
      final CartItem item = existingItem.get();
      item.updateQuantity(Quantity.of(item.quantity().value() + quantity.value()));
    } else {
      // Add new item
      final CartItem newItem = new CartItem(CartItemId.generate(), productId, quantity, price);
      items.add(newItem);
    }

    // Raise domain event
    registerEvent(CartItemAddedToCart.now(this.id, productId, quantity));
  }

  /**
   * Removes an item from the cart.
   *
   * @param itemId the ID of the item to remove
   * @throws IllegalStateException if cart is checked out
   * @throws IllegalArgumentException if item not found
   */
  public void removeItem(final CartItemId itemId) {
    ensureCartIsActive();

    final boolean removed = items.removeIf(item -> item.id().equals(itemId));
    if (!removed) {
      throw new IllegalArgumentException("Cart item not found: " + itemId.value());
    }
  }

  /**
   * Removes an item by product ID.
   *
   * <p>Raises a {@link ProductRemovedFromCart} domain event.
   *
   * @param productId the product ID
   * @throws IllegalStateException if cart is checked out
   */
  public void removeItemByProductId(final ProductId productId) {
    ensureCartIsActive();

    final boolean removed = items.removeIf(item -> item.productId().equals(productId));
    if (!removed) {
      throw new IllegalArgumentException("Product not found in cart: " + productId.value());
    }

    // Raise domain event
    registerEvent(ProductRemovedFromCart.now(this.id, productId));
  }

  /**
   * Updates the quantity of a cart item.
   *
   * <p>Raises a {@link CartItemQuantityChanged} domain event.
   *
   * @param itemId the item ID
   * @param newQuantity the new quantity
   * @throws IllegalStateException if cart is checked out
   * @throws IllegalArgumentException if item not found
   */
  public void updateItemQuantity(
      final CartItemId itemId, final Quantity newQuantity) {
    ensureCartIsActive();

    final CartItem item =
        findItemById(itemId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cart item not found: " + itemId.value()));

    final Quantity oldQuantity = item.quantity();
    item.updateQuantity(newQuantity);

    // Raise domain event
    registerEvent(CartItemQuantityChanged.now(this.id, itemId, item.productId(), oldQuantity, newQuantity));
  }

  /**
   * Increases the quantity of a cart item by 1.
   *
   * @param itemId the item ID
   * @throws IllegalStateException if cart is checked out
   */
  public void increaseItemQuantity(final CartItemId itemId) {
    ensureCartIsActive();

    final CartItem item =
        findItemById(itemId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cart item not found: " + itemId.value()));

    item.increaseQuantity();
  }

  /**
   * Decreases the quantity of a cart item by 1.
   *
   * @param itemId the item ID
   * @throws IllegalStateException if cart is checked out
   */
  public void decreaseItemQuantity(final CartItemId itemId) {
    ensureCartIsActive();

    final CartItem item =
        findItemById(itemId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cart item not found: " + itemId.value()));

    item.decreaseQuantity();
  }

  /**
   * Clears all items from the cart.
   *
   * <p>Raises a {@link CartCleared} domain event.
   *
   * @throws IllegalStateException if cart is checked out
   */
  public void clear() {
    ensureCartIsActive();
    final int itemsCount = items.size();
    items.clear();

    // Raise domain event
    registerEvent(CartCleared.now(this.id, itemsCount));
  }

  /**
   * Checks out the cart, preventing further modifications.
   *
   * <p>Raises a {@link CartCheckedOut} domain event.
   *
   * @throws IllegalStateException if cart is already checked out or empty
   */
  public void checkout() {
    if (status == CartStatus.CHECKED_OUT) {
      throw new IllegalStateException("Cart is already checked out");
    }
    if (items.isEmpty()) {
      throw new IllegalStateException("Cannot checkout an empty cart");
    }

    final Money totalAmount = calculateTotal();
    final int count = itemCount();

    this.status = CartStatus.CHECKED_OUT;

    // Raise domain event with cart items for cross-context integration
    registerEvent(CartCheckedOut.now(this.id, this.customerId, totalAmount, count, this.items));
  }

  /**
   * Marks the cart as abandoned.
   */
  public void abandon() {
    this.status = CartStatus.ABANDONED;
  }

  /**
   * Marks the cart as completed after checkout confirmation.
   *
   * <p>This method is called when the checkout process has been fully confirmed
   * (customer has completed payment/review steps). The cart can be completed
   * from either ACTIVE or CHECKED_OUT status.
   *
   * @throws IllegalStateException if cart is already completed or abandoned
   */
  public void complete() {
    if (status == CartStatus.COMPLETED) {
      throw new IllegalStateException("Cart is already completed");
    }
    if (status == CartStatus.ABANDONED) {
      throw new IllegalStateException("Cannot complete an abandoned cart");
    }
    this.status = CartStatus.COMPLETED;
  }

  /**
   * Calculates the total value of all items in the cart using stored prices.
   *
   * @return the total money value
   * @deprecated Use {@link #calculateTotal(ArticlePriceResolver)} for fresh pricing data
   */
  @Deprecated
  public Money calculateTotal() {
    if (items.isEmpty()) {
      return Money.euro(0.0);
    }

    Money total = Money.euro(0.0);
    for (final CartItem item : items) {
      final Money itemTotal =
          item.priceAtAddition().value().multiply(item.quantity().value());
      total = total.add(itemTotal);
    }
    return total;
  }

  /**
   * Calculates the total value of all items in the cart using fresh pricing from the resolver.
   *
   * <p>This method iterates through all cart items and uses the resolver to fetch
   * current pricing for each product, ensuring accurate totals at checkout time.
   *
   * @param priceResolver the resolver to fetch current pricing
   * @return the total money value based on current prices
   */
  public Money calculateTotal(final ArticlePriceResolver priceResolver) {
    if (priceResolver == null) {
      throw new IllegalArgumentException("Price resolver cannot be null");
    }
    if (items.isEmpty()) {
      return Money.euro(0.0);
    }

    Money total = Money.euro(0.0);
    for (final CartItem item : items) {
      final ArticlePrice articlePrice = priceResolver.resolve(item.productId());
      final Money itemTotal = articlePrice.price().multiply(item.quantity().value());
      total = total.add(itemTotal);
    }
    return total;
  }

  /**
   * Validates the cart for checkout using fresh pricing and availability data.
   *
   * <p>This method checks each item in the cart against current availability
   * and stock levels to ensure the cart can proceed to checkout.
   *
   * @param priceResolver the resolver to fetch current pricing and availability
   * @return a CartValidationResult containing any validation errors
   */
  public CartValidationResult validateForCheckout(final ArticlePriceResolver priceResolver) {
    if (priceResolver == null) {
      throw new IllegalArgumentException("Price resolver cannot be null");
    }
    if (items.isEmpty()) {
      return CartValidationResult.valid();
    }

    final List<CartValidationResult.ValidationError> errors = new ArrayList<>();
    for (final CartItem item : items) {
      final ArticlePrice articlePrice = priceResolver.resolve(item.productId());

      if (!articlePrice.isAvailable()) {
        errors.add(CartValidationResult.ValidationError.productUnavailable(item.productId()));
      } else if (articlePrice.availableStock() < item.quantity().value()) {
        errors.add(CartValidationResult.ValidationError.insufficientStock(
            item.productId(),
            item.quantity().value(),
            articlePrice.availableStock()));
      }
    }

    return errors.isEmpty()
        ? CartValidationResult.valid()
        : CartValidationResult.withErrors(errors);
  }

  /**
   * Gets the total number of items in the cart.
   *
   * @return the count of items
   */
  public int itemCount() {
    return items.size();
  }

  /**
   * Gets the total quantity of all items in the cart.
   *
   * @return the total quantity
   */
  public int totalQuantity() {
    return items.stream().mapToInt(item -> item.quantity().value()).sum();
  }

  /**
   * Checks if the cart is empty.
   *
   * @return true if cart has no items
   */
  public boolean isEmpty() {
    return items.isEmpty();
  }

  /**
   * Checks if the cart is active and can be modified.
   *
   * @return true if cart status is ACTIVE
   */
  public boolean isActive() {
    return status == CartStatus.ACTIVE;
  }

  /**
   * Checks if the cart contains a specific product.
   *
   * @param productId the product ID
   * @return true if product is in cart
   */
  public boolean containsProduct(final ProductId productId) {
    return items.stream().anyMatch(item -> item.productId().equals(productId));
  }

  /**
   * Merges items from another cart into this cart.
   *
   * <p>For each item in the source cart:
   * <ul>
   *   <li>If the product already exists in this cart, quantities are combined</li>
   *   <li>If the product doesn't exist, a new item is added with the source item's price</li>
   * </ul>
   *
   * <p>This method does not modify the source cart.
   *
   * @param sourceCart the cart to merge items from
   * @return the number of items merged (added or quantity increased)
   * @throws IllegalStateException if this cart is not active
   * @throws IllegalArgumentException if sourceCart is null
   */
  public int merge(final ShoppingCart sourceCart) {
    if (sourceCart == null) {
      throw new IllegalArgumentException("Source cart cannot be null");
    }
    ensureCartIsActive();

    int mergedCount = 0;
    for (final CartItem sourceItem : sourceCart.items()) {
      addItem(sourceItem.productId(), sourceItem.quantity(), sourceItem.priceAtAddition());
      mergedCount++;
    }

    return mergedCount;
  }

  private Optional<CartItem> findItemById(final CartItemId itemId) {
    return items.stream().filter(item -> item.id().equals(itemId)).findFirst();
  }

  private Optional<CartItem> findItemByProductId(final ProductId productId) {
    return items.stream().filter(item -> item.productId().equals(productId)).findFirst();
  }

  private void ensureCartIsActive() {
    if (status != CartStatus.ACTIVE) {
      throw new IllegalStateException("Cannot modify cart with status: " + status);
    }
  }

  /**
   * Pushes cart state to an interested party through the Interest Interface pattern.
   *
   * <p>This method allows the aggregate to control exactly what state is exposed,
   * following the "Tell, Don't Ask" principle. The aggregate pushes its state to
   * the interest, rather than exposing internal structure through getters.
   *
   * <p>The resolver is used to fetch current product information (name and price)
   * since CartItem only stores the productId and price at the time of addition.
   *
   * @param interest the interest to receive the cart state
   * @param resolver the resolver to fetch current product information
   * @throws IllegalArgumentException if interest or resolver is null
   * @see CartStateInterest
   * @see ArticleInfoResolver
   */
  public void provideStateTo(final CartStateInterest interest, final ArticleInfoResolver resolver) {
    if (interest == null) {
      throw new IllegalArgumentException("Interest cannot be null");
    }
    if (resolver == null) {
      throw new IllegalArgumentException("Resolver cannot be null");
    }

    // Push identifiers
    interest.receiveCartId(this.id);
    interest.receiveCustomerId(this.customerId);

    // Push all line items with resolved product information
    for (final CartItem item : this.items) {
      final ArticleInfo articleInfo = resolver.resolve(item.productId());
      interest.receiveLineItem(
          item.id(),
          item.productId(),
          articleInfo.name(),
          articleInfo.price(),
          item.quantity().value());
    }

    // Push calculated values
    interest.receiveItemCount(this.itemCount());
    interest.receiveSubtotal(this.calculateTotal());
  }
}
