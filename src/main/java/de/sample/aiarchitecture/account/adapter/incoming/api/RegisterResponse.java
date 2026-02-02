package de.sample.aiarchitecture.account.adapter.incoming.api;

/**
 * API result DTO for register endpoint.
 *
 * @param success whether registration was successful
 * @param token the JWT token (only present on success)
 * @param email the user's email (only present on success)
 * @param errorMessage error message (only present on failure)
 */
public record RegisterResponse(
    boolean success,
    String token,
    String email,
    String errorMessage) {

  public static RegisterResponse success(final String token, final String email) {
    return new RegisterResponse(true, token, email, null);
  }

  public static RegisterResponse failure(final String errorMessage) {
    return new RegisterResponse(false, null, null, errorMessage);
  }
}
