package de.sample.aiarchitecture.account.domain.event;

import de.sample.aiarchitecture.account.domain.model.AccountId;
import de.sample.aiarchitecture.sharedkernel.domain.common.UserId;
import de.sample.aiarchitecture.sharedkernel.domain.marker.DomainEvent;
import java.time.Instant;
import org.jspecify.annotations.NonNull;

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
 * @param accountId the account that was linked
 * @param userId the UserId that was linked
 * @param occurredAt when the linking occurred
 */
public record AccountLinkedToIdentity(
    @NonNull AccountId accountId,
    @NonNull UserId userId,
    @NonNull Instant occurredAt) implements DomainEvent {

  /**
   * Creates an AccountLinkedToIdentity event with the current timestamp.
   *
   * @param accountId the account that was linked
   * @param userId the UserId that was linked
   * @return a new AccountLinkedToIdentity event
   */
  public static AccountLinkedToIdentity now(final AccountId accountId, final UserId userId) {
    return new AccountLinkedToIdentity(accountId, userId, Instant.now());
  }
}
