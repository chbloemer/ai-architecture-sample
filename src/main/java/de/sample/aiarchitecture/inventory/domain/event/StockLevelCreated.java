package de.sample.aiarchitecture.inventory.domain.event;

import de.sample.aiarchitecture.inventory.domain.model.StockLevelId;
import de.sample.aiarchitecture.inventory.domain.model.StockQuantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a new stock level was created for a product.
 */
public record StockLevelCreated(
    @NonNull UUID eventId,
    @NonNull StockLevelId stockLevelId,
    @NonNull ProductId productId,
    @NonNull StockQuantity quantity,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static StockLevelCreated now(
      @NonNull final StockLevelId stockLevelId,
      @NonNull final ProductId productId,
      @NonNull final StockQuantity quantity) {
    return new StockLevelCreated(
        UUID.randomUUID(), stockLevelId, productId, quantity, Instant.now(), 1);
  }
}
