package de.sample.aiarchitecture.pricing.domain.event;

import de.sample.aiarchitecture.pricing.domain.model.PriceId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a new price was created for a product.
 */
public record PriceCreated(
    @NonNull UUID eventId,
    @NonNull PriceId priceId,
    @NonNull ProductId productId,
    @NonNull Money price,
    @NonNull Instant effectiveFrom,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static PriceCreated now(
      @NonNull final PriceId priceId,
      @NonNull final ProductId productId,
      @NonNull final Money price,
      @NonNull final Instant effectiveFrom) {
    return new PriceCreated(
        UUID.randomUUID(), priceId, productId, price, effectiveFrom, Instant.now(), 1);
  }
}
