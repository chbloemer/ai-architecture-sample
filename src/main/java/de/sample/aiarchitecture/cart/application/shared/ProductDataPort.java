package de.sample.aiarchitecture.cart.application.shared;

import de.sample.aiarchitecture.sharedkernel.application.port.OutputPort;
import de.sample.aiarchitecture.sharedkernel.domain.common.Price;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import java.util.Optional;

/**
 * Output port for accessing product data from Cart context.
 *
 * <p>Cart context defines its own port for product data needs. This port is
 * implemented by an outgoing adapter that delegates to the Product context's
 * Open Host Service.
 *
 * <p><b>Hexagonal Architecture:</b> This is a secondary/driven port that defines what
 * the Cart application layer needs from the Product context.
 */
public interface ProductDataPort extends OutputPort {

    /**
     * Product data as needed by Cart context.
     */
    record ProductData(
        ProductId productId,
        Price price,
        boolean hasStock
    ) {}

    /**
     * Retrieves product data for adding to cart.
     *
     * @param productId the product ID
     * @param requestedQuantity the quantity to check stock for
     * @return product data if found
     */
    Optional<ProductData> getProductData(ProductId productId, int requestedQuantity);
}
