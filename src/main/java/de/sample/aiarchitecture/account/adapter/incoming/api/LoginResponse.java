package de.sample.aiarchitecture.account.adapter.incoming.api;

/**
 * Response DTO for login endpoint.
 *
 * @param success whether login was successful
 * @param token the JWT token (only present on success)
 * @param email the user's email (only present on success)
 * @param errorMessage error message (only present on failure)
 */
public record LoginResponse(
    boolean success,
    String token,
    String email,
    String errorMessage) {

  public static LoginResponse success(final String token, final String email) {
    return new LoginResponse(true, token, email, null);
  }

  public static LoginResponse failure(final String errorMessage) {
    return new LoginResponse(false, null, null, errorMessage);
  }
}
