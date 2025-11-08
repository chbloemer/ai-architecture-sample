package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CartItem;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import de.sample.aiarchitecture.sharedkernel.domain.marker.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a shopping cart was checked out.
 *
 * <p><b>Cross-Context Integration:</b> This event uses lightweight DTOs (ItemInfo)
 * instead of full domain objects to avoid bounded context violations. The DTOs contain
 * only the data needed for other contexts to react (e.g., Product context reducing stock).
 *
 * <p><b>DDD Pattern:</b> Events should be self-contained and not expose domain objects
 * from one bounded context to another. This ensures proper context isolation.
 */
public record CartCheckedOut(
    @NonNull UUID eventId,
    @NonNull CartId cartId,
    @NonNull CustomerId customerId,
    @NonNull Money totalAmount,
    int itemCount,
    @NonNull List<ItemInfo> items,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  /**
   * Creates a CartCheckedOut event from cart domain objects.
   *
   * <p>Converts CartItem domain objects to lightweight ItemInfo DTOs.
   */
  public static CartCheckedOut now(
      @NonNull final CartId cartId,
      @NonNull final CustomerId customerId,
      @NonNull final Money totalAmount,
      final int itemCount,
      @NonNull final List<CartItem> cartItems) {

    // Convert domain objects to DTOs for cross-context communication
    final List<ItemInfo> itemInfos = cartItems.stream()
        .map(item -> new ItemInfo(item.productId(), item.quantity().value()))
        .toList();

    return new CartCheckedOut(
        UUID.randomUUID(),
        cartId,
        customerId,
        totalAmount,
        itemCount,
        itemInfos,
        Instant.now(),
        1
    );
  }

  /**
   * Lightweight DTO for item information in the event.
   *
   * <p>Uses only Shared Kernel types (ProductId) to avoid bounded context violations.
   *
   * @param productId the product ID from Shared Kernel
   * @param quantity the quantity (primitive type)
   */
  public record ItemInfo(
      @NonNull ProductId productId,
      int quantity
  ) {}
}
