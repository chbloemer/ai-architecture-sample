package de.sample.aiarchitecture.application;

import de.sample.aiarchitecture.domain.model.product.Product;
import de.sample.aiarchitecture.domain.model.product.ProductRepository;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving all products.
 *
 * <p>This is a query use case that retrieves all products without modifying state.
 */
@Service
@Transactional(readOnly = true)
public class GetAllProductsUseCase implements UseCase<GetAllProductsInput, GetAllProductsOutput> {

  private final ProductRepository productRepository;

  public GetAllProductsUseCase(final ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public @NonNull GetAllProductsOutput execute(@NonNull final GetAllProductsInput input) {
    final List<Product> products = productRepository.findAll();

    final List<GetAllProductsOutput.ProductSummary> summaries = products.stream()
        .map(product -> new GetAllProductsOutput.ProductSummary(
            product.id().value().toString(),
            product.sku().value(),
            product.name().value(),
            product.price().value().amount(),
            product.price().value().currency().getCurrencyCode(),
            product.category().name(),
            product.stock().quantity()
        ))
        .toList();

    return new GetAllProductsOutput(summaries);
  }
}
