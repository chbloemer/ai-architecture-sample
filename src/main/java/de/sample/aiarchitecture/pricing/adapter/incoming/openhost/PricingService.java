package de.sample.aiarchitecture.pricing.adapter.incoming.openhost;

import de.sample.aiarchitecture.pricing.application.getpricesforproducts.GetPricesForProductsInputPort;
import de.sample.aiarchitecture.pricing.application.getpricesforproducts.GetPricesForProductsQuery;
import de.sample.aiarchitecture.pricing.application.getpricesforproducts.GetPricesForProductsResult;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.strategic.OpenHostService;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Open Host Service for Pricing.
 *
 * <p>This is an incoming adapter that exposes Pricing context capabilities to other bounded
 * contexts. It delegates to use cases (input ports) and translates responses to OHS DTOs.
 *
 * <p>Consuming contexts should NOT use this service directly in their use cases - they should
 * define their own output ports and implement adapters that delegate to this service.
 *
 * <p><b>Hexagonal Architecture:</b> As an incoming adapter, this service calls input ports (use
 * cases), NOT output ports (repositories) directly.
 */
@OpenHostService(
    context = "Pricing",
    description = "Provides pricing information for other bounded contexts")
@Service("pricingContextOhs")
public class PricingService {

  private final GetPricesForProductsInputPort getPricesForProductsInputPort;

  public PricingService(GetPricesForProductsInputPort getPricesForProductsInputPort) {
    this.getPricesForProductsInputPort = getPricesForProductsInputPort;
  }

  /**
   * Price information DTO for cross-context communication.
   *
   * @param productId the product ID
   * @param currentPrice the current price
   * @param effectiveFrom when the price became effective
   */
  public record PriceInfo(
      ProductId productId, Money currentPrice, Instant effectiveFrom) {}

  /**
   * Retrieves prices for multiple products.
   *
   * @param productIds the collection of product IDs to get prices for
   * @return map of product IDs to their price info
   */
  public Map<ProductId, PriceInfo> getPrices(Collection<ProductId> productIds) {
    if (productIds.isEmpty()) {
      return Collections.emptyMap();
    }

    GetPricesForProductsResult result =
        getPricesForProductsInputPort.execute(new GetPricesForProductsQuery(productIds));

    return result.prices().entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry ->
                    new PriceInfo(
                        entry.getValue().productId(),
                        entry.getValue().currentPrice(),
                        entry.getValue().effectiveFrom())));
  }

  /**
   * Retrieves price for a single product.
   *
   * @param productId the product ID
   * @return price info if found
   */
  public Optional<PriceInfo> getPrice(ProductId productId) {
    Map<ProductId, PriceInfo> prices = getPrices(Collections.singletonList(productId));
    return Optional.ofNullable(prices.get(productId));
  }
}
