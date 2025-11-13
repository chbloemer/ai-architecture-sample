package de.sample.aiarchitecture.product.application.updateproductprice;

import de.sample.aiarchitecture.product.application.updateproductprice.UpdateProductPriceInputPort;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.common.Price;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import de.sample.aiarchitecture.sharedkernel.application.port.DomainEventPublisher;
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
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link UpdateProductPriceInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class UpdateProductPriceUseCase implements UpdateProductPriceInputPort {

  private final ProductRepository productRepository;
  private final DomainEventPublisher eventPublisher;

  public UpdateProductPriceUseCase(
      final ProductRepository productRepository,
      final DomainEventPublisher eventPublisher) {
    this.productRepository = productRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public @NonNull UpdateProductPriceResponse execute(@NonNull final UpdateProductPriceCommand input) {
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
    return new UpdateProductPriceResponse(
        product.id().value().toString(),
        oldPrice.value().amount(),
        oldPrice.value().currency().getCurrencyCode(),
        newPrice.value().amount(),
        newPrice.value().currency().getCurrencyCode()
    );
  }
}
