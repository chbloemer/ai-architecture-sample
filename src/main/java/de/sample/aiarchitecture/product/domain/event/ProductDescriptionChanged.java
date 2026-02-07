package de.sample.aiarchitecture.product.domain.event;

import de.sample.aiarchitecture.product.domain.model.ProductDescription;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a product's description was changed.
 */
public record ProductDescriptionChanged(
    UUID eventId,
    ProductId productId,
    ProductDescription oldDescription,
    ProductDescription newDescription,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static ProductDescriptionChanged now(
      final ProductId productId,
      final ProductDescription oldDescription,
      final ProductDescription newDescription) {
    return new ProductDescriptionChanged(
        UUID.randomUUID(), productId, oldDescription, newDescription, Instant.now(), 1);
  }
}
