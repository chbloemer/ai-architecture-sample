package de.sample.aiarchitecture.product.domain.model;

import de.sample.aiarchitecture.product.domain.event.ProductCreated;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Factory;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import org.jspecify.annotations.NonNull;

/**
 * Factory for creating Product aggregates.
 *
 * <p>Encapsulates complex product creation logic and ensures all invariants
 * are satisfied from the moment of creation.
 *
 * <p><b>Note:</b> Pricing is managed by the Pricing bounded context. The initial price
 * is included in the ProductCreated event for cross-context synchronization.
 * Stock/availability is managed by the Inventory bounded context.
 */
public final class ProductFactory implements Factory {

  /**
   * Creates a new product with generated ID.
   *
   * <p>Raises a {@link ProductCreated} domain event with the initial price for
   * synchronization with the Pricing bounded context (and stock for Inventory context).
   *
   * @param sku the SKU
   * @param name the product name
   * @param description the product description
   * @param category the product category
   * @param initialPrice the initial price (for Pricing context synchronization)
   * @param initialStock the initial stock quantity (for Inventory context synchronization)
   * @return a new Product aggregate
   */
  public Product createProduct(
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final ProductDescription description,
      @NonNull final Category category,
      @NonNull final Money initialPrice,
      final int initialStock) {

    final ProductId id = ProductId.generate();

    final Product product = new Product(id, sku, name, description, category);

    // Raise domain event with initial price for Pricing context and stock for Inventory context
    product.registerEvent(ProductCreated.now(id, sku, name, initialPrice, initialStock));

    return product;
  }

  /**
   * Creates a new product with specified ID.
   *
   * <p>Note: This method does NOT raise a ProductCreated event. Use this for
   * reconstituting products from persistence or for testing purposes only.
   *
   * @param id the product ID
   * @param sku the SKU
   * @param name the product name
   * @param description the product description
   * @param category the product category
   * @return a new Product aggregate
   */
  public Product createProductWithId(
      @NonNull final ProductId id,
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final ProductDescription description,
      @NonNull final Category category) {

    return new Product(id, sku, name, description, category);
  }

  /**
   * Creates a new product with default empty description.
   *
   * @param sku the SKU
   * @param name the product name
   * @param category the product category
   * @param initialPrice the initial price (for Pricing context synchronization)
   * @param initialStock the initial stock quantity (for Inventory context synchronization)
   * @return a new Product aggregate
   */
  public Product createBasicProduct(
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final Category category,
      @NonNull final Money initialPrice,
      final int initialStock) {

    return createProduct(sku, name, ProductDescription.empty(), category, initialPrice, initialStock);
  }
}
