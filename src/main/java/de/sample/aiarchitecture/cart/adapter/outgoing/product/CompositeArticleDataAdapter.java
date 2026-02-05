package de.sample.aiarchitecture.cart.adapter.outgoing.product;

import de.sample.aiarchitecture.cart.application.shared.ArticleDataPort;
import de.sample.aiarchitecture.cart.domain.model.CartArticle;
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
 * Composite adapter that aggregates article data from multiple Open Host Services.
 *
 * <p>This adapter implements Cart's ArticleDataPort by delegating to three OHS:
 * <ul>
 *   <li>ProductCatalogService - for product names (identity/description)</li>
 *   <li>PricingService - for current prices</li>
 *   <li>InventoryService - for stock availability</li>
 * </ul>
 *
 * <p>This adapter is the ONLY place in Cart context that imports from Product,
 * Pricing, and Inventory contexts, isolating cross-context coupling to the adapter layer.
 *
 * <p><b>Hexagonal Architecture:</b> This is an outgoing adapter that implements
 * an output port by delegating to incoming adapters (OHS) of other contexts.
 */
@Component
public class CompositeArticleDataAdapter implements ArticleDataPort {

    private final ProductCatalogService productCatalogService;
    private final PricingService pricingService;
    private final InventoryService inventoryService;

    public CompositeArticleDataAdapter(
            ProductCatalogService productCatalogService,
            PricingService pricingService,
            InventoryService inventoryService) {
        this.productCatalogService = productCatalogService;
        this.pricingService = pricingService;
        this.inventoryService = inventoryService;
    }

    @Override
    public Map<ProductId, CartArticle> getArticleData(Collection<ProductId> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        // Fetch data from all three OHS services
        Map<ProductId, PriceInfo> prices = pricingService.getPrices(productIds);
        Map<ProductId, StockInfo> stocks = inventoryService.getStock(productIds);

        Map<ProductId, CartArticle> result = new HashMap<>();

        for (ProductId productId : productIds) {
            // ProductCatalogService doesn't have bulk fetch, so fetch individually
            Optional<ProductInfo> productInfo = productCatalogService.getProductInfo(productId);

            // Only include if we have product info (name is required)
            if (productInfo.isPresent()) {
                PriceInfo priceInfo = prices.get(productId);
                if (priceInfo == null) {
                    throw new IllegalStateException(
                        "Pricing data not available for product: " + productId.value() +
                        ". Ensure price is set in Pricing context.");
                }

                CartArticle cartArticle = buildCartArticle(
                    productId,
                    productInfo.get(),
                    priceInfo,
                    stocks.get(productId)
                );
                result.put(productId, cartArticle);
            }
        }

        return result;
    }

    @Override
    public Optional<CartArticle> getArticleData(ProductId productId) {
        if (productId == null) {
            return Optional.empty();
        }

        // Fetch product info (required - contains name)
        Optional<ProductInfo> productInfo = productCatalogService.getProductInfo(productId);

        if (productInfo.isEmpty()) {
            return Optional.empty();
        }

        // Fetch pricing (required)
        Optional<PriceInfo> priceInfo = pricingService.getPrice(productId);
        if (priceInfo.isEmpty()) {
            throw new IllegalStateException(
                "Pricing data not available for product: " + productId.value() +
                ". Ensure price is set in Pricing context.");
        }

        // Fetch inventory (required - Inventory is the owner of stock data)
        Optional<StockInfo> stockInfo = inventoryService.getStock(productId);
        if (stockInfo.isEmpty()) {
            throw new IllegalStateException(
                "Inventory data not available for product: " + productId.value() +
                ". Ensure stock level is set in Inventory context.");
        }

        return Optional.of(buildCartArticle(
            productId,
            productInfo.get(),
            priceInfo.get(),
            stockInfo.get()
        ));
    }

    /**
     * Builds a CartArticle domain object from multiple OHS data sources.
     */
    private CartArticle buildCartArticle(
            ProductId productId,
            ProductInfo productInfo,
            PriceInfo priceInfo,
            StockInfo stockInfo) {

        String name = productInfo.name();
        Money currentPrice = priceInfo.currentPrice();

        // Use Inventory context as the single source of truth for stock
        int availableStock = stockInfo != null ? stockInfo.availableStock() : 0;
        boolean isAvailable = stockInfo != null && stockInfo.isAvailable();

        return CartArticle.of(productId, name, currentPrice, availableStock, isAvailable);
    }
}
