package de.sample.aiarchitecture.inventory.domain.event;

import de.sample.aiarchitecture.inventory.domain.model.StockLevelId;
import de.sample.aiarchitecture.inventory.domain.model.StockQuantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that stock was reserved for a product.
 */
public record StockReserved(
    UUID eventId,
    StockLevelId stockLevelId,
    ProductId productId,
    StockQuantity reservedQuantity,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static StockReserved now(
      final StockLevelId stockLevelId,
      final ProductId productId,
      final StockQuantity reservedQuantity) {
    return new StockReserved(
        UUID.randomUUID(), stockLevelId, productId, reservedQuantity, Instant.now(), 1);
  }
}
