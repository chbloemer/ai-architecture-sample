package de.sample.aiarchitecture.inventory.domain.event;

import de.sample.aiarchitecture.inventory.domain.model.StockLevelId;
import de.sample.aiarchitecture.inventory.domain.model.StockQuantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that previously reserved stock was released.
 */
public record StockReleased(
    UUID eventId,
    StockLevelId stockLevelId,
    ProductId productId,
    StockQuantity releasedQuantity,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static StockReleased now(
      final StockLevelId stockLevelId,
      final ProductId productId,
      final StockQuantity releasedQuantity) {
    return new StockReleased(
        UUID.randomUUID(), stockLevelId, productId, releasedQuantity, Instant.now(), 1);
  }
}
