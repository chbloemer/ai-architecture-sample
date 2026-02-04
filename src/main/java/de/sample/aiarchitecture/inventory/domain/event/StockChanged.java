package de.sample.aiarchitecture.inventory.domain.event;

import de.sample.aiarchitecture.inventory.domain.model.StockLevelId;
import de.sample.aiarchitecture.inventory.domain.model.StockQuantity;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that stock levels have changed for a product.
 */
public record StockChanged(
    @NonNull UUID eventId,
    @NonNull StockLevelId stockLevelId,
    @NonNull ProductId productId,
    @NonNull StockQuantity previousAvailableQuantity,
    @NonNull StockQuantity newAvailableQuantity,
    @NonNull StockQuantity previousReservedQuantity,
    @NonNull StockQuantity newReservedQuantity,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static StockChanged now(
      @NonNull final StockLevelId stockLevelId,
      @NonNull final ProductId productId,
      @NonNull final StockQuantity previousAvailableQuantity,
      @NonNull final StockQuantity newAvailableQuantity,
      @NonNull final StockQuantity previousReservedQuantity,
      @NonNull final StockQuantity newReservedQuantity) {
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
