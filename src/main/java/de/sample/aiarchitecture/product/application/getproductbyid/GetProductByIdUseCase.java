package de.sample.aiarchitecture.product.application.getproductbyid;

import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.product.application.shared.PricingDataPort;
import de.sample.aiarchitecture.product.application.shared.PricingDataPort.PriceData;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a product by its ID.
 *
 * <p>This is a query use case that retrieves product details without modifying state.
 * Pricing data is fetched from the Pricing bounded context via the PricingDataPort.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetProductByIdInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetProductByIdUseCase implements GetProductByIdInputPort {

  private final ProductRepository productRepository;
  private final PricingDataPort pricingDataPort;

  public GetProductByIdUseCase(
      final ProductRepository productRepository,
      final PricingDataPort pricingDataPort) {
    this.productRepository = productRepository;
    this.pricingDataPort = pricingDataPort;
  }

  @Override
  public @NonNull GetProductByIdResult execute(@NonNull final GetProductByIdQuery input) {
    final ProductId productId = ProductId.of(input.productId());

    final Optional<Product> productOpt = productRepository.findById(productId);

    if (productOpt.isEmpty()) {
      return GetProductByIdResult.notFound();
    }

    final Product product = productOpt.get();

    // Fetch price from Pricing context
    Optional<PriceData> priceData = pricingDataPort.getPrice(productId);
    BigDecimal priceAmount = priceData.map(pd -> pd.currentPrice().amount()).orElse(BigDecimal.ZERO);
    String priceCurrency = priceData.map(pd -> pd.currentPrice().currency().getCurrencyCode()).orElse("EUR");

    return GetProductByIdResult.found(
        product.id().value().toString(),
        product.sku().value(),
        product.name().value(),
        product.description().value(),
        priceAmount,
        priceCurrency,
        product.category().name(),
        product.stock().quantity(),
        product.isAvailable()
    );
  }
}
