package de.sample.aiarchitecture.product.adapter.incoming.openhost;

import de.sample.aiarchitecture.product.application.getallproducts.GetAllProductsInputPort;
import de.sample.aiarchitecture.product.application.getallproducts.GetAllProductsQuery;
import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdInputPort;
import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdQuery;
import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdResult;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.strategic.OpenHostService;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Open Host Service for Product Catalog.
 *
 * <p>This is an incoming adapter that exposes Product context capabilities to other
 * bounded contexts. It delegates to use cases (input ports) and translates responses
 * to OHS DTOs, similar to how REST controllers work.
 *
 * <p>Consuming contexts should NOT use this service directly in their use cases -
 * they should define their own output ports and implement adapters that delegate
 * to this service.
 *
 * <p><b>Hexagonal Architecture:</b> As an incoming adapter, this service calls
 * input ports (use cases), NOT output ports (repositories) directly.
 *
 * <p><b>Placement rationale:</b> This is an incoming adapter because other contexts
 * "call into" Product context through this service. It's parallel to REST controllers
 * which also reside in adapter/incoming/.
 */
@OpenHostService(
    context = "Product Catalog",
    description = "Provides product information (name, SKU, stock) for other bounded contexts. Pricing is provided by PricingService."
)
@Service
public class ProductCatalogService {

    private final GetProductByIdInputPort getProductByIdInputPort;
    private final GetAllProductsInputPort getAllProductsInputPort;

    public ProductCatalogService(
            GetProductByIdInputPort getProductByIdInputPort,
            GetAllProductsInputPort getAllProductsInputPort) {
        this.getProductByIdInputPort = getProductByIdInputPort;
        this.getAllProductsInputPort = getAllProductsInputPort;
    }

    /**
     * Product information DTO for cross-context communication.
     *
     * <p>Note: Price is no longer provided by this service (separation of concerns).
     * Use PricingService to get current prices.
     */
    public record ProductInfo(
        ProductId productId,
        String name,
        String sku,
        int availableStock
    ) {}

    /**
     * Retrieves product information by ID.
     *
     * @param productId the product ID
     * @return product info if found
     */
    public Optional<ProductInfo> getProductInfo(ProductId productId) {
        GetProductByIdResult response = getProductByIdInputPort.execute(
            new GetProductByIdQuery(productId.value()));

        if (!response.found()) {
            return Optional.empty();
        }

        return Optional.of(new ProductInfo(
            productId,
            response.name(),
            response.sku(),
            response.stockQuantity()
        ));
    }

    /**
     * Checks if sufficient stock is available.
     *
     * @param productId the product ID
     * @param quantity the requested quantity
     * @return true if sufficient stock is available
     */
    public boolean hasStock(ProductId productId, int quantity) {
        GetProductByIdResult response = getProductByIdInputPort.execute(
            new GetProductByIdQuery(productId.value()));

        return response.found() && response.stockQuantity() >= quantity;
    }

    /**
     * Retrieves all products in the catalog.
     *
     * @return list of all products with their information
     */
    public List<ProductInfo> getAllProducts() {
        var result = getAllProductsInputPort.execute(new GetAllProductsQuery());

        return result.products().stream()
            .map(summary -> new ProductInfo(
                ProductId.of(summary.productId()),
                summary.name(),
                summary.sku(),
                summary.stockQuantity()
            ))
            .toList();
    }

    // ==================== Deprecated Pricing Initialization Methods ====================
    // These methods are for transitional pricing initialization only.
    // Once pricing is fully managed by the Pricing context, these will be removed.

    /**
     * Product information with initial price for pricing context initialization.
     *
     * @deprecated This is a temporary migration DTO. Pricing will be fully managed
     *     by the Pricing bounded context in the future.
     */
    @Deprecated(forRemoval = true)
    public record ProductInfoWithPrice(
        ProductId productId,
        Money initialPrice
    ) {}

    /**
     * Retrieves all products with their initial prices for pricing initialization.
     *
     * <p>This method is used ONLY for initializing the Pricing context's data from
     * existing Product data during the migration period. It should NOT be used for
     * ongoing price retrieval - use PricingService for that.
     *
     * @return list of all products with their initial prices
     * @deprecated This is a temporary migration method. Once pricing is fully managed
     *     by the Pricing context, this method will be removed.
     */
    @Deprecated(forRemoval = true)
    public List<ProductInfoWithPrice> getAllProductsWithInitialPrice() {
        var result = getAllProductsInputPort.execute(new GetAllProductsQuery());

        return result.products().stream()
            .map(summary -> new ProductInfoWithPrice(
                ProductId.of(summary.productId()),
                Money.of(summary.priceAmount(), Currency.getInstance(summary.priceCurrency()))
            ))
            .toList();
    }
}
