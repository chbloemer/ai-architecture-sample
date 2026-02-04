package de.sample.aiarchitecture.product.adapter.outgoing.pricing;

import de.sample.aiarchitecture.pricing.adapter.incoming.openhost.PricingService;
import de.sample.aiarchitecture.pricing.adapter.incoming.openhost.PricingService.PriceInfo;
import de.sample.aiarchitecture.product.application.shared.PricingDataPort;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Outgoing adapter that implements PricingDataPort by delegating to the Pricing context's
 * Open Host Service.
 *
 * <p>This adapter is the ONLY place in Product context that imports from the Pricing context,
 * isolating cross-context coupling to the adapter layer.
 *
 * <p><b>Hexagonal Architecture:</b> This is an outgoing adapter that implements
 * an output port by delegating to an incoming adapter (OHS) of another context.
 */
@Component
public class PricingDataAdapter implements PricingDataPort {

    private final PricingService pricingService;

    public PricingDataAdapter(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @Override
    public Map<ProductId, PriceData> getPrices(Collection<ProductId> productIds) {
        Map<ProductId, PriceInfo> prices = pricingService.getPrices(productIds);

        return prices.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new PriceData(
                    entry.getKey(),
                    entry.getValue().currentPrice()
                )
            ));
    }

    @Override
    public Optional<PriceData> getPrice(ProductId productId) {
        return pricingService.getPrice(productId)
            .map(priceInfo -> new PriceData(productId, priceInfo.currentPrice()));
    }
}
