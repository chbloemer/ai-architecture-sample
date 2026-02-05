package de.sample.aiarchitecture.inventory.domain.event;

import de.sample.aiarchitecture.inventory.domain.model.StockLevelId;
import de.sample.aiarchitecture.inventory.domain.model.StockQuantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that stock levels have changed for a product.
 */
public record StockChanged(
    UUID eventId,
    StockLevelId stockLevelId,
    ProductId productId,
    StockQuantity previousAvailableQuantity,
    StockQuantity newAvailableQuantity,
    StockQuantity previousReservedQuantity,
    StockQuantity newReservedQuantity,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static StockChanged now(
      final StockLevelId stockLevelId,
      final ProductId productId,
      final StockQuantity previousAvailableQuantity,
      final StockQuantity newAvailableQuantity,
      final StockQuantity previousReservedQuantity,
      final StockQuantity newReservedQuantity) {
    return new StockChanged(
        UUID.randomUUID(),
        stockLevelId,
        productId,
        previousAvailableQuantity,
        newAvailableQuantity,
        previousReservedQuantity,
        newReservedQuantity,
        Instant.now(),
        1);
  }
}
