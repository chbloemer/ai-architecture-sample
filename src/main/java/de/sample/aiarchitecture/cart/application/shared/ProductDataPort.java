package de.sample.aiarchitecture.cart.application.shared;

import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Optional;

/**
 * Output port for accessing product data from Cart context.
 *
 * <p>Cart context defines its own port for product data needs. This port is
 * implemented by an outgoing adapter that delegates to the Product context's
 * Open Host Service.
 *
 * <p><b>Note:</b> This port only provides product existence and stock validation.
 * Pricing information is obtained through {@link ArticleDataPort} which delegates
 * to the dedicated Pricing bounded context.
 *
 * <p><b>Hexagonal Architecture:</b> This is a secondary/driven port that defines what
 * the Cart application layer needs from the Product context.
 */
public interface ProductDataPort extends OutputPort {

    /**
     * Product data as needed by Cart context for validation.
     *
     * <p>Note: Price is not included here (separation of concerns).
     * Use ArticleDataPort for pricing information.
     */
    record ProductData(
        ProductId productId,
        boolean hasStock
    ) {}

    /**
     * Retrieves product data for validating product existence and stock availability.
     *
     * @param productId the product ID
     * @param requestedQuantity the quantity to check stock for
     * @return product data if found
     */
    Optional<ProductData> getProductData(ProductId productId, int requestedQuantity);
}
