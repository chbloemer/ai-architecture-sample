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
 * <p>This event includes the initial price for the product, which is used by the Pricing
 * bounded context to initialize the ProductPrice entity. While pricing is managed by
 * the Pricing context, the initial price is captured at product creation time to ensure
 * pricing data is available immediately.
 */
public record ProductCreated(
    @NonNull UUID eventId,
    @NonNull ProductId productId,
    @NonNull SKU sku,
    @NonNull ProductName name,
    @NonNull Money initialPrice,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static ProductCreated now(
      @NonNull final ProductId productId,
      @NonNull final SKU sku,
      @NonNull final ProductName name,
      @NonNull final Money initialPrice) {
    return new ProductCreated(UUID.randomUUID(), productId, sku, name, initialPrice, Instant.now(), 1);
  }
}
