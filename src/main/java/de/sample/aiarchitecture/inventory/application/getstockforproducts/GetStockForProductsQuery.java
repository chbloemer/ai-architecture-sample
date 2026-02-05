package de.sample.aiarchitecture.inventory.application.getstockforproducts;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import java.util.List;

/**
 * Query model for retrieving stock levels for multiple products.
 *
 * <p>Used by Cart/Checkout to efficiently check availability for multiple products at once.
 *
 * @param productIds the collection of product IDs to check stock for
 */
public record GetStockForProductsQuery(Collection<ProductId> productIds) {

  public GetStockForProductsQuery {
    if (productIds == null) {
      throw new IllegalArgumentException("ProductIds cannot be null");
    }
  }

  /**
   * Creates a new query with the given product IDs.
   *
   * @param productIds the product IDs to query
   * @return a new GetStockForProductsQuery
   */
  public static GetStockForProductsQuery of(final Collection<ProductId> productIds) {
    return new GetStockForProductsQuery(List.copyOf(productIds));
  }
}
