package de.sample.aiarchitecture.checkout.adapter.outgoing.product;

import de.sample.aiarchitecture.checkout.application.shared.CheckoutArticleDataPort;
import de.sample.aiarchitecture.inventory.adapter.incoming.openhost.InventoryService;
import de.sample.aiarchitecture.inventory.adapter.incoming.openhost.InventoryService.StockInfo;
import de.sample.aiarchitecture.pricing.adapter.incoming.openhost.PricingService;
import de.sample.aiarchitecture.pricing.adapter.incoming.openhost.PricingService.PriceInfo;
import de.sample.aiarchitecture.product.adapter.incoming.openhost.ProductCatalogService;
import de.sample.aiarchitecture.product.adapter.incoming.openhost.ProductCatalogService.ProductInfo;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Composite adapter that aggregates article data from multiple Open Host Services for checkout.
 *
 * <p>This adapter implements Checkout's CheckoutArticleDataPort by delegating to three OHS:
 * <ul>
 *   <li>ProductCatalogService - for product names</li>
 *   <li>PricingService - for current prices</li>
 *   <li>InventoryService - for stock availability</li>
 * </ul>
 *
 * <p>This adapter is the ONLY place in Checkout context that imports from Product,
 * Pricing, and Inventory contexts, isolating cross-context coupling to the adapter layer.
 *
 * <p><b>Hexagonal Architecture:</b> This is an outgoing adapter that implements
 * an output port by delegating to incoming adapters (OHS) of other contexts.
 */
@Component
public class CompositeCheckoutArticleDataAdapter implements CheckoutArticleDataPort {

    private final ProductCatalogService productCatalogService;
    private final PricingService pricingService;
    private final InventoryService inventoryService;

    public CompositeCheckoutArticleDataAdapter(
            ProductCatalogService productCatalogService,
            PricingService pricingService,
            InventoryService inventoryService) {
        this.productCatalogService = productCatalogService;
        this.pricingService = pricingService;
        this.inventoryService = inventoryService;
    }

    @Override
    public Map<ProductId, ArticleData> getArticleData(Collection<ProductId> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        // Fetch data from all three OHS services
        Map<ProductId, PriceInfo> prices = pricingService.getPrices(productIds);
        Map<ProductId, StockInfo> stocks = inventoryService.getStock(productIds);

        Map<ProductId, ArticleData> result = new HashMap<>();

        for (ProductId productId : productIds) {
            // ProductCatalogService doesn't have bulk fetch, so fetch individually
            Optional<ProductInfo> productInfo = productCatalogService.getProductInfo(productId);

            // Only include if we have product info (name is required)
            if (productInfo.isPresent()) {
                ArticleData articleData = combineData(
                    productId,
                    productInfo.get(),
                    prices.get(productId),
                    stocks.get(productId)
                );
                result.put(productId, articleData);
            }
        }

        return result;
    }

    /**
     * Combines data from multiple sources into a single ArticleData record.
     *
     * <p>Handles missing data gracefully:
     * <ul>
     *   <li>If pricing data is missing, uses price from ProductCatalogService</li>
     *   <li>If inventory data is missing, uses stock from ProductCatalogService</li>
     * </ul>
     */
    private ArticleData combineData(
            ProductId productId,
            ProductInfo productInfo,
            PriceInfo priceInfo,
            StockInfo stockInfo) {

        String name = productInfo.name();

        // Use dedicated pricing if available, fall back to product catalog price
        Money currentPrice = priceInfo != null
            ? priceInfo.currentPrice()
            : productInfo.price().value();

        // Use dedicated inventory if available, fall back to product catalog stock
        int availableStock = stockInfo != null
            ? stockInfo.availableStock()
            : productInfo.availableStock();

        boolean isAvailable = stockInfo != null
            ? stockInfo.isAvailable()
            : availableStock > 0;

        return new ArticleData(productId, name, currentPrice, availableStock, isAvailable);
    }
}
