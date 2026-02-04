package de.sample.aiarchitecture.inventory.domain.event;

import de.sample.aiarchitecture.inventory.domain.model.StockLevelId;
import de.sample.aiarchitecture.inventory.domain.model.StockQuantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that stock was decreased for a product.
 */
public record StockDecreased(
    @NonNull UUID eventId,
    @NonNull StockLevelId stockLevelId,
    @NonNull ProductId productId,
    @NonNull StockQuantity removedQuantity,
    @NonNull StockQuantity newQuantity,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static StockDecreased now(
      @NonNull final StockLevelId stockLevelId,
      @NonNull final ProductId productId,
      @NonNull final StockQuantity removedQuantity,
      @NonNull final StockQuantity newQuantity) {
    return new StockDecreased(
        UUID.randomUUID(), stockLevelId, productId, removedQuantity, newQuantity, Instant.now(), 1);
  }
}
