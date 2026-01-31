package de.sample.aiarchitecture.sharedkernel.application.port.security;

import de.sample.aiarchitecture.sharedkernel.domain.common.UserId;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;

/**
 * Represents the current user's identity extracted from their JWT token.
 *
 * <p>This is an immutable record that carries identity information throughout
 * a request. It is available via {@link IdentityProvider#getCurrentIdentity()}.
 *
 * <p>Identity can be either:
 * <ul>
 *   <li><b>Anonymous</b> - User has a UserId but no account (soft identity)</li>
 *   <li><b>Registered</b> - User has a UserId linked to an account (hard identity)</li>
 * </ul>
 *
 * <p>The UserId remains the same when an anonymous user registers, ensuring
 * cart continuity.
 *
 * @param userId the user's unique identifier (always present)
 * @param type the identity type (ANONYMOUS or REGISTERED)
 * @param email the user's email (only present for registered users)
 * @param roles the user's roles (typically empty for anonymous, contains CUSTOMER for registered)
 */
public record Identity(
    @NonNull UserId userId,
    @NonNull IdentityType type,
    @NonNull Optional<String> email,
    @NonNull Set<String> roles) {

  /**
   * Default role for registered customers.
   */
  public static final String ROLE_CUSTOMER = "CUSTOMER";

  public Identity {
    if (userId == null) {
      throw new IllegalArgumentException("UserId cannot be null");
    }
    if (type == null) {
      throw new IllegalArgumentException("IdentityType cannot be null");
    }
    if (email == null) {
      email = Optional.empty();
    }
    if (roles == null) {
      roles = Set.of();
    }
  }

  /**
   * Creates an anonymous identity.
   *
   * @param userId the anonymous user's ID
   * @return a new anonymous identity
   */
  public static Identity anonymous(@NonNull final UserId userId) {
    return new Identity(userId, IdentityType.ANONYMOUS, Optional.empty(), Set.of());
  }

  /**
   * Creates a registered identity.
   *
   * @param userId the registered user's ID
   * @param email the user's email address
   * @param roles the user's roles
   * @return a new registered identity
   */
  public static Identity registered(
      @NonNull final UserId userId,
      @NonNull final String email,
      @NonNull final Set<String> roles) {
    return new Identity(userId, IdentityType.REGISTERED, Optional.of(email), roles);
  }

  /**
   * Creates a registered identity with the default CUSTOMER role.
   *
   * @param userId the registered user's ID
   * @param email the user's email address
   * @return a new registered identity with CUSTOMER role
   */
  public static Identity registeredCustomer(@NonNull final UserId userId, @NonNull final String email) {
    return registered(userId, email, Set.of(ROLE_CUSTOMER));
  }

  /**
   * Checks if this is an anonymous identity.
   *
   * @return true if the identity is anonymous
   */
  public boolean isAnonymous() {
    return type == IdentityType.ANONYMOUS;
  }

  /**
   * Checks if this is a registered identity.
   *
   * @return true if the identity is registered
   */
  public boolean isRegistered() {
    return type == IdentityType.REGISTERED;
  }

  /**
   * Checks if this identity has a specific role.
   *
   * @param role the role to check
   * @return true if the identity has the role
   */
  public boolean hasRole(final String role) {
    return roles.contains(role);
  }
}
