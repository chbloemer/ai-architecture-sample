package de.sample.aiarchitecture.inventory.domain.event;

import de.sample.aiarchitecture.inventory.domain.model.StockLevelId;
import de.sample.aiarchitecture.inventory.domain.model.StockQuantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that stock was decreased for a product.
 */
public record StockDecreased(
    UUID eventId,
    StockLevelId stockLevelId,
    ProductId productId,
    StockQuantity removedQuantity,
    StockQuantity newQuantity,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static StockDecreased now(
      final StockLevelId stockLevelId,
      final ProductId productId,
      final StockQuantity removedQuantity,
      final StockQuantity newQuantity) {
    return new StockDecreased(
        UUID.randomUUID(), stockLevelId, productId, removedQuantity, newQuantity, Instant.now(), 1);
  }
}
