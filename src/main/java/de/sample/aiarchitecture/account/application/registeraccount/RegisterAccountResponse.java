package de.sample.aiarchitecture.account.application.registeraccount;

import java.util.Set;
import org.jspecify.annotations.NonNull;

/**
 * Response from registering an account.
 *
 * @param accountId the new account's ID
 * @param userId the user's ID (to be used in JWT)
 * @param email the registered email
 * @param roles the user's roles
 */
public record RegisterAccountResponse(
    @NonNull String accountId,
    @NonNull String userId,
    @NonNull String email,
    @NonNull Set<String> roles) {

  /**
   * Creates a successful registration response.
   *
   * @param accountId the account ID
   * @param userId the user ID
   * @param email the email
   * @param roles the roles
   * @return a new response
   */
  public static RegisterAccountResponse of(
      final String accountId,
      final String userId,
      final String email,
      final Set<String> roles) {
    return new RegisterAccountResponse(accountId, userId, email, roles);
  }
}
