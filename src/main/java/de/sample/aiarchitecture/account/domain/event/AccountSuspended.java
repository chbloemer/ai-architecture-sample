package de.sample.aiarchitecture.account.domain.event;

import de.sample.aiarchitecture.account.domain.model.AccountId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that an account was suspended.
 */
public record AccountSuspended(
    UUID eventId,
    AccountId accountId,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static AccountSuspended now(final AccountId accountId) {
    return new AccountSuspended(UUID.randomUUID(), accountId, Instant.now(), 1);
  }
}
