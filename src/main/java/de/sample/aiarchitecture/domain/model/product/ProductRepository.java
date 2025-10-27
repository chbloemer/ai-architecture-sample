package de.sample.aiarchitecture.domain.model.product;

import de.sample.aiarchitecture.domain.model.ddd.Repository;
import de.sample.aiarchitecture.domain.model.shared.ProductId;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

/**
 * Repository interface for Product aggregate.
 *
 * <p>Provides collection-like access to Product aggregates using domain language.
 * Implementation resides in the secondary adapter layer.
 *
 * <p>Extends the base {@link Repository} interface which provides common methods:
 * <ul>
 *   <li>{@code findById(ProductId)} - inherited from base interface
 *   <li>{@code save(Product)} - inherited from base interface
 *   <li>{@code deleteById(ProductId)} - inherited from base interface
 * </ul>
 */
public interface ProductRepository extends Repository<Product, ProductId> {

  /**
   * Finds a product by its SKU (Stock Keeping Unit).
   *
   * @param sku the SKU to search for
   * @return the product if found, empty otherwise
   */
  Optional<Product> findBySku(@NonNull SKU sku);

  /**
   * Finds all products in a specific category.
   *
   * @param category the category to filter by
   * @return list of products in the category
   */
  List<Product> findByCategory(@NonNull Category category);

  /**
   * Retrieves all products.
   *
   * @return list of all products
   */
  List<Product> findAll();

  /**
   * Checks if a product with the given SKU exists.
   *
   * @param sku the SKU to check
   * @return true if exists, false otherwise
   */
  boolean existsBySku(@NonNull SKU sku);
}
