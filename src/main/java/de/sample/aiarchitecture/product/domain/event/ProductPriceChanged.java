package de.sample.aiarchitecture.product.domain.event;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import de.sample.aiarchitecture.sharedkernel.domain.model.Price;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a product's price was changed.
 */
public record ProductPriceChanged(
    @NonNull UUID eventId,
    @NonNull ProductId productId,
    @NonNull Price oldPrice,
    @NonNull Price newPrice,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static ProductPriceChanged now(
      @NonNull final ProductId productId,
      @NonNull final Price oldPrice,
      @NonNull final Price newPrice) {
    return new ProductPriceChanged(UUID.randomUUID(), productId, oldPrice, newPrice, Instant.now(), 1);
  }
}
