package de.sample.aiarchitecture.sharedkernel.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a User's unique identifier.
 *
 * <p>This is the shared identity across all bounded contexts. A UserId is a raw UUID
 * that remains stable across the user lifecycle - from anonymous browsing through
 * registration and beyond.
 *
 * <p>Anonymous users receive a UserId from their first visit, which is carried in their
 * JWT token. When they register, this same UserId is linked to their Account, ensuring
 * continuity of cart, checkout session, and other user data.
 *
 * <p>The UserId value is used as the JWT subject claim and maps 1:1 to:
 * <ul>
 *   <li>Cart context's CustomerId</li>
 *   <li>Checkout context's CustomerId</li>
 *   <li>Account context's linked identity</li>
 * </ul>
 *
 * <p><b>Note:</b> The distinction between anonymous and registered users is tracked
 * in the JWT claims (type: "anonymous" vs "registered"), not in the UserId itself.
 * This ensures the UserId remains unchanged on registration.
 */
public record UserId(@NonNull String value) implements Id, Value {

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
   * Generates a new UserId for an anonymous user.
   *
   * <p>The generated UserId is a raw UUID without any prefix. This ensures
   * the UserId remains stable when the user later registers, preserving
   * cart and checkout session data.
   *
   * @return a new UserId
   */
  public static UserId generateAnonymous() {
    return new UserId(UUID.randomUUID().toString());
  }

  /**
   * Generates a new UserId for a registered user.
   *
   * <p>Generates a raw UUID, same format as anonymous users.
   *
   * @return a new UserId
   */
  public static UserId generateRegistered() {
    return new UserId(UUID.randomUUID().toString());
  }
}
