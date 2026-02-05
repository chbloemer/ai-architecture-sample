package de.sample.aiarchitecture.product.adapter.incoming.openhost;

import de.sample.aiarchitecture.product.application.getallproducts.GetAllProductsInputPort;
import de.sample.aiarchitecture.product.application.getallproducts.GetAllProductsQuery;
import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdInputPort;
import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdQuery;
import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdResult;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.strategic.OpenHostService;
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
 *
 * <p><b>Note:</b> Product context owns identity (productId, sku) and description (name).
 * Pricing is provided by PricingService. Stock/availability is provided by InventoryService.
 */
@OpenHostService(
    context = "Product Catalog",
    description = "Provides product identity and description (name, SKU) for other bounded contexts. Pricing is provided by PricingService. Stock is provided by InventoryService."
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
     * <p>Contains only identity and description data owned by Product context.
     *
     * <p>Note: Price is provided by PricingService. Stock is provided by InventoryService.
     */
    public record ProductInfo(
        ProductId productId,
        String name,
        String sku
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

        if (!response.found() || response.product() == null) {
            return Optional.empty();
        }

        var product = response.product();
        return Optional.of(new ProductInfo(
            product.productId(),
            product.name(),
            product.sku()
        ));
    }

    /**
     * Retrieves all products in the catalog.
     *
     * @return list of all products with their information
     */
    public List<ProductInfo> getAllProducts() {
        var result = getAllProductsInputPort.execute(new GetAllProductsQuery());

        return result.products().stream()
            .map(product -> new ProductInfo(
                product.productId(),
                product.name(),
                product.sku()
            ))
            .toList();
    }
}
