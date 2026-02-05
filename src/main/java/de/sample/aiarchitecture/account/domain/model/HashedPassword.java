package de.sample.aiarchitecture.account.domain.model;

import de.sample.aiarchitecture.account.domain.service.PasswordHasher;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Value Object representing a securely hashed password.
 *
 * <p>This value object:
 * <ul>
 *   <li>Never stores plaintext passwords</li>
 *   <li>Validates password strength requirements</li>
 *   <li>Delegates hashing to a {@link PasswordHasher} domain service</li>
 * </ul>
 *
 * <p><b>Security:</b>
 * <ul>
 *   <li>Passwords are hashed with BCrypt (cost factor 12)</li>
 *   <li>toString() does not reveal the hash</li>
 * </ul>
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * // Create from plaintext (validates strength and hashes)
 * HashedPassword password = HashedPassword.fromPlaintext("MyP@ssw0rd!", hasher);
 *
 * // Verify password
 * boolean valid = password.matches("MyP@ssw0rd!", hasher);
 *
 * // Restore from persistence
 * HashedPassword restored = HashedPassword.of("$2a$12$...");
 * }</pre>
 */
public record HashedPassword(String hash) implements Value {

  /**
   * Minimum password length requirement.
   */
  public static final int MIN_LENGTH = 8;

  public HashedPassword {
    if (hash == null || hash.isBlank()) {
      throw new IllegalArgumentException("Password hash cannot be null or blank");
    }
  }

  /**
   * Creates a HashedPassword from plaintext by validating and hashing.
   *
   * <p>This factory method:
   * <ol>
   *   <li>Validates password strength requirements</li>
   *   <li>Hashes the password using the provided hasher</li>
   * </ol>
   *
   * @param plaintext the plaintext password
   * @param hasher the password hasher service
   * @return a HashedPassword wrapping the generated hash
   * @throws IllegalArgumentException if password doesn't meet strength requirements
   */
  public static HashedPassword fromPlaintext(
      final String plaintext,
      final PasswordHasher hasher) {
    validatePasswordStrength(plaintext);
    return new HashedPassword(hasher.hash(plaintext));
  }

  /**
   * Creates a HashedPassword from an existing hash.
   *
   * <p>This factory method creates a HashedPassword from a pre-computed hash,
   * such as one loaded from storage.
   *
   * @param hash the BCrypt hash
   * @return a HashedPassword wrapping the hash
   */
  public static HashedPassword of(final String hash) {
    return new HashedPassword(hash);
  }

  /**
   * Alias for {@link #of(String)} - restores a HashedPassword from storage.
   *
   * @param hash the existing BCrypt hash
   * @return a HashedPassword wrapping the hash
   */
  public static HashedPassword fromHash(final String hash) {
    return of(hash);
  }

  /**
   * Checks if a plaintext password matches this hashed password.
   *
   * @param plaintext the plaintext password to verify
   * @param hasher the password hasher service
   * @return true if the password matches
   */
  public boolean matches(final String plaintext, final PasswordHasher hasher) {
    return hasher.matches(plaintext, hash);
  }

  /**
   * Validates password strength requirements.
   *
   * <p>Requirements:
   * <ul>
   *   <li>Minimum 8 characters</li>
   *   <li>At least one uppercase letter</li>
   *   <li>At least one lowercase letter</li>
   *   <li>At least one digit</li>
   * </ul>
   *
   * @param plaintext the password to validate
   * @throws IllegalArgumentException if requirements are not met
   */
  public static void validatePasswordStrength(final String plaintext) {
    if (plaintext == null || plaintext.length() < MIN_LENGTH) {
      throw new IllegalArgumentException(
          "Password must be at least " + MIN_LENGTH + " characters long");
    }

    boolean hasUpper = false;
    boolean hasLower = false;
    boolean hasDigit = false;

    for (char c : plaintext.toCharArray()) {
      if (Character.isUpperCase(c)) hasUpper = true;
      if (Character.isLowerCase(c)) hasLower = true;
      if (Character.isDigit(c)) hasDigit = true;
    }

    if (!hasUpper) {
      throw new IllegalArgumentException("Password must contain at least one uppercase letter");
    }
    if (!hasLower) {
      throw new IllegalArgumentException("Password must contain at least one lowercase letter");
    }
    if (!hasDigit) {
      throw new IllegalArgumentException("Password must contain at least one digit");
    }
  }

  /**
   * Returns a masked string representation (never reveals the hash).
   *
   * @return a constant masked string
   */
  @Override
  public String toString() {
    return "HashedPassword[********]";
  }
}
