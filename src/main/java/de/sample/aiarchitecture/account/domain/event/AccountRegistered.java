package de.sample.aiarchitecture.account.domain.event;

import de.sample.aiarchitecture.account.domain.model.AccountId;
import de.sample.aiarchitecture.account.domain.model.Email;
import de.sample.aiarchitecture.sharedkernel.domain.model.UserId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event raised when a new account is registered.
 *
 * <p>This event is raised during account creation and can be used by:
 * <ul>
 *   <li>Email service to send welcome email</li>
 *   <li>Analytics to track registrations</li>
 *   <li>Other bounded contexts that need to know about new users</li>
 * </ul>
 *
 * @param eventId unique identifier for this event instance
 * @param accountId the new account's ID
 * @param email the user's email address
 * @param linkedUserId the UserId linked to this account
 * @param occurredOn when the registration occurred
 * @param version event schema version
 */
public record AccountRegistered(
    UUID eventId,
    AccountId accountId,
    Email email,
    UserId linkedUserId,
    Instant occurredOn,
    int version) implements DomainEvent {

  /**
   * Creates an AccountRegistered event with the current timestamp.
   *
   * @param accountId the new account's ID
   * @param email the user's email address
   * @param linkedUserId the UserId linked to this account
   * @return a new AccountRegistered event
   */
  public static AccountRegistered now(
      final AccountId accountId,
      final Email email,
      final UserId linkedUserId) {
    return new AccountRegistered(UUID.randomUUID(), accountId, email, linkedUserId, Instant.now(), 1);
  }
}
