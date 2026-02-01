package de.sample.aiarchitecture.checkout.application.shared;

import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Optional;

/**
 * Output port for accessing product information from Checkout context.
 *
 * <p>Checkout context defines its own port for product information needs. This port is
 * implemented by an outgoing adapter that delegates to the Product context's
 * Open Host Service.
 *
 * <p><b>Hexagonal Architecture:</b> This is a secondary/driven port that defines what
 * the Checkout application layer needs from the Product context.
 *
 * <p><b>Note:</b> Checkout only needs product names for line items, so this port
 * provides a minimal interface specific to Checkout's needs.
 */
public interface ProductInfoPort extends OutputPort {

    /**
     * Retrieves the product name.
     *
     * @param productId the product ID
     * @return product name if found
     */
    Optional<String> getProductName(ProductId productId);
}
