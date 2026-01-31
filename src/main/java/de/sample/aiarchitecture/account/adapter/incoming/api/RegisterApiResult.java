package de.sample.aiarchitecture.account.adapter.incoming.api;

/**
 * API result DTO for register endpoint.
 *
 * @param success whether registration was successful
 * @param token the JWT token (only present on success)
 * @param email the user's email (only present on success)
 * @param errorMessage error message (only present on failure)
 */
public record RegisterApiResult(
    boolean success,
    String token,
    String email,
    String errorMessage) {

  public static RegisterApiResult success(final String token, final String email) {
    return new RegisterApiResult(true, token, email, null);
  }

  public static RegisterApiResult failure(final String errorMessage) {
    return new RegisterApiResult(false, null, null, errorMessage);
  }
}
