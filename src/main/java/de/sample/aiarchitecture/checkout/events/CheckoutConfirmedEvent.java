package de.sample.aiarchitecture.checkout.events;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.IntegrationEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Integration Event published when a checkout is confirmed.
 *
 * <p>This event is published for cross-module consumption. Internal domain event {@code
 * CheckoutConfirmed} is converted to this integration event by {@code
 * CheckoutConfirmedEventPublisher}.
 */
public record CheckoutConfirmedEvent(
    UUID eventId,
    String sessionId,
    String cartId,
    String customerId,
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
}
