package de.sample.aiarchitecture.account.application.authenticateaccount;

import java.util.Set;
import org.jspecify.annotations.NonNull;

/**
 * Response from authenticating an account.
 *
 * @param success whether authentication was successful
 * @param userId the user's ID (for JWT generation)
 * @param email the user's email
 * @param roles the user's roles
 * @param errorMessage error message if authentication failed
 */
public record AuthenticateAccountResponse(
    boolean success,
    String userId,
    String email,
    Set<String> roles,
    String errorMessage) {

  /**
   * Creates a successful authentication response.
   *
   * @param userId the user ID
   * @param email the email
   * @param roles the roles
   * @return a successful response
   */
  public static AuthenticateAccountResponse success(
      @NonNull final String userId,
      @NonNull final String email,
      @NonNull final Set<String> roles) {
    return new AuthenticateAccountResponse(true, userId, email, roles, null);
  }

  /**
   * Creates a failed authentication response.
   *
   * @param errorMessage the error message
   * @return a failed response
   */
  public static AuthenticateAccountResponse failure(@NonNull final String errorMessage) {
    return new AuthenticateAccountResponse(false, null, null, null, errorMessage);
  }
}
