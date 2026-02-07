package de.sample.aiarchitecture.product.domain.event;

import de.sample.aiarchitecture.product.domain.model.ProductName;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a product's name was changed.
 */
public record ProductNameChanged(
    UUID eventId,
    ProductId productId,
    ProductName oldName,
    ProductName newName,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static ProductNameChanged now(
      final ProductId productId,
      final ProductName oldName,
      final ProductName newName) {
    return new ProductNameChanged(UUID.randomUUID(), productId, oldName, newName, Instant.now(), 1);
  }
}
