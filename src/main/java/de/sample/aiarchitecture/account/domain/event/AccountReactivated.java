package de.sample.aiarchitecture.account.domain.event;

import de.sample.aiarchitecture.account.domain.model.AccountId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a suspended account was reactivated.
 */
public record AccountReactivated(
    UUID eventId,
    AccountId accountId,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static AccountReactivated now(final AccountId accountId) {
    return new AccountReactivated(UUID.randomUUID(), accountId, Instant.now(), 1);
  }
}
