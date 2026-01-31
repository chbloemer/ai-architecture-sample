package de.sample.aiarchitecture.account.adapter.incoming.api;

/**
 * API result DTO for login endpoint.
 *
 * @param success whether login was successful
 * @param token the JWT token (only present on success)
 * @param email the user's email (only present on success)
 * @param errorMessage error message (only present on failure)
 */
public record LoginApiResult(
    boolean success,
    String token,
    String email,
    String errorMessage) {

  public static LoginApiResult success(final String token, final String email) {
    return new LoginApiResult(true, token, email, null);
  }

  public static LoginApiResult failure(final String errorMessage) {
    return new LoginApiResult(false, null, null, errorMessage);
  }
}
