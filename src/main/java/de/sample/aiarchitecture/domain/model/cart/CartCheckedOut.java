package de.sample.aiarchitecture.domain.model.cart;

import de.sample.aiarchitecture.domain.model.ddd.DomainEvent;
import de.sample.aiarchitecture.domain.model.shared.Money;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Domain Event indicating that a shopping cart was checked out.
 */
public record CartCheckedOut(
    @NonNull UUID eventId,
    @NonNull CartId cartId,
    @NonNull CustomerId customerId,
    @NonNull Money totalAmount,
    int itemCount,
    @NonNull Instant occurredOn,
    int version)
    implements DomainEvent {

  public static CartCheckedOut now(
      @NonNull final CartId cartId,
      @NonNull final CustomerId customerId,
      @NonNull final Money totalAmount,
      final int itemCount) {
    return new CartCheckedOut(UUID.randomUUID(), cartId, customerId, totalAmount, itemCount, Instant.now(), 1);
  }
}
