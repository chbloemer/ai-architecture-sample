package de.sample.aiarchitecture.inventory.domain.event;

import de.sample.aiarchitecture.inventory.domain.model.StockLevelId;
import de.sample.aiarchitecture.inventory.domain.model.StockQuantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that stock was reserved for a product.
 */
public record StockReserved(
    @NonNull UUID eventId,
    @NonNull StockLevelId stockLevelId,
    @NonNull ProductId productId,
    @NonNull StockQuantity reservedQuantity,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static StockReserved now(
      @NonNull final StockLevelId stockLevelId,
      @NonNull final ProductId productId,
      @NonNull final StockQuantity reservedQuantity) {
    return new StockReserved(
        UUID.randomUUID(), stockLevelId, productId, reservedQuantity, Instant.now(), 1);
  }
}
