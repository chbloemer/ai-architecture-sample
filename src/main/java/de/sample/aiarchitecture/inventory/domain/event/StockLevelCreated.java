package de.sample.aiarchitecture.inventory.domain.event;

import de.sample.aiarchitecture.inventory.domain.model.StockLevelId;
import de.sample.aiarchitecture.inventory.domain.model.StockQuantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a new stock level was created for a product.
 */
public record StockLevelCreated(
    UUID eventId,
    StockLevelId stockLevelId,
    ProductId productId,
    StockQuantity quantity,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static StockLevelCreated now(
      final StockLevelId stockLevelId,
      final ProductId productId,
      final StockQuantity quantity) {
    return new StockLevelCreated(
        UUID.randomUUID(), stockLevelId, productId, quantity, Instant.now(), 1);
  }
}
