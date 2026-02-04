package de.sample.aiarchitecture.product.application.shared;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Output port for accessing pricing data from the Product context.
 *
 * <p>Since pricing is managed by the Pricing bounded context, the Product context
 * defines this port for its pricing data needs. This port is implemented by an
 * outgoing adapter that delegates to the Pricing context's Open Host Service.
 *
 * <p><b>Hexagonal Architecture:</b> This is a secondary/driven port that defines what
 * the Product application layer needs from the Pricing context.
 */
public interface PricingDataPort extends OutputPort {

    /**
     * Price data as needed by Product context.
     *
     * @param productId the product identifier
     * @param currentPrice the current price of the product
     */
    record PriceData(
        ProductId productId,
        Money currentPrice
    ) {}

    /**
     * Retrieves price data for multiple products.
     *
     * @param productIds the collection of product IDs
     * @return a map of product ID to price data for found products
     */
    Map<ProductId, PriceData> getPrices(Collection<ProductId> productIds);

    /**
     * Retrieves price data for a single product.
     *
     * @param productId the product ID
     * @return price data if found
     */
    Optional<PriceData> getPrice(ProductId productId);
}
