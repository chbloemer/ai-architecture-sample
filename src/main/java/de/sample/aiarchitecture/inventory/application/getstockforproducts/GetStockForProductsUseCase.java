package de.sample.aiarchitecture.inventory.application.getstockforproducts;

import de.sample.aiarchitecture.inventory.application.getstockforproducts.GetStockForProductsResult.StockData;
import de.sample.aiarchitecture.inventory.application.shared.StockLevelRepository;
import de.sample.aiarchitecture.inventory.domain.model.StockLevel;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving stock levels for multiple products.
 *
 * <p>This use case provides bulk stock lookup for Cart/Checkout to efficiently
 * check availability for multiple products in a single database call.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetStockForProductsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetStockForProductsUseCase implements GetStockForProductsInputPort {

  private final StockLevelRepository stockLevelRepository;

  public GetStockForProductsUseCase(@NonNull final StockLevelRepository stockLevelRepository) {
    this.stockLevelRepository = stockLevelRepository;
  }

  @Override
  public @NonNull GetStockForProductsResult execute(@NonNull final GetStockForProductsQuery query) {
    final List<StockLevel> stockLevels =
        stockLevelRepository.findByProductIds(query.productIds());

    final Map<ProductId, StockData> stocks = stockLevels.stream()
        .map(this::mapToStockData)
        .collect(Collectors.toMap(StockData::productId, data -> data));

    return new GetStockForProductsResult(stocks);
  }

  private StockData mapToStockData(final StockLevel stockLevel) {
    final int availableStock =
        stockLevel.availableQuantity().value() - stockLevel.reservedQuantity().value();
    return new StockData(
        stockLevel.productId(),
        availableStock,
        stockLevel.isAvailable());
  }
}
