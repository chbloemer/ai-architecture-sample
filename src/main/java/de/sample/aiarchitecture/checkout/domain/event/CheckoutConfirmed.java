package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItem;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain Event indicating that a checkout was confirmed by the customer.
 *
 * <p>This is an internal domain event raised by the {@code CheckoutSession} aggregate when the
 * customer reviews and confirms their order.
 *
 * <p><b>Cross-context communication</b> uses the integration event {@code CheckoutConfirmedEvent}
 * (created by an outgoing event adapter), not this domain event directly.
 *
 * @see de.sample.aiarchitecture.checkout.adapter.outgoing.event.CheckoutConfirmedEvent
 */
public record CheckoutConfirmed(
    UUID eventId,
    CheckoutSessionId sessionId,
    CartId cartId,
    CustomerId customerId,
    Money totalAmount,
    List<LineItemInfo> items,
    Instant occurredOn)
    implements DomainEvent {

  /** Creates a CheckoutConfirmed event from checkout session data. */
  public static CheckoutConfirmed now(
      final CheckoutSessionId sessionId,
      final CartId cartId,
      final CustomerId customerId,
      final Money totalAmount,
      final List<CheckoutLineItem> lineItems) {

    final List<LineItemInfo> itemInfos =
        lineItems.stream()
            .map(item -> new LineItemInfo(item.productId(), item.quantity()))
            .toList();

    return new CheckoutConfirmed(
        UUID.randomUUID(), sessionId, cartId, customerId, totalAmount, itemInfos, Instant.now());
  }

  /**
   * Lightweight DTO for line item information in the event.
   *
   * @param productId the product ID from Shared Kernel
   * @param quantity the quantity
   */
  public record LineItemInfo(ProductId productId, int quantity) {}
}
