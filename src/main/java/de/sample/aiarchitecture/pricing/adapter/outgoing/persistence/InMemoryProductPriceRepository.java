package de.sample.aiarchitecture.pricing.adapter.outgoing.persistence;

import de.sample.aiarchitecture.pricing.application.shared.ProductPriceRepository;
import de.sample.aiarchitecture.pricing.domain.model.PriceId;
import de.sample.aiarchitecture.pricing.domain.model.ProductPrice;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of ProductPriceRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for product prices using
 * ConcurrentHashMap. A secondary index on ProductId enables efficient lookups by product.
 *
 * <p>In a production system, this would be replaced with a database implementation.
 */
@Repository
public class InMemoryProductPriceRepository implements ProductPriceRepository {

  private final ConcurrentHashMap<PriceId, ProductPrice> prices = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ProductId, PriceId> productIdIndex = new ConcurrentHashMap<>();

  @Override
  public Optional<ProductPrice> findById(@NonNull final PriceId id) {
    return Optional.ofNullable(prices.get(id));
  }

  @Override
  public Optional<ProductPrice> findByProductId(@NonNull final ProductId productId) {
    final PriceId priceId = productIdIndex.get(productId);
    if (priceId == null) {
      return Optional.empty();
    }
    return findById(priceId);
  }

  @Override
  public List<ProductPrice> findByProductIds(@NonNull final Collection<ProductId> productIds) {
    return productIds.stream()
        .map(productIdIndex::get)
        .filter(priceId -> priceId != null)
        .map(prices::get)
        .filter(price -> price != null)
        .toList();
  }

  @Override
  public ProductPrice save(@NonNull final ProductPrice productPrice) {
    prices.put(productPrice.id(), productPrice);
    productIdIndex.put(productPrice.productId(), productPrice.id());
    return productPrice;
  }

  @Override
  public void deleteById(@NonNull final PriceId id) {
    final ProductPrice productPrice = prices.remove(id);
    if (productPrice != null) {
      productIdIndex.remove(productPrice.productId());
    }
  }
}
