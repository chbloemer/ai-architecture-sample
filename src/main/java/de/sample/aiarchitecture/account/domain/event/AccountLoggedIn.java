package de.sample.aiarchitecture.account.domain.event;

import de.sample.aiarchitecture.account.domain.model.AccountId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that a user logged in to their account.
 */
public record AccountLoggedIn(
    UUID eventId,
    AccountId accountId,
    Instant occurredOn,
    int version)
    implements DomainEvent {

  public static AccountLoggedIn now(final AccountId accountId) {
    return new AccountLoggedIn(UUID.randomUUID(), accountId, Instant.now(), 1);
  }
}
