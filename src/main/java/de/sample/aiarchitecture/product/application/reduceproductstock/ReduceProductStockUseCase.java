package de.sample.aiarchitecture.product.application.reduceproductstock;

import de.sample.aiarchitecture.product.application.reduceproductstock.ReduceProductStockInputPort;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for reducing product stock quantity.
 *
 * <p><b>Cross-Context Integration:</b> This use case is typically triggered by
 * domain events from other bounded contexts (e.g., CartCheckedOut event).
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link ReduceProductStockInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class ReduceProductStockUseCase implements ReduceProductStockInputPort {

  private final ProductRepository productRepository;

  public ReduceProductStockUseCase(final ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public @NonNull ReduceProductStockResponse execute(@NonNull final ReduceProductStockCommand input) {
    final ProductId productId = ProductId.of(input.productId());

    final Product product = productRepository
        .findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + input.productId()));

    final int previousStock = product.stock().quantity();

    // Reduce stock using domain logic
    product.decreaseStock(input.quantity());

    productRepository.save(product);

    final int newStock = product.stock().quantity();

    return new ReduceProductStockResponse(
        product.id().value().toString(),
        previousStock,
        newStock
    );
  }
}
