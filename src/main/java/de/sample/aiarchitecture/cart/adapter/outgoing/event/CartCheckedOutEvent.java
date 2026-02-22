package de.sample.aiarchitecture.cart.adapter.outgoing.event;

import de.sample.aiarchitecture.cart.domain.event.CartCheckedOut;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.IntegrationEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Integration Event published when a shopping cart is checked out.
 *
 * <p>This adapter-layer DTO is created from the internal {@link CartCheckedOut} domain event by
 * {@link CartCheckedOutEventPublisher} and published for cross-context consumption.
 *
 * @see CartCheckedOut
 * @see CartCheckedOutEventPublisher
 */
public record CartCheckedOutEvent(
    UUID eventId,
    CartId cartId,
    CustomerId customerId,
    Money totalAmount,
    int itemCount,
    List<ItemInfo> items,
    Instant occurredOn,
    int version)
    implements IntegrationEvent {

  /**
   * Lightweight DTO for item information.
   *
   * @param productId the product ID from Shared Kernel
   * @param quantity the quantity
   */
  public record ItemInfo(ProductId productId, int quantity) {}

  /** Creates an integration event from the internal domain event. */
  public static CartCheckedOutEvent from(final CartCheckedOut domainEvent) {
    final List<ItemInfo> items =
        domainEvent.items().stream()
            .map(item -> new ItemInfo(item.productId(), item.quantity()))
            .toList();

    return new CartCheckedOutEvent(
        domainEvent.eventId(),
        domainEvent.cartId(),
        domainEvent.customerId(),
        domainEvent.totalAmount(),
        domainEvent.itemCount(),
        items,
        domainEvent.occurredOn(),
        1);
  }
}
