package de.sample.aiarchitecture.application;

import de.sample.aiarchitecture.domain.model.product.Product;
import de.sample.aiarchitecture.domain.model.product.ProductRepository;
import de.sample.aiarchitecture.domain.model.shared.Money;
import de.sample.aiarchitecture.domain.model.shared.Price;
import de.sample.aiarchitecture.domain.model.shared.ProductId;
import de.sample.aiarchitecture.infrastructure.api.DomainEventPublisher;
import java.util.Currency;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for updating a product's price.
 *
 * <p>This use case orchestrates the price update by:
 * <ol>
 *   <li>Retrieving the product</li>
 *   <li>Changing the price (business logic in aggregate)</li>
 *   <li>Persisting the updated product</li>
 *   <li>Publishing domain events</li>
 * </ol>
 */
@Service
@Transactional
public class UpdateProductPriceUseCase implements UseCase<UpdateProductPriceInput, UpdateProductPriceOutput> {

  private final ProductRepository productRepository;
  private final DomainEventPublisher eventPublisher;

  public UpdateProductPriceUseCase(
      final ProductRepository productRepository,
      final DomainEventPublisher eventPublisher) {
    this.productRepository = productRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public @NonNull UpdateProductPriceOutput execute(@NonNull final UpdateProductPriceInput input) {
    final ProductId productId = ProductId.of(input.productId());

    final Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + input.productId()));

    // Capture old price for output
    final Price oldPrice = product.price();

    // Business logic: change price (validates in aggregate)
    final Price newPrice = new Price(new Money(input.newPriceAmount(), Currency.getInstance(input.newPriceCurrency())));
    product.changePrice(newPrice);

    // Persist
    productRepository.save(product);

    // Publish domain events
    eventPublisher.publishAndClearEvents(product);

    // Map to output
    return new UpdateProductPriceOutput(
        product.id().value().toString(),
        oldPrice.value().amount(),
        oldPrice.value().currency().getCurrencyCode(),
        newPrice.value().amount(),
        newPrice.value().currency().getCurrencyCode()
    );
  }
}
