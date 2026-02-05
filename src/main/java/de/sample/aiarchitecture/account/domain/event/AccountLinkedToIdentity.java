package de.sample.aiarchitecture.account.domain.event;

import de.sample.aiarchitecture.account.domain.model.AccountId;
import de.sample.aiarchitecture.sharedkernel.domain.model.UserId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event raised when an account is linked to a UserId.
 *
 * <p>This event is raised during registration when the anonymous user's
 * UserId is linked to their new account.
 *
 * <p>This allows other bounded contexts to know that:
 * <ul>
 *   <li>The UserId now represents a registered user</li>
 *   <li>Resources associated with this UserId (like carts) now belong to an account</li>
 * </ul>
 *
 * @param eventId unique identifier for this event instance
 * @param accountId the account that was linked
 * @param userId the UserId that was linked
 * @param occurredOn when the linking occurred
 * @param version event schema version
 */
public record AccountLinkedToIdentity(
    UUID eventId,
    AccountId accountId,
    UserId userId,
    Instant occurredOn,
    int version) implements DomainEvent {

  /**
   * Creates an AccountLinkedToIdentity event with the current timestamp.
   *
   * @param accountId the account that was linked
   * @param userId the UserId that was linked
   * @return a new AccountLinkedToIdentity event
   */
  public static AccountLinkedToIdentity now(final AccountId accountId, final UserId userId) {
    return new AccountLinkedToIdentity(UUID.randomUUID(), accountId, userId, Instant.now(), 1);
  }
}
