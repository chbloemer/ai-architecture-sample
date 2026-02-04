package de.sample.aiarchitecture.pricing.adapter.outgoing.persistence;

import de.sample.aiarchitecture.pricing.application.shared.ProductPriceRepository;
import de.sample.aiarchitecture.pricing.domain.model.PriceId;
import de.sample.aiarchitecture.pricing.domain.model.ProductPrice;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.infrastructure.AsyncInitialize;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of ProductPriceRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for product prices using
 * ConcurrentHashMap. A secondary index on ProductId enables efficient lookups by product.
 *
 * <p><b>Initialization:</b> Pricing data is initialized via two mechanisms:
 * <ul>
 *   <li>ProductCreatedEventConsumer - creates prices when products are created</li>
 *   <li>SampleDataInitializer - triggers ProductCreated events with initial prices</li>
 * </ul>
 *
 * <p>In a production system, this would be replaced with a database implementation.
 *
 * @see AsyncInitialize
 */
@Repository
@AsyncInitialize(priority = 60, description = "Initialize pricing data repository")
public class InMemoryProductPriceRepository implements ProductPriceRepository {

  private static final Logger logger = LoggerFactory.getLogger(InMemoryProductPriceRepository.class);

  private final ConcurrentHashMap<PriceId, ProductPrice> prices = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ProductId, PriceId> productIdIndex = new ConcurrentHashMap<>();

  public InMemoryProductPriceRepository() {
  }

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

  /**
   * Asynchronous initialization method triggered by {@link AsyncInitialize}.
   *
   * <p>This method performs cache warmup and logs initialization status. Actual pricing
   * data is populated via ProductCreatedEventConsumer when products are created.
   *
   * @see AsyncInitialize
   * @see de.sample.aiarchitecture.pricing.adapter.incoming.event.ProductCreatedEventConsumer
   * @see de.sample.aiarchitecture.infrastructure.config.AsyncConfiguration
   * @see de.sample.aiarchitecture.infrastructure.support.AsyncInitializationProcessor
   */
  @Async
  public void asyncInitialize() {
    logger.info("Starting async initialization of ProductPriceRepository...");

    try {
      // Wait for product events to be processed
      Thread.sleep(3000);

      logger.info(
          "ProductPriceRepository initialization completed. {} price entries available.",
          prices.size());

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("ProductPriceRepository async initialization interrupted", e);
    } catch (Exception e) {
      logger.error("Error during ProductPriceRepository async initialization", e);
    }
  }
}
