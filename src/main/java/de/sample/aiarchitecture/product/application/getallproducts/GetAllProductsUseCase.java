package de.sample.aiarchitecture.product.application.getallproducts;

import de.sample.aiarchitecture.product.application.shared.PricingDataPort;
import de.sample.aiarchitecture.product.application.shared.PricingDataPort.PriceData;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.application.shared.ProductStockDataPort;
import de.sample.aiarchitecture.product.application.shared.ProductStockDataPort.StockData;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving all products.
 *
 * <p>This is a query use case that retrieves all products without modifying state.
 * Pricing data is fetched from the Pricing bounded context via the PricingDataPort.
 * Stock data is fetched from the Inventory bounded context via the ProductStockDataPort.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetAllProductsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetAllProductsUseCase implements GetAllProductsInputPort {

  private final ProductRepository productRepository;
  private final PricingDataPort pricingDataPort;
  private final ProductStockDataPort productStockDataPort;

  public GetAllProductsUseCase(
      final ProductRepository productRepository,
      final PricingDataPort pricingDataPort,
      final ProductStockDataPort productStockDataPort) {
    this.productRepository = productRepository;
    this.pricingDataPort = pricingDataPort;
    this.productStockDataPort = productStockDataPort;
  }

  @Override
  public @NonNull GetAllProductsResult execute(@NonNull final GetAllProductsQuery input) {
    final List<Product> products = productRepository.findAll();

    // Fetch prices and stock for all products from external contexts
    final List<ProductId> productIds = products.stream()
        .map(Product::id)
        .toList();
    final Map<ProductId, PriceData> prices = pricingDataPort.getPrices(productIds);
    final Map<ProductId, StockData> stocks = productStockDataPort.getStockData(productIds);

    final List<GetAllProductsResult.ProductSummary> summaries = products.stream()
        .map(product -> {
            PriceData priceData = prices.get(product.id());
            BigDecimal priceAmount = priceData != null ? priceData.currentPrice().amount() : BigDecimal.ZERO;
            String priceCurrency = priceData != null ? priceData.currentPrice().currency().getCurrencyCode() : "EUR";

            StockData stockData = stocks.get(product.id());
            int stockQuantity = stockData != null ? stockData.availableStock() : 0;

            return new GetAllProductsResult.ProductSummary(
                product.id().value().toString(),
                product.sku().value(),
                product.name().value(),
                priceAmount,
                priceCurrency,
                product.category().name(),
                stockQuantity
            );
        })
        .toList();

    return new GetAllProductsResult(summaries);
  }
}
