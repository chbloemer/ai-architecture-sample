package de.sample.aiarchitecture.sharedkernel.application.port.security;

import de.sample.aiarchitecture.sharedkernel.domain.common.UserId;
import org.jspecify.annotations.NonNull;

/**
 * Port for validating that a registered user's account still exists.
 *
 * <p>This is used by the security infrastructure to verify that registered
 * users have valid accounts. After an application restart with in-memory storage,
 * JWT tokens may still be valid but the accounts they reference no longer exist.
 *
 * <p>Implementation resides in the account bounded context adapter layer.
 */
public interface RegisteredUserValidator {

  /**
   * Checks if a registered user account exists for the given user ID.
   *
   * @param userId the user ID to check
   * @return true if an account exists for this user ID, false otherwise
   */
  boolean existsForUserId(@NonNull UserId userId);
}
