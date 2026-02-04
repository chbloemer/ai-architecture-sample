package de.sample.aiarchitecture.inventory.domain.event;

import de.sample.aiarchitecture.inventory.domain.model.StockLevelId;
import de.sample.aiarchitecture.inventory.domain.model.StockQuantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that stock was increased for a product.
 */
public record StockIncreased(
    @NonNull UUID eventId,
    @NonNull StockLevelId stockLevelId,
    @NonNull ProductId productId,
    @NonNull StockQuantity addedQuantity,
    @NonNull StockQuantity newQuantity,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static StockIncreased now(
      @NonNull final StockLevelId stockLevelId,
      @NonNull final ProductId productId,
      @NonNull final StockQuantity addedQuantity,
      @NonNull final StockQuantity newQuantity) {
    return new StockIncreased(
        UUID.randomUUID(), stockLevelId, productId, addedQuantity, newQuantity, Instant.now(), 1);
  }
}
