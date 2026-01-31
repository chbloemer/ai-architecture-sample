package de.sample.aiarchitecture.product.adapter.incoming.openhost;

import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.sharedkernel.domain.common.Price;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import de.sample.aiarchitecture.sharedkernel.stereotype.OpenHostService;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Open Host Service for Product Catalog.
 *
 * <p>This is an incoming adapter that exposes Product context capabilities to other
 * bounded contexts. It translates domain objects to DTOs, similar to how REST
 * controllers translate domain objects to JSON.
 *
 * <p>Consuming contexts should NOT use this service directly in their use cases -
 * they should define their own output ports and implement adapters that delegate
 * to this service.
 *
 * <p><b>Placement rationale:</b> This is an incoming adapter because other contexts
 * "call into" Product context through this service. It's parallel to REST controllers
 * which also reside in adapter/incoming/.
 */
@OpenHostService(
    context = "Product Catalog",
    description = "Provides product information (name, price, stock) for other bounded contexts"
)
@Service
public class ProductCatalogService {

    private final ProductRepository productRepository;

    public ProductCatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Product information DTO for cross-context communication.
     */
    public record ProductInfo(
        ProductId productId,
        String name,
        Price price,
        int availableStock
    ) {}

    /**
     * Retrieves product information by ID.
     *
     * @param productId the product ID
     * @return product info if found
     */
    public Optional<ProductInfo> getProductInfo(ProductId productId) {
        return productRepository.findById(productId)
            .map(product -> new ProductInfo(
                productId,
                product.name().value(),
                product.price(),
                product.stock().quantity()
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
        return productRepository.findById(productId)
            .map(product -> product.hasStockFor(quantity))
            .orElse(false);
    }
}
