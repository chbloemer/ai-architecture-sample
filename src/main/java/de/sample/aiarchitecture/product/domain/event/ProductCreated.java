package de.sample.aiarchitecture.product.domain.event;

import de.sample.aiarchitecture.product.domain.model.ProductName;
import de.sample.aiarchitecture.product.domain.model.SKU;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a new product was created.
 *
 * <p>This event includes:
 * <ul>
 *   <li>Initial price - for the Pricing bounded context to initialize ProductPrice entity
 *   <li>Initial stock - for the Inventory bounded context to initialize StockLevel entity
 * </ul>
 *
 * <p>While pricing is managed by the Pricing context and stock by the Inventory context,
 * these initial values are captured at product creation time to ensure data is available
 * immediately in the respective contexts.
 */
public record ProductCreated(
    @NonNull UUID eventId,
    @NonNull ProductId productId,
    @NonNull SKU sku,
    @NonNull ProductName name,
    @NonNull Money initialPrice,
    int initialStock,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static ProductCreated now(
      @NonNull final ProductId productId,
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final Money initialPrice,
      final int initialStock) {
    return new ProductCreated(UUID.randomUUID(), productId, sku, name, initialPrice, initialStock, Instant.now(), 1);
  }
}
