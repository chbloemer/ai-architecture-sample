package de.sample.aiarchitecture.sharedkernel.application.port.security;

import de.sample.aiarchitecture.sharedkernel.domain.common.UserId;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;

/**
 * Port for JWT token operations.
 *
 * <p>This interface defines the contract for creating, validating, and parsing
 * JWT tokens. The implementation resides in the infrastructure layer.
 *
 * <p><b>Token Types:</b>
 * <ul>
 *   <li><b>Anonymous Token</b> - Created for first-time visitors, contains only UserId</li>
 *   <li><b>Registered Token</b> - Created after login/registration, contains UserId, email, and roles</li>
 * </ul>
 *
 * <p><b>Token Claims:</b>
 * <pre>
 * Anonymous:
 * {
 *   "sub": "anon-550e8400-e29b-41d4-a716-446655440000",
 *   "type": "anonymous",
 *   "iat": 1706688000,
 *   "exp": 1707292800
 * }
 *
 * Registered:
 * {
 *   "sub": "550e8400-e29b-41d4-a716-446655440000",
 *   "type": "registered",
 *   "email": "user@example.com",
 *   "roles": ["CUSTOMER"],
 *   "iat": 1706688000,
 *   "exp": 1707292800
 * }
 * </pre>
 */
public interface TokenService {

  /**
   * Generates a JWT token for an anonymous user.
   *
   * <p>The token will contain:
   * <ul>
   *   <li>subject (sub): the anonymous UserId</li>
   *   <li>type: "anonymous"</li>
   *   <li>issued at (iat): current timestamp</li>
   *   <li>expiration (exp): based on anonymous-expiration-days config</li>
   * </ul>
   *
   * @param userId the anonymous user's ID
   * @return the generated JWT token string
   */
  @NonNull
  String generateAnonymousToken(@NonNull UserId userId);

  /**
   * Generates a JWT token for a registered user.
   *
   * <p>The token will contain:
   * <ul>
   *   <li>subject (sub): the registered UserId</li>
   *   <li>type: "registered"</li>
   *   <li>email: the user's email</li>
   *   <li>roles: the user's roles</li>
   *   <li>issued at (iat): current timestamp</li>
   *   <li>expiration (exp): based on registered-expiration-days config</li>
   * </ul>
   *
   * @param userId the registered user's ID
   * @param email the user's email address
   * @param roles the user's roles
   * @return the generated JWT token string
   */
  @NonNull
  String generateRegisteredToken(
      @NonNull UserId userId,
      @NonNull String email,
      @NonNull Set<String> roles);

  /**
   * Validates and parses a JWT token.
   *
   * <p>This method will:
   * <ol>
   *   <li>Verify the token signature</li>
   *   <li>Check the token has not expired</li>
   *   <li>Extract and return the Identity from claims</li>
   * </ol>
   *
   * @param token the JWT token string
   * @return the parsed Identity if valid, empty if invalid or expired
   */
  @NonNull
  Optional<Identity> validateAndParse(@NonNull String token);

  /**
   * Checks if a token is expired.
   *
   * @param token the JWT token string
   * @return true if the token is expired
   */
  boolean isExpired(@NonNull String token);
}
