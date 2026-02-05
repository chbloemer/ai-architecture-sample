package de.sample.aiarchitecture.pricing.domain.event;

import de.sample.aiarchitecture.pricing.domain.model.PriceId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a product's price was changed.
 */
public record PriceChanged(
    UUID eventId,
    PriceId priceId,
    ProductId productId,
    Money oldPrice,
    Money newPrice,
    Instant effectiveFrom,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static PriceChanged now(
      final PriceId priceId,
      final ProductId productId,
      final Money oldPrice,
      final Money newPrice,
      final Instant effectiveFrom) {
    return new PriceChanged(
        UUID.randomUUID(), priceId, productId, oldPrice, newPrice, effectiveFrom, Instant.now(), 1);
  }
}
