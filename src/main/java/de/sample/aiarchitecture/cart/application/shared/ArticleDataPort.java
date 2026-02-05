package de.sample.aiarchitecture.cart.application.shared;

import de.sample.aiarchitecture.cart.domain.model.CartArticle;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Output port for accessing article data from Cart context.
 *
 * <p>Cart context defines its own port for article data needs. This port is
 * implemented by an outgoing adapter that delegates to the Product and Inventory
 * contexts' Open Host Services.
 *
 * <p><b>Hexagonal Architecture:</b> This is a secondary/driven port that defines what
 * the Cart application layer needs from other contexts (Product for name/price,
 * Inventory for stock information).
 */
public interface ArticleDataPort extends OutputPort {

    /**
     * Retrieves article data for multiple products.
     *
     * @param productIds the collection of product IDs
     * @return a map of product ID to CartArticle for found products
     */
    Map<ProductId, CartArticle> getArticleData(Collection<ProductId> productIds);

    /**
     * Retrieves article data for a single product.
     *
     * @param productId the product ID
     * @return CartArticle if found
     */
    Optional<CartArticle> getArticleData(ProductId productId);
}
