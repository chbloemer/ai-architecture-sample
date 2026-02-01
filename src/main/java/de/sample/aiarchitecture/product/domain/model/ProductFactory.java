package de.sample.aiarchitecture.product.domain.model;

import de.sample.aiarchitecture.product.domain.event.ProductCreated;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Factory;
import de.sample.aiarchitecture.sharedkernel.domain.model.Price;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import org.jspecify.annotations.NonNull;

/**
 * Factory for creating Product aggregates.
 *
 * <p>Encapsulates complex product creation logic and ensures all invariants
 * are satisfied from the moment of creation.
 */
public final class ProductFactory implements Factory {

  /**
   * Creates a new product with generated ID.
   *
   * <p>Raises a {@link ProductCreated} domain event.
   *
   * @param sku the SKU
   * @param name the product name
   * @param description the product description
   * @param price the product price
   * @param category the product category
   * @param stock the initial stock
   * @return a new Product aggregate
   */
  public Product createProduct(
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final ProductDescription description,
      @NonNull final Price price,
      @NonNull final Category category,
      @NonNull final ProductStock stock) {

    final ProductId id = ProductId.generate();

    final Product product = new Product(id, sku, name, description, price, category, stock);

    // Raise domain event
    product.registerEvent(ProductCreated.now(id, sku, name));

    return product;
  }

  /**
   * Creates a new product with specified ID.
   *
   * @param id the product ID
   * @param sku the SKU
   * @param name the product name
   * @param description the product description
   * @param price the product price
   * @param category the product category
   * @param stock the initial stock
   * @return a new Product aggregate
   */
  public Product createProductWithId(
      @NonNull final ProductId id,
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final ProductDescription description,
      @NonNull final Price price,
      @NonNull final Category category,
      @NonNull final ProductStock stock) {

    return new Product(id, sku, name, description, price, category, stock);
  }

  /**
   * Creates a new product with default empty description.
   *
   * @param sku the SKU
   * @param name the product name
   * @param price the product price
   * @param category the product category
   * @param stock the initial stock
   * @return a new Product aggregate
   */
  public Product createBasicProduct(
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final Price price,
      @NonNull final Category category,
      @NonNull final ProductStock stock) {

    return createProduct(sku, name, ProductDescription.empty(), price, category, stock);
  }
}
