package de.sample.aiarchitecture.cart.events;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.IntegrationEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Integration Event published when a shopping cart is checked out.
 *
 * <p>This event is published for cross-module consumption. Internal domain event {@code
 * CartCheckedOut} is converted to this integration event by {@code CartCheckedOutEventPublisher}.
 */
public record CartCheckedOutEvent(
    UUID eventId,
    UUID cartId,
    String customerId,
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
}
