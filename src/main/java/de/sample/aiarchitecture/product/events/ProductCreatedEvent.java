package de.sample.aiarchitecture.product.events;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when a new product is created.
 *
 * <p>This event is published for cross-module consumption. Internal domain event {@code
 * ProductCreated} is converted to this integration event by {@code ProductCreatedEventPublisher}.
 *
 * <p>Consumers: Pricing context (creates initial price), Inventory context (creates stock level).
 */
public record ProductCreatedEvent(
    UUID eventId,
    ProductId productId,
    Money initialPrice,
    int initialStock,
    Instant occurredOn,
    int version)
    implements IntegrationEvent {

  /** Creates a new event from product creation data. */
  public static ProductCreatedEvent now(ProductId productId, Money initialPrice, int initialStock) {
    return new ProductCreatedEvent(
        UUID.randomUUID(), productId, initialPrice, initialStock, Instant.now(), 1);
  }
}
