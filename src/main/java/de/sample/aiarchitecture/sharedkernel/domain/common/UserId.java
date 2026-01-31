package de.sample.aiarchitecture.sharedkernel.domain.common;

import de.sample.aiarchitecture.sharedkernel.domain.marker.Id;
import de.sample.aiarchitecture.sharedkernel.domain.marker.Value;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a User's unique identifier.
 *
 * <p>This is the shared identity across all bounded contexts. A UserId can represent
 * either an anonymous user (soft identity) or a registered user (hard identity).
 *
 * <p>Anonymous users receive a UserId from the first visit, which is carried in their
 * JWT token. When they register, this same UserId is linked to their Account.
 *
 * <p>The UserId value is used as the JWT subject claim and maps 1:1 to:
 * <ul>
 *   <li>Cart context's CustomerId</li>
 *   <li>Checkout context's CustomerId</li>
 *   <li>Account context's linked identity</li>
 * </ul>
 */
public record UserId(@NonNull String value) implements Id, Value {

  /**
   * Prefix for anonymous user IDs.
   */
  public static final String ANONYMOUS_PREFIX = "anon-";

  public UserId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("UserId cannot be null or blank");
    }
  }

  /**
   * Creates a UserId from a string value.
   *
   * @param value the user ID value
   * @return a new UserId
   */
  public static UserId of(final String value) {
    return new UserId(value);
  }

  /**
   * Generates a new anonymous UserId.
   *
   * <p>Anonymous user IDs are prefixed with "anon-" to distinguish them
   * from registered user IDs.
   *
   * @return a new anonymous UserId
   */
  public static UserId generateAnonymous() {
    return new UserId(ANONYMOUS_PREFIX + UUID.randomUUID());
  }

  /**
   * Generates a new registered UserId.
   *
   * <p>Registered user IDs do not have a prefix.
   *
   * @return a new registered UserId
   */
  public static UserId generateRegistered() {
    return new UserId(UUID.randomUUID().toString());
  }

  /**
   * Checks if this UserId represents an anonymous user.
   *
   * @return true if this is an anonymous user ID
   */
  public boolean isAnonymous() {
    return value.startsWith(ANONYMOUS_PREFIX);
  }

  /**
   * Returns the raw UUID portion of the user ID.
   *
   * <p>For anonymous users, this strips the "anon-" prefix.
   * For registered users, this returns the full value.
   *
   * @return the UUID portion of the user ID
   */
  public String rawUuid() {
    if (isAnonymous()) {
      return value.substring(ANONYMOUS_PREFIX.length());
    }
    return value;
  }
}
