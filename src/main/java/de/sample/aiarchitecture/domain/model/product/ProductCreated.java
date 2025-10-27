package de.sample.aiarchitecture.domain.model.product;

import de.sample.aiarchitecture.domain.model.ddd.DomainEvent;
import de.sample.aiarchitecture.domain.model.shared.ProductId;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a new product was created.
 */
public record ProductCreated(
    @NonNull UUID eventId,
    @NonNull ProductId productId,
    @NonNull SKU sku,
    @NonNull ProductName name,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static ProductCreated now(
      @NonNull final ProductId productId,
      @NonNull final SKU sku,
      @NonNull final ProductName name) {
    return new ProductCreated(UUID.randomUUID(), productId, sku, name, Instant.now(), 1);
  }
}
