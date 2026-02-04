package de.sample.aiarchitecture.cart.adapter.outgoing.product;

import de.sample.aiarchitecture.cart.application.shared.ProductDataPort;
import de.sample.aiarchitecture.product.adapter.incoming.openhost.ProductCatalogService;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adapter that implements Cart's ProductDataPort by delegating to
 * Product context's Open Host Service.
 *
 * <p>This adapter provides product existence and stock validation only.
 * Pricing information is handled by CompositeArticleDataAdapter which
 * delegates to the dedicated Pricing bounded context.
 *
 * <p>This adapter is one place in Cart context that imports from
 * Product context, isolating cross-context coupling to the adapter layer.
 */
@Component
public class ProductDataAdapter implements ProductDataPort {

    private final ProductCatalogService productCatalogService;

    public ProductDataAdapter(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @Override
    public Optional<ProductData> getProductData(ProductId productId, int requestedQuantity) {
        return productCatalogService.getProductInfo(productId)
            .map(info -> new ProductData(
                productId,
                info.availableStock() >= requestedQuantity
            ));
    }
}
