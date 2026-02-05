package de.sample.aiarchitecture.cart.domain.readmodel;

import de.sample.aiarchitecture.cart.domain.model.CartArticle;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CartItemId;
import de.sample.aiarchitecture.cart.domain.model.CartStatus;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.cart.domain.model.EnrichedCart;
import de.sample.aiarchitecture.cart.domain.model.EnrichedCartItem;
import de.sample.aiarchitecture.cart.domain.model.Quantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.Price;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.ReadModelBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Builder that constructs {@link EnrichedCart} read models by implementing
 * {@link EnrichedCartStateInterest}.
 *
 * <p>This builder combines snapshot state from the {@link de.sample.aiarchitecture.cart.domain.model.ShoppingCart}
 * aggregate with current article data from external services. The aggregate calls the
 * {@code receive*()} methods from {@link de.sample.aiarchitecture.cart.domain.model.CartStateInterest}
 * to push its state, and then current article data is provided via {@link #receiveCurrentArticleData}.
 * Finally, {@link #build()} is called to construct the immutable read model.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * EnrichedCartBuilder builder = new EnrichedCartBuilder();
 * shoppingCart.provideStateTo(builder, articleInfoResolver);
 * // Provide current article data for each product
 * for (ProductId productId : productIds) {
 *     builder.receiveCurrentArticleData(productId, fetchCurrentArticle(productId));
 * }
 * EnrichedCart enrichedCart = builder.build();
 * }</pre>
 *
 * <p>The builder is reusable. Call {@link #reset()} to clear internal state and build another
 * read model from a different aggregate.
 *
 * @see EnrichedCartStateInterest
 * @see EnrichedCart
 * @see ReadModelBuilder
 */
public class EnrichedCartBuilder implements EnrichedCartStateInterest, ReadModelBuilder {

  private @Nullable CartId cartId;
  private @Nullable CustomerId customerId;
  private final List<LineItemSnapshot> lineItems = new ArrayList<>();
  private final Map<ProductId, CartArticle> currentArticles = new HashMap<>();
  private int itemCount;
  private @Nullable Money subtotal;

  /**
   * Creates a new EnrichedCartBuilder.
   */
  public EnrichedCartBuilder() {
    // Default constructor
  }

  @Override
  public void receiveCartId(final CartId cartId) {
    this.cartId = cartId;
  }

  @Override
  public void receiveCustomerId(final CustomerId customerId) {
    this.customerId = customerId;
  }

  @Override
  public void receiveLineItem(
      final CartItemId lineItemId,
      final ProductId productId,
      final String name,
      final Money price,
      final int quantity) {
    lineItems.add(new LineItemSnapshot(lineItemId, productId, name, price, quantity));
  }

  @Override
  public void receiveItemCount(final int itemCount) {
    this.itemCount = itemCount;
  }

  @Override
  public void receiveSubtotal(final Money subtotal) {
    this.subtotal = subtotal;
  }

  @Override
  public void receiveCurrentArticleData(final ProductId productId, final CartArticle cartArticle) {
    if (productId == null) {
      throw new IllegalArgumentException("Product ID cannot be null");
    }
    if (cartArticle == null) {
      throw new IllegalArgumentException("Cart article cannot be null");
    }
    currentArticles.put(productId, cartArticle);
  }

  /**
   * Builds the immutable {@link EnrichedCart} from the received state.
   *
   * <p>All required state (cartId, customerId) must have been received before calling
   * this method. Additionally, current article data must have been provided for all
   * line items in the cart.
   *
   * @return the constructed EnrichedCart
   * @throws IllegalStateException if required state has not been received or current
   *         article data is missing for any line item
   */
  public EnrichedCart build() {
    if (cartId == null) {
      throw new IllegalStateException("Cart ID has not been received");
    }
    if (customerId == null) {
      throw new IllegalStateException("Customer ID has not been received");
    }

    final List<EnrichedCartItem> enrichedItems = new ArrayList<>();
    for (final LineItemSnapshot snapshot : lineItems) {
      final CartArticle currentArticle = currentArticles.get(snapshot.productId());
      if (currentArticle == null) {
        throw new IllegalStateException(
            "Current article data has not been received for product: " + snapshot.productId().value());
      }

      final EnrichedCartItem enrichedItem = new EnrichedCartItem(
          snapshot.lineItemId(),
          snapshot.productId(),
          Quantity.of(snapshot.quantity()),
          Price.of(snapshot.price()),
          currentArticle);
      enrichedItems.add(enrichedItem);
    }

    return EnrichedCart.of(
        cartId,
        customerId,
        List.copyOf(enrichedItems),
        CartStatus.ACTIVE);
  }

  /**
   * Resets the builder to its initial state.
   *
   * <p>Call this method to reuse the builder for constructing another read model.
   */
  public void reset() {
    cartId = null;
    customerId = null;
    lineItems.clear();
    currentArticles.clear();
    itemCount = 0;
    subtotal = null;
  }

  /**
   * Internal snapshot of line item data received from the aggregate.
   */
  private record LineItemSnapshot(
      CartItemId lineItemId,
      ProductId productId,
      String name,
      Money price,
      int quantity) {}
}
