package de.sample.aiarchitecture.cart.domain.event;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.marker.DomainEvent;

import de.sample.aiarchitecture.sharedkernel.domain.marker.DomainEvent;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
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
