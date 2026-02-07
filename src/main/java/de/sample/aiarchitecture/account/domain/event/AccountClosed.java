package de.sample.aiarchitecture.account.domain.event;

import de.sample.aiarchitecture.account.domain.model.AccountId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that an account was permanently closed.
 */
public record AccountClosed(
    UUID eventId,
    AccountId accountId,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static AccountClosed now(final AccountId accountId) {
    return new AccountClosed(UUID.randomUUID(), accountId, Instant.now(), 1);
  }
}
