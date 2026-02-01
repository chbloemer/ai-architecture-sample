package de.sample.aiarchitecture.checkout.adapter.outgoing.product;

import de.sample.aiarchitecture.checkout.application.shared.ProductInfoPort;
import de.sample.aiarchitecture.product.adapter.incoming.openhost.ProductCatalogService;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adapter that implements Checkout's ProductInfoPort by delegating to
 * Product context's Open Host Service.
 *
 * <p>This adapter is the ONLY place in Checkout context that imports from
 * Product context, isolating cross-context coupling to the adapter layer.
 */
@Component
public class ProductInfoAdapter implements ProductInfoPort {

    private final ProductCatalogService productCatalogService;

    public ProductInfoAdapter(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @Override
    public Optional<String> getProductName(ProductId productId) {
        return productCatalogService.getProductInfo(productId)
            .map(ProductCatalogService.ProductInfo::name);
    }
}
