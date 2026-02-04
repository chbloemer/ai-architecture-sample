package de.sample.aiarchitecture.pricing.domain.event;

import de.sample.aiarchitecture.pricing.domain.model.PriceId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a product's price was changed.
 */
public record PriceChanged(
    @NonNull UUID eventId,
    @NonNull PriceId priceId,
    @NonNull ProductId productId,
    @NonNull Money oldPrice,
    @NonNull Money newPrice,
    @NonNull Instant effectiveFrom,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static PriceChanged now(
      @NonNull final PriceId priceId,
      @NonNull final ProductId productId,
      @NonNull final Money oldPrice,
      @NonNull final Money newPrice,
      @NonNull final Instant effectiveFrom) {
    return new PriceChanged(
        UUID.randomUUID(), priceId, productId, oldPrice, newPrice, effectiveFrom, Instant.now(), 1);
  }
}
