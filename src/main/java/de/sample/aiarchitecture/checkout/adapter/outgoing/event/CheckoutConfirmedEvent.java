package de.sample.aiarchitecture.checkout.adapter.outgoing.event;

import de.sample.aiarchitecture.checkout.domain.event.CheckoutConfirmed;
import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.IntegrationEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Integration Event published when a checkout is confirmed.
 *
 * <p>This adapter-layer DTO is created from the internal {@link CheckoutConfirmed} domain event by
 * {@link CheckoutConfirmedEventPublisher} and published for cross-context consumption.
 *
 * @see CheckoutConfirmed
 * @see CheckoutConfirmedEventPublisher
 */
public record CheckoutConfirmedEvent(
    UUID eventId,
    CheckoutSessionId sessionId,
    CartId cartId,
    CustomerId customerId,
    Money totalAmount,
    List<LineItemInfo> items,
    Instant occurredOn,
    int version)
    implements IntegrationEvent {

  /**
   * Lightweight DTO for line item information.
   *
   * @param productId the product ID from Shared Kernel
   * @param quantity the quantity
   */
  public record LineItemInfo(ProductId productId, int quantity) {}

  /** Creates an integration event from the internal domain event. */
  public static CheckoutConfirmedEvent from(final CheckoutConfirmed domainEvent) {
    final List<LineItemInfo> items =
        domainEvent.items().stream()
            .map(item -> new LineItemInfo(item.productId(), item.quantity()))
            .toList();

    return new CheckoutConfirmedEvent(
        domainEvent.eventId(),
        domainEvent.sessionId(),
        domainEvent.cartId(),
        domainEvent.customerId(),
        domainEvent.totalAmount(),
        items,
        domainEvent.occurredOn(),
        1);
  }
}
