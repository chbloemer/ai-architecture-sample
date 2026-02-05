package de.sample.aiarchitecture.pricing.domain.event;

import de.sample.aiarchitecture.pricing.domain.model.PriceId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a new price was created for a product.
 */
public record PriceCreated(
    UUID eventId,
    PriceId priceId,
    ProductId productId,
    Money price,
    Instant effectiveFrom,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static PriceCreated now(
      final PriceId priceId,
      final ProductId productId,
      final Money price,
      final Instant effectiveFrom) {
    return new PriceCreated(
        UUID.randomUUID(), priceId, productId, price, effectiveFrom, Instant.now(), 1);
  }
}
