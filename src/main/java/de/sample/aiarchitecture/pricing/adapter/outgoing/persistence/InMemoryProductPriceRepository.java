package de.sample.aiarchitecture.pricing.adapter.outgoing.persistence;

import de.sample.aiarchitecture.pricing.application.shared.ProductPriceRepository;
import de.sample.aiarchitecture.pricing.domain.model.PriceId;
import de.sample.aiarchitecture.pricing.domain.model.ProductPrice;
import de.sample.aiarchitecture.product.adapter.incoming.openhost.ProductCatalogService;
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
 * <p><b>Async Initialization:</b> This repository uses {@link AsyncInitialize} to load pricing
 * data from existing products on startup. The {@code asyncInitialize()} method is invoked
 * asynchronously after bean initialization to copy prices from the Product context.
 *
 * <p>In a production system, this would be replaced with a database implementation.
 *
 * @see AsyncInitialize
 */
@Repository
@AsyncInitialize(priority = 60, description = "Initialize pricing data from products")
public class InMemoryProductPriceRepository implements ProductPriceRepository {

  private static final Logger logger = LoggerFactory.getLogger(InMemoryProductPriceRepository.class);

  private final ProductCatalogService productCatalogService;

  private final ConcurrentHashMap<PriceId, ProductPrice> prices = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ProductId, PriceId> productIdIndex = new ConcurrentHashMap<>();

  public InMemoryProductPriceRepository(final ProductCatalogService productCatalogService) {
    this.productCatalogService = productCatalogService;
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
   * <p>This method loads all products from the Product context via the Open Host Service and
   * creates a corresponding ProductPrice entry in the Pricing context for each product. This
   * ensures pricing data is available immediately after startup without manual intervention.
   *
   * <p><b>Pattern:</b> This demonstrates cross-context data synchronization during startup,
   * where the Pricing context initializes its data from the Product context using the
   * Open Host Service pattern for proper bounded context isolation.
   *
   * @see AsyncInitialize
   * @see ProductCatalogService
   * @see de.sample.aiarchitecture.infrastructure.config.AsyncConfiguration
   * @see de.sample.aiarchitecture.infrastructure.support.AsyncInitializationProcessor
   */
  @SuppressWarnings("deprecation") // Using deprecated migration method for initialization
  @Async
  public void asyncInitialize() {
    logger.info("Starting async initialization of ProductPriceRepository from products...");

    try {
      // Wait for product repository to complete initialization first
      Thread.sleep(3000);

      // Use the deprecated migration method to get initial prices
      final var products = productCatalogService.getAllProductsWithInitialPrice();
      int initializedCount = 0;

      for (final var productInfo : products) {
        // Skip if price already exists for this product
        if (productIdIndex.containsKey(productInfo.productId())) {
          continue;
        }

        // Create ProductPrice from Product's initial price via Open Host Service
        final var productPrice = ProductPrice.create(
            productInfo.productId(),
            productInfo.initialPrice()
        );

        // Save without triggering domain events (internal initialization)
        prices.put(productPrice.id(), productPrice);
        productIdIndex.put(productPrice.productId(), productPrice.id());
        initializedCount++;
      }

      logger.info(
          "ProductPriceRepository initialization completed. Created {} price entries from {} products.",
          initializedCount, products.size());

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("ProductPriceRepository async initialization interrupted", e);
    } catch (Exception e) {
      logger.error("Error during ProductPriceRepository async initialization", e);
    }
  }
}
