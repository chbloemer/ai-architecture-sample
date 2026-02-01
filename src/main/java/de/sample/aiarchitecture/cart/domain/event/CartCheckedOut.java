package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CartItem;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.IntegrationEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Integration Event indicating that a shopping cart was checked out.
 *
 * <p><b>Integration Event:</b> This event crosses bounded context boundaries and represents
 * a public contract between the Cart context and consuming contexts (e.g., Product, Order).
 * Published by Cart context when checkout completes successfully.
 *
 * <p><b>Consumers:</b>
 * <ul>
 *   <li>Product Context - Reduces product stock via {@code ProductStockEventListener}
 *   <li>Order Context - Creates order (future implementation)
 *   <li>Analytics Context - Tracks checkout metrics (future implementation)
 * </ul>
 *
 * <p><b>Cross-Context Integration Pattern:</b> This event uses lightweight DTOs (ItemInfo)
 * instead of full domain objects to avoid bounded context violations. The DTOs contain
 * only the data needed for other contexts to react. Consumers should use an Anti-Corruption
 * Layer to translate this event into their own ubiquitous language.
 *
 * <p><b>Versioning:</b> This event is versioned (currently v1) and must maintain backward
 * compatibility. Schema changes require version increments and proper handling in consumer ACLs.
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
    implements IntegrationEvent {

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
