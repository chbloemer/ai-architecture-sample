package de.sample.aiarchitecture.product.application;

import de.sample.aiarchitecture.infrastructure.api.DomainEventPublisher;
import de.sample.aiarchitecture.product.application.port.out.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.product.domain.model.Category;
import de.sample.aiarchitecture.product.domain.model.ProductDescription;
import de.sample.aiarchitecture.product.domain.model.ProductFactory;
import de.sample.aiarchitecture.product.domain.model.ProductName;
import de.sample.aiarchitecture.product.domain.model.ProductStock;
import de.sample.aiarchitecture.product.domain.model.SKU;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;

import de.sample.aiarchitecture.sharedkernel.domain.common.Price;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import de.sample.aiarchitecture.infrastructure.api.DomainEventPublisher;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service for Product operations.
 *
 * <p>Orchestrates product-related use cases and coordinates domain objects.
 * This service is thin and delegates business logic to the domain model.
 *
 * <p><b>Transactional Boundary:</b>
 * This service defines the transactional boundary for product operations. Each public method
 * represents a use case and runs within a transaction. Domain events are published after
 * successful transaction commit.
 *
 * <p><b>Domain Events:</b>
 * This service publishes domain events after successfully persisting aggregates,
 * enabling eventual consistency and loose coupling between bounded contexts.
 */
@Service
@Transactional
public class ProductApplicationService {

  private final ProductRepository productRepository;
  private final ProductFactory productFactory;
  private final DomainEventPublisher eventPublisher;

  public ProductApplicationService(
      final ProductRepository productRepository,
      final ProductFactory productFactory,
      final DomainEventPublisher eventPublisher) {
    this.productRepository = productRepository;
    this.productFactory = productFactory;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Creates a new product.
   *
   * <p>Publishes a {@link de.sample.aiarchitecture.domain.model.product.ProductCreated} event.
   *
   * @param sku the SKU
   * @param name the product name
   * @param description the product description
   * @param price the product price
   * @param category the product category
   * @param stock the initial stock
   * @return the created product
   * @throws IllegalArgumentException if SKU already exists
   */
  public Product createProduct(
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final ProductDescription description,
      @NonNull final Price price,
      @NonNull final Category category,
      @NonNull final ProductStock stock) {

    // Check if SKU already exists
    if (productRepository.existsBySku(sku)) {
      throw new IllegalArgumentException("Product with SKU already exists: " + sku.value());
    }

    final Product product = productFactory.createProduct(sku, name, description, price, category, stock);

    productRepository.save(product);

    // Publish domain events
    eventPublisher.publishAndClearEvents(product);

    return product;
  }

  /**
   * Finds a product by its ID.
   *
   * @param id the product ID
   * @return the product if found
   */
  @Transactional(readOnly = true)
  public Optional<Product> findProductById(@NonNull final ProductId id) {
    return productRepository.findById(id);
  }

  /**
   * Finds a product by its SKU.
   *
   * @param sku the SKU
   * @return the product if found
   */
  @Transactional(readOnly = true)
  public Optional<Product> findProductBySku(@NonNull final SKU sku) {
    return productRepository.findBySku(sku);
  }

  /**
   * Finds all products in a category.
   *
   * @param category the category
   * @return list of products
   */
  @Transactional(readOnly = true)
  public List<Product> findProductsByCategory(@NonNull final Category category) {
    return productRepository.findByCategory(category);
  }

  /**
   * Retrieves all products.
   *
   * @return list of all products
   */
  @Transactional(readOnly = true)
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  /**
   * Updates a product's price.
   *
   * <p>Publishes a {@link de.sample.aiarchitecture.domain.model.product.ProductPriceChanged} event.
   *
   * @param productId the product ID
   * @param newPrice the new price
   * @throws IllegalArgumentException if product not found
   */
  public void updateProductPrice(@NonNull final ProductId productId, @NonNull final Price newPrice) {
    final Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId.value()));

    product.changePrice(newPrice);

    productRepository.save(product);

    // Publish domain events
    eventPublisher.publishAndClearEvents(product);
  }

  /**
   * Updates a product's stock.
   *
   * @param productId the product ID
   * @param newStock the new stock
   * @throws IllegalArgumentException if product not found
   */
  public void updateProductStock(
      @NonNull final ProductId productId, @NonNull final ProductStock newStock) {
    final Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId.value()));

    product.updateStock(newStock);

    productRepository.save(product);
  }

  /**
   * Updates product details.
   *
   * @param productId the product ID
   * @param name the new name
   * @param description the new description
   * @param category the new category
   * @throws IllegalArgumentException if product not found
   */
  public void updateProductDetails(
      @NonNull final ProductId productId,
      @NonNull final ProductName name,
      @NonNull final ProductDescription description,
      @NonNull final Category category) {

    final Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId.value()));

    product.updateName(name);
    product.updateDescription(description);
    product.updateCategory(category);

    productRepository.save(product);
  }

  /**
   * Deletes a product.
   *
   * @param productId the product ID
   */
  public void deleteProduct(@NonNull final ProductId productId) {
    productRepository.deleteById(productId);
  }
}
