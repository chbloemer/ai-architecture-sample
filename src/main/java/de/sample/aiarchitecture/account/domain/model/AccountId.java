package de.sample.aiarchitecture.account.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.UUID;

/**
 * Value Object representing an Account's unique identifier.
 *
 * <p>This is distinct from UserId - AccountId identifies the Account aggregate,
 * while UserId identifies the user across all contexts.
 *
 * <p>The relationship is:
 * <ul>
 *   <li>One Account has one AccountId (aggregate identity)</li>
 *   <li>One Account is linked to one UserId (cross-context identity)</li>
 *   <li>Anonymous users have a UserId but no Account</li>
 * </ul>
 */
public record AccountId(String value) implements Id, Value {

  public AccountId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("AccountId cannot be null or blank");
    }
  }

  /**
   * Creates an AccountId from a string value.
   *
   * @param value the account ID value
   * @return a new AccountId
   */
  public static AccountId of(final String value) {
    return new AccountId(value);
  }

  /**
   * Generates a new unique AccountId.
   *
   * @return a new AccountId with a random UUID
   */
  public static AccountId generate() {
    return new AccountId(UUID.randomUUID().toString());
  }
}
