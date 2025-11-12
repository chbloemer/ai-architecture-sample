package de.sample.aiarchitecture.product.application.getproductbyid;

import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdInputPort;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a product by its ID.
 *
 * <p>This is a query use case that retrieves product details without modifying state.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetProductByIdInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetProductByIdUseCase implements GetProductByIdInputPort {

  private final ProductRepository productRepository;

  public GetProductByIdUseCase(final ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public @NonNull GetProductByIdResponse execute(@NonNull final GetProductByIdQuery input) {
    final ProductId productId = ProductId.of(input.productId());

    final Optional<Product> productOpt = productRepository.findById(productId);

    if (productOpt.isEmpty()) {
      return GetProductByIdResponse.notFound();
    }

    final Product product = productOpt.get();

    return GetProductByIdResponse.found(
        product.id().value().toString(),
        product.sku().value(),
        product.name().value(),
        product.description().value(),
        product.price().value().amount(),
        product.price().value().currency().getCurrencyCode(),
        product.category().name(),
        product.stock().quantity(),
        product.isAvailable()
    );
  }
}
