package de.sample.aiarchitecture.pricing.adapter.incoming.event;

import de.sample.aiarchitecture.pricing.application.shared.ProductPriceRepository;
import de.sample.aiarchitecture.pricing.domain.model.ProductPrice;
import de.sample.aiarchitecture.product.domain.event.ProductCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event consumer that listens for ProductCreated events and creates corresponding
 * price entries in the Pricing bounded context.
 *
 * <p>This consumer handles the cross-context synchronization of pricing data when
 * new products are created. The initial price is included in the ProductCreated event,
 * which this consumer uses to create a ProductPrice entity in the Pricing context.
 *
 * <p><b>Hexagonal Architecture:</b> This is an incoming adapter that receives events
 * from the Product context and coordinates with the Pricing domain.
 */
@Component
public class ProductCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductCreatedEventConsumer.class);

    private final ProductPriceRepository productPriceRepository;

    public ProductCreatedEventConsumer(ProductPriceRepository productPriceRepository) {
        this.productPriceRepository = productPriceRepository;
    }

    /**
     * Handles ProductCreated events by creating a corresponding price entry.
     *
     * <p>This handler executes after the transaction commits successfully, ensuring
     * the product was actually persisted before creating the price entry.
     *
     * @param event the product created event (includes initial price)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductCreated(final ProductCreated event) {
        log.debug("Received ProductCreated event for product: {}", event.productId().value());

        // Check if price already exists for this product
        if (productPriceRepository.findByProductId(event.productId()).isPresent()) {
            log.debug("Price already exists for product: {}, skipping", event.productId().value());
            return;
        }

        // Get the initial price from the event
        var initialPrice = event.initialPrice();

        // Create ProductPrice in Pricing context
        var productPrice = ProductPrice.create(event.productId(), initialPrice);
        productPriceRepository.save(productPrice);

        log.info("Created price entry for new product: {} with initial price: {}",
                event.productId().value(), initialPrice);
    }
}
