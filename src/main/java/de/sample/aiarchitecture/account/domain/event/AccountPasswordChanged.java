package de.sample.aiarchitecture.account.domain.event;

import de.sample.aiarchitecture.account.domain.model.AccountId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that an account's password was changed.
 */
public record AccountPasswordChanged(
    UUID eventId,
    AccountId accountId,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static AccountPasswordChanged now(final AccountId accountId) {
    return new AccountPasswordChanged(UUID.randomUUID(), accountId, Instant.now(), 1);
  }
}
