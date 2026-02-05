package de.sample.aiarchitecture.product.application.getproductbyid;

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
import java.util.Currency;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a product by its ID.
 *
 * <p>This is a query use case that retrieves product details without modifying state.
 * Uses the Interest Interface pattern to build an enriched read model that combines
 * aggregate state with external data from Pricing and Inventory contexts.
 *
 * <p><b>Pattern:</b> Aggregate.provideStateTo(Builder) → Builder.build() → Result(EnrichedProduct)
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetProductByIdInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetProductByIdUseCase implements GetProductByIdInputPort {

  private static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

  private final ProductRepository productRepository;
  private final PricingDataPort pricingDataPort;
  private final ProductStockDataPort productStockDataPort;

  public GetProductByIdUseCase(
      final ProductRepository productRepository,
      final PricingDataPort pricingDataPort,
      final ProductStockDataPort productStockDataPort) {
    this.productRepository = productRepository;
    this.pricingDataPort = pricingDataPort;
    this.productStockDataPort = productStockDataPort;
  }

  @Override
  public GetProductByIdResult execute(final GetProductByIdQuery input) {
    final ProductId productId = ProductId.of(input.productId());

    final Optional<Product> productOpt = productRepository.findById(productId);

    if (productOpt.isEmpty()) {
      return GetProductByIdResult.notFound();
    }

    final Product product = productOpt.get();

    // Use builder pattern with Interest Interface
    final EnrichedProductBuilder builder = new EnrichedProductBuilder();
    product.provideStateTo(builder);

    // Build article data from external contexts
    final ProductArticle articleData = buildArticleData(productId);
    builder.receiveArticleData(articleData);

    // Build enriched product and wrap in result
    final EnrichedProduct enrichedProduct = builder.build();
    return GetProductByIdResult.found(enrichedProduct);
  }

  private ProductArticle buildArticleData(final ProductId productId) {
    // Fetch price from Pricing context
    final Optional<PriceData> priceData = pricingDataPort.getPrice(productId);
    final Money currentPrice = priceData
        .map(PriceData::currentPrice)
        .orElse(Money.zero(DEFAULT_CURRENCY));

    // Fetch stock from Inventory context
    final Optional<StockData> stockData = productStockDataPort.getStockData(productId);
    final int stockQuantity = stockData.map(StockData::availableStock).orElse(0);
    final boolean isAvailable = stockData.map(StockData::isAvailable).orElse(false);

    return new ProductArticle(currentPrice, stockQuantity, isAvailable);
  }
}
