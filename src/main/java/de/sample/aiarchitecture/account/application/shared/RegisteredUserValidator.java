package de.sample.aiarchitecture.account.application.shared;

import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort;
import de.sample.aiarchitecture.sharedkernel.domain.model.UserId;

/**
 * Validator for checking if a registered user's account exists.
 *
 * <p>This is used by the security infrastructure to verify that registered
 * users have valid accounts. After an application restart with in-memory storage,
 * JWT tokens may still be valid but the accounts they reference no longer exist.
 *
 * <p>This interface belongs to the account bounded context because only the
 * account context knows about account existence.
 */
public interface RegisteredUserValidator extends OutputPort {

  /**
   * Checks if a registered user account exists for the given user ID.
   *
   * @param userId the user ID to check
   * @return true if an account exists for this user ID, false otherwise
   */
  boolean existsForUserId(UserId userId);
}
