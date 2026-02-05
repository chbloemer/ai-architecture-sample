package de.sample.aiarchitecture.checkout.domain.event;

import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItem;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.IntegrationEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Integration Event indicating that a checkout was confirmed by the customer.
 *
 * <p>This event is raised when a customer reviews and confirms their order from the review step.
 * As an integration event, it is published across bounded contexts to trigger order creation
 * and payment processing in other contexts.
 *
 * <p><b>Consumers:</b>
 * <ul>
 *   <li>Order context - to create an Order from the confirmed checkout</li>
 *   <li>Payment context - to initiate payment processing</li>
 *   <li>Product context - to reduce product availability</li>
 * </ul>
 *
 * <p><b>Cross-Context Integration Pattern:</b> This event uses lightweight DTOs (LineItemInfo)
 * instead of full domain objects to avoid bounded context violations. Consumers should use an
 * Anti-Corruption Layer to translate this event into their own ubiquitous language.
 */
public record CheckoutConfirmed(
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
   * Creates a CheckoutConfirmed event from checkout session data.
   *
   * <p>Converts CheckoutLineItem domain objects to lightweight LineItemInfo DTOs.
   */
  public static CheckoutConfirmed now(
      final CheckoutSessionId sessionId,
      final CartId cartId,
      final CustomerId customerId,
      final Money totalAmount,
      final List<CheckoutLineItem> lineItems) {

    // Convert domain objects to DTOs for cross-context communication
    final List<LineItemInfo> itemInfos = lineItems.stream()
        .map(item -> new LineItemInfo(item.productId(), item.quantity()))
        .toList();

    return new CheckoutConfirmed(
        UUID.randomUUID(), sessionId, cartId, customerId, totalAmount, itemInfos, Instant.now(), 1);
  }

  /**
   * Lightweight DTO for line item information in the event.
   *
   * <p>Uses only Shared Kernel types (ProductId) to avoid bounded context violations.
   *
   * @param productId the product ID from Shared Kernel
   * @param quantity the quantity (primitive type)
   */
  public record LineItemInfo(
      ProductId productId,
      int quantity
  ) {}
}
