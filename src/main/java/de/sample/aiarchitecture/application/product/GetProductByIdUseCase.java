package de.sample.aiarchitecture.application.product;

import de.sample.aiarchitecture.application.UseCase;
import de.sample.aiarchitecture.domain.model.product.Product;
import de.sample.aiarchitecture.domain.model.product.ProductRepository;
import de.sample.aiarchitecture.domain.model.shared.ProductId;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a product by its ID.
 *
 * <p>This is a query use case that retrieves product details without modifying state.
 */
@Service
@Transactional(readOnly = true)
public class GetProductByIdUseCase implements UseCase<GetProductByIdInput, GetProductByIdOutput> {

  private final ProductRepository productRepository;

  public GetProductByIdUseCase(final ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public @NonNull GetProductByIdOutput execute(@NonNull final GetProductByIdInput input) {
    final ProductId productId = ProductId.of(input.productId());

    final Optional<Product> productOpt = productRepository.findById(productId);

    if (productOpt.isEmpty()) {
      return GetProductByIdOutput.notFound();
    }

    final Product product = productOpt.get();

    return GetProductByIdOutput.found(
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
