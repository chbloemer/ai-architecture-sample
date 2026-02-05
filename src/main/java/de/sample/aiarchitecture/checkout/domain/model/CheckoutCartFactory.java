package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Factory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for assembling CheckoutCart instances from checkout line items and article data.
 *
 * <p>Encapsulates the complex assembly of enriched line items by combining checkout line items
 * with their corresponding article data to create a fully hydrated CheckoutCart.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Validates that all line items have corresponding article data</li>
 *   <li>Creates EnrichedCheckoutLineItem instances by pairing line items with articles</li>
 *   <li>Assembles the final CheckoutCart with all enriched items</li>
 * </ul>
 */
public final class CheckoutCartFactory implements Factory {

  /**
   * Creates a CheckoutCart from cart identifiers, line items, and article data.
   *
   * @param cartId the cart identifier
   * @param customerId the customer identifier
   * @param lineItems the list of checkout line items
   * @param articleData map of product IDs to their corresponding article data
   * @return a new CheckoutCart with enriched line items
   * @throws IllegalArgumentException if article data is missing for any line item
   */
  public CheckoutCart create(
      final CartId cartId,
      final CustomerId customerId,
      final List<CheckoutLineItem> lineItems,
      final Map<ProductId, CheckoutArticle> articleData) {

    validateArticleDataComplete(lineItems, articleData);

    final List<EnrichedCheckoutLineItem> enrichedItems = createEnrichedItems(lineItems, articleData);

    return CheckoutCart.of(cartId, customerId, enrichedItems);
  }

  /**
   * Creates a CheckoutCart from an existing CheckoutSession and article data.
   *
   * <p>This is a convenience method for assembling a CheckoutCart directly from a checkout
   * session, using its cart ID, customer ID, and line items.
   *
   * @param session the checkout session
   * @param articleData map of product IDs to their corresponding article data
   * @return a new CheckoutCart with enriched line items
   * @throws IllegalArgumentException if article data is missing for any line item
   */
  public CheckoutCart fromSession(
      final CheckoutSession session,
      final Map<ProductId, CheckoutArticle> articleData) {

    return create(
        session.cartId(),
        session.customerId(),
        session.lineItems(),
        articleData);
  }

  private void validateArticleDataComplete(
      final List<CheckoutLineItem> lineItems,
      final Map<ProductId, CheckoutArticle> articleData) {

    final List<ProductId> missingArticles = new ArrayList<>();

    for (final CheckoutLineItem lineItem : lineItems) {
      if (!articleData.containsKey(lineItem.productId())) {
        missingArticles.add(lineItem.productId());
      }
    }

    if (!missingArticles.isEmpty()) {
      throw new IllegalArgumentException(
          "Missing article data for product IDs: " + missingArticles);
    }
  }

  private List<EnrichedCheckoutLineItem> createEnrichedItems(
      final List<CheckoutLineItem> lineItems,
      final Map<ProductId, CheckoutArticle> articleData) {

    final List<EnrichedCheckoutLineItem> enrichedItems = new ArrayList<>();

    for (final CheckoutLineItem lineItem : lineItems) {
      final CheckoutArticle article = articleData.get(lineItem.productId());
      enrichedItems.add(EnrichedCheckoutLineItem.of(lineItem, article));
    }

    return enrichedItems;
  }
}
