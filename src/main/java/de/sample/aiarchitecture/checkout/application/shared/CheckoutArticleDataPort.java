package de.sample.aiarchitecture.checkout.application.shared;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort;
import java.util.Collection;
import java.util.Map;

/**
 * Output port for accessing article data during checkout.
 *
 * <p>This port allows checkout use cases to fetch current article information
 * including pricing and availability. It provides a bulk operation to efficiently
 * retrieve data for multiple products at once.
 *
 * <p><b>Hexagonal Architecture:</b> This is a secondary/driven port that defines what
 * the Checkout application layer needs from external article/product data sources.
 */
public interface CheckoutArticleDataPort extends OutputPort {

    /**
     * Retrieves article data for a collection of product IDs.
     *
     * @param productIds the collection of product IDs to fetch data for
     * @return a map from ProductId to ArticleData for all found products;
     *         products not found will not be included in the map
     */
    Map<ProductId, ArticleData> getArticleData(Collection<ProductId> productIds);

    /**
     * Article data for checkout operations.
     *
     * <p>Contains current pricing and availability information for an article.
     *
     * @param productId the product identifier
     * @param name the product name
     * @param currentPrice the current price of the product
     * @param availableStock the number of units available in stock
     * @param isAvailable whether the product is available for purchase
     */
    record ArticleData(
            ProductId productId,
            String name,
            Money currentPrice,
            int availableStock,
            boolean isAvailable) {}
}
