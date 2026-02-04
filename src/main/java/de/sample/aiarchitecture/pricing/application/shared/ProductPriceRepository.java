package de.sample.aiarchitecture.pricing.application.shared;

import de.sample.aiarchitecture.pricing.domain.model.PriceId;
import de.sample.aiarchitecture.pricing.domain.model.ProductPrice;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

/**
 * Repository interface for ProductPrice aggregate.
 *
 * <p>Provides collection-like access to ProductPrice aggregates using domain language.
 * Implementation resides in the secondary adapter layer.
 *
 * <p>Extends the base {@link Repository} interface which provides common methods:
 * <ul>
 *   <li>{@code findById(PriceId)} - inherited from base interface
 *   <li>{@code save(ProductPrice)} - inherited from base interface
 *   <li>{@code deleteById(PriceId)} - inherited from base interface
 * </ul>
 */
public interface ProductPriceRepository extends Repository<ProductPrice, PriceId> {

  /**
   * Finds the price for a specific product.
   *
   * @param productId the product ID to search for
   * @return the product price if found, empty otherwise
   */
  Optional<ProductPrice> findByProductId(@NonNull ProductId productId);

  /**
   * Finds prices for multiple products.
   *
   * @param productIds the collection of product IDs to search for
   * @return list of product prices for the given product IDs
   */
  List<ProductPrice> findByProductIds(@NonNull Collection<ProductId> productIds);
}
