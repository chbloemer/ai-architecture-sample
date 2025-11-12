package de.sample.aiarchitecture.product.application.getallproducts;

import de.sample.aiarchitecture.product.application.getallproducts.GetAllProductsInputPort;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving all products.
 *
 * <p>This is a query use case that retrieves all products without modifying state.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetAllProductsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetAllProductsUseCase implements GetAllProductsInputPort {

  private final ProductRepository productRepository;

  public GetAllProductsUseCase(final ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public @NonNull GetAllProductsResponse execute(@NonNull final GetAllProductsQuery input) {
    final List<Product> products = productRepository.findAll();

    final List<GetAllProductsResponse.ProductSummary> summaries = products.stream()
        .map(product -> new GetAllProductsResponse.ProductSummary(
            product.id().value().toString(),
            product.sku().value(),
            product.name().value(),
            product.price().value().amount(),
            product.price().value().currency().getCurrencyCode(),
            product.category().name(),
            product.stock().quantity()
        ))
        .toList();

    return new GetAllProductsResponse(summaries);
  }
}
