package de.sample.aiarchitecture.account.application.shared;

/**
 * Port for managing user identity sessions.
 *
 * <p>This is an output port specific to the account bounded context. It abstracts
 * session management (setting/clearing cookies) from the infrastructure layer.
 * Adapters use this port to establish or terminate user sessions without depending
 * on specific session implementations.
 *
 * <p><b>Why in Account Context:</b> Only the account context modifies identity sessions
 * (during login/registration/logout). Other contexts use {@code IdentityProvider} to
 * read the current identity, which was already established by the infrastructure layer.
 *
 * <p><b>Usage in Adapters:</b>
 * <pre>{@code
 * @RestController
 * public class AuthResource {
 *     private final TokenService tokenService;
 *     private final IdentitySession identitySession;
 *
 *     public ResponseEntity<LoginResult> login(...) {
 *         String token = tokenService.generateRegisteredToken(userId, email, roles);
 *         identitySession.setRegisteredIdentity(token);
 *         // ...
 *     }
 *
 *     public ResponseEntity<Void> logout() {
 *         identitySession.clearIdentity();
 *         // ...
 *     }
 * }
 * }</pre>
 *
 * <p><b>Implementation:</b> The infrastructure layer provides the concrete implementation
 * (e.g., JwtIdentitySession) that handles cookie management using the current HTTP response.
 *
 * <p><b>Request Scope:</b> Implementations are typically request-scoped because they need
 * access to the current HTTP response to set cookies.
 *
 * @see TokenService for generating tokens
 * @see de.sample.aiarchitecture.sharedkernel.marker.port.out.IdentityProvider for reading identity
 */
public interface IdentitySession {

  /**
   * Sets the identity cookie for a registered user.
   *
   * <p>This establishes the user's session by storing the token in an HTTP-only cookie.
   * The cookie settings (HttpOnly, Secure, SameSite, expiration) are managed by the
   * infrastructure implementation.
   *
   * @param token the authentication token to store
   */
  void setRegisteredIdentity(String token);

  /**
   * Clears the identity cookie, effectively logging out the user.
   *
   * <p>This terminates the user's session by removing the authentication cookie.
   * After calling this method, subsequent requests will create a new anonymous identity.
   */
  void clearIdentity();
}
