package de.sample.aiarchitecture.checkout.events;

import de.sample.aiarchitecture.cart.events.CartCompletionTrigger;
import de.sample.aiarchitecture.inventory.events.StockReductionTrigger;
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
 *
 * <p>Implements consumer-defined trigger interfaces (Interface Inversion pattern) so that consuming
 * modules listen to their own interfaces and avoid depending on the Checkout module:
 *
 * <ul>
 *   <li>{@link CartCompletionTrigger} — triggers cart completion in the Cart module
 *   <li>{@link StockReductionTrigger} — triggers stock reduction in the Inventory module
 * </ul>
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
    implements IntegrationEvent, CartCompletionTrigger, StockReductionTrigger {

  /**
   * Lightweight DTO for line item information.
   *
   * @param productId the product ID from Shared Kernel
   * @param quantity the quantity
   */
  public record LineItemInfo(ProductId productId, int quantity) {}

  @Override
  public List<OrderLineItem> orderLineItems() {
    return items.stream()
        .map(item -> new OrderLineItem(item.productId(), item.quantity()))
        .toList();
  }
}
