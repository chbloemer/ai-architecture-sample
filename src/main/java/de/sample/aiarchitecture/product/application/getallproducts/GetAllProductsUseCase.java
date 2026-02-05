package de.sample.aiarchitecture.product.application.getallproducts;

import de.sample.aiarchitecture.product.application.shared.PricingDataPort;
import de.sample.aiarchitecture.product.application.shared.PricingDataPort.PriceData;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.application.shared.ProductStockDataPort;
import de.sample.aiarchitecture.product.application.shared.ProductStockDataPort.StockData;
import de.sample.aiarchitecture.product.domain.model.EnrichedProduct;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.product.domain.model.ProductArticle;
import de.sample.aiarchitecture.product.domain.readmodel.EnrichedProductBuilder;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving all products.
 *
 * <p>This is a query use case that retrieves all products without modifying state.
 * Uses the Interest Interface pattern to build enriched read models that combine
 * aggregate state with external data from Pricing and Inventory contexts.
 *
 * <p><b>Pattern:</b> Aggregate.provideStateTo(Builder) → Builder.build() → Result(EnrichedProduct list)
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetAllProductsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetAllProductsUseCase implements GetAllProductsInputPort {

  private static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

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
  public GetAllProductsResult execute(final GetAllProductsQuery input) {
    final List<Product> products = productRepository.findAll();

    // Fetch prices and stock for all products from external contexts
    final List<ProductId> productIds = products.stream()
        .map(Product::id)
        .toList();
    final Map<ProductId, PriceData> prices = pricingDataPort.getPrices(productIds);
    final Map<ProductId, StockData> stocks = productStockDataPort.getStockData(productIds);

    // Use builder pattern with Interest Interface for each product
    final List<EnrichedProduct> enrichedProducts = new ArrayList<>();
    final EnrichedProductBuilder builder = new EnrichedProductBuilder();

    for (final Product product : products) {
      builder.reset();

      // Aggregate pushes its state to the builder
      product.provideStateTo(builder);

      // Build article data from external contexts
      final ProductArticle articleData = buildArticleData(product.id(), prices, stocks);
      builder.receiveArticleData(articleData);

      // Build enriched product and add to list
      enrichedProducts.add(builder.build());
    }

    return new GetAllProductsResult(enrichedProducts);
  }

  private ProductArticle buildArticleData(
      final ProductId productId,
      final Map<ProductId, PriceData> prices,
      final Map<ProductId, StockData> stocks) {

    final PriceData priceData = prices.get(productId);
    final Money currentPrice = priceData != null
        ? priceData.currentPrice()
        : Money.zero(DEFAULT_CURRENCY);

    final StockData stockData = stocks.get(productId);
    final int stockQuantity = stockData != null ? stockData.availableStock() : 0;
    final boolean isAvailable = stockData != null && stockData.isAvailable();

    return new ProductArticle(currentPrice, stockQuantity, isAvailable);
  }
}
