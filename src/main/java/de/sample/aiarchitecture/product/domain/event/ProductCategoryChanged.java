package de.sample.aiarchitecture.product.domain.event;

import de.sample.aiarchitecture.product.domain.model.Category;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a product's category was changed.
 */
public record ProductCategoryChanged(
    UUID eventId,
    ProductId productId,
    Category oldCategory,
    Category newCategory,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static ProductCategoryChanged now(
      final ProductId productId,
      final Category oldCategory,
      final Category newCategory) {
    return new ProductCategoryChanged(
        UUID.randomUUID(), productId, oldCategory, newCategory, Instant.now(), 1);
  }
}
