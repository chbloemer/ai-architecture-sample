package de.sample.aiarchitecture.portadapter.outgoing.product;

import de.sample.aiarchitecture.domain.model.product.*;
import de.sample.aiarchitecture.domain.model.shared.ProductId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of ProductRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for products
 * using ConcurrentHashMap. In a production system, this would be replaced with
 * a database implementation.
 */
@Repository
public class InMemoryProductRepository implements ProductRepository {

  private final ConcurrentHashMap<ProductId, Product> products = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<SKU, ProductId> skuIndex = new ConcurrentHashMap<>();

  @Override
  public Optional<Product> findById(@NonNull final ProductId id) {
    return Optional.ofNullable(products.get(id));
  }

  @Override
  public Optional<Product> findBySku(@NonNull final SKU sku) {
    final ProductId productId = skuIndex.get(sku);
    if (productId == null) {
      return Optional.empty();
    }
    return findById(productId);
  }

  @Override
  public List<Product> findByCategory(@NonNull final Category category) {
    return products.values().stream()
        .filter(product -> product.category().equals(category))
        .toList();
  }

  @Override
  public List<Product> findAll() {
    return List.copyOf(products.values());
  }

  @Override
  public Product save(@NonNull final Product product) {
    products.put(product.id(), product);
    skuIndex.put(product.sku(), product.id());
    return product;
  }

  @Override
  public void deleteById(@NonNull final ProductId id) {
    final Product product = products.remove(id);
    if (product != null) {
      skuIndex.remove(product.sku());
    }
  }

  @Override
  public boolean existsBySku(@NonNull final SKU sku) {
    return skuIndex.containsKey(sku);
  }
}
