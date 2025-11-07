package de.sample.aiarchitecture.product.application.usecase.createproduct;

import de.sample.aiarchitecture.infrastructure.api.DomainEventPublisher;
import de.sample.aiarchitecture.product.application.port.out.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Category;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.product.domain.model.ProductDescription;
import de.sample.aiarchitecture.product.domain.model.ProductFactory;
import de.sample.aiarchitecture.product.domain.model.ProductName;
import de.sample.aiarchitecture.product.domain.model.ProductStock;
import de.sample.aiarchitecture.product.domain.model.SKU;
import de.sample.aiarchitecture.sharedkernel.application.marker.InputPort;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.common.Price;
import java.util.Currency;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating a new product.
 *
 * <p>This use case orchestrates the creation of a new product by:
 * <ol>
 *   <li>Validating business rules (SKU uniqueness)</li>
 *   <li>Creating the product aggregate using the factory</li>
 *   <li>Persisting the product via repository</li>
 *   <li>Publishing domain events</li>
 * </ol>
 */
@Service
@Transactional
public class CreateProductUseCase implements InputPort<CreateProductCommand, CreateProductResponse> {

  private final ProductRepository productRepository;
  private final ProductFactory productFactory;
  private final DomainEventPublisher eventPublisher;

  public CreateProductUseCase(
      final ProductRepository productRepository,
      final ProductFactory productFactory,
      final DomainEventPublisher eventPublisher) {
    this.productRepository = productRepository;
    this.productFactory = productFactory;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public @NonNull CreateProductResponse execute(@NonNull final CreateProductCommand input) {
    final SKU sku = new SKU(input.sku());

    // Business rule: SKU must be unique
    if (productRepository.existsBySku(sku)) {
      throw new IllegalArgumentException("Product with SKU already exists: " + sku.value());
    }

    // Convert input to domain value objects
    final ProductName name = new ProductName(input.name());
    final ProductDescription description = new ProductDescription(input.description());
    final Price price = new Price(new Money(input.priceAmount(), Currency.getInstance(input.priceCurrency())));
    final Category category = new Category(input.category());
    final ProductStock stock = new ProductStock(input.stockQuantity());

    // Create product aggregate
    final Product product = productFactory.createProduct(sku, name, description, price, category, stock);

    // Persist
    productRepository.save(product);

    // Publish domain events
    eventPublisher.publishAndClearEvents(product);

    // Map to output
    return new CreateProductResponse(
        product.id().value().toString(),
        product.sku().value(),
        product.name().value(),
        product.description().value(),
        product.price().value().amount(),
        product.price().value().currency().getCurrencyCode(),
        product.category().name(),
        product.stock().quantity()
    );
  }
}
