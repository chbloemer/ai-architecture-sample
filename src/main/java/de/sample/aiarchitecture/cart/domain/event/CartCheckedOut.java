package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CartItem;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain Event indicating that a shopping cart was checked out.
 *
 * <p>This is an internal domain event raised by the {@code ShoppingCart} aggregate when checkout
 * completes. It captures a snapshot of cart data at checkout time.
 *
 * <p><b>Cross-context communication</b> uses the integration event {@code CartCheckedOutEvent}
 * (created by an outgoing event adapter), not this domain event directly.
 *
 * @see de.sample.aiarchitecture.cart.adapter.outgoing.event.CartCheckedOutEvent
 */
public record CartCheckedOut(
    UUID eventId,
    CartId cartId,
    CustomerId customerId,
    Money totalAmount,
    int itemCount,
    List<ItemInfo> items,
    Instant occurredOn)
    implements DomainEvent {

  /** Creates a CartCheckedOut event from cart domain objects. */
  public static CartCheckedOut now(
      final CartId cartId,
      final CustomerId customerId,
      final Money totalAmount,
      final int itemCount,
      final List<CartItem> cartItems) {

    final List<ItemInfo> itemInfos =
        cartItems.stream()
            .map(item -> new ItemInfo(item.productId(), item.quantity().value()))
            .toList();

    return new CartCheckedOut(
        UUID.randomUUID(), cartId, customerId, totalAmount, itemCount, itemInfos, Instant.now());
  }

  /**
   * Lightweight DTO for item information in the event.
   *
   * @param productId the product ID from Shared Kernel
   * @param quantity the quantity
   */
  public record ItemInfo(ProductId productId, int quantity) {}
}
