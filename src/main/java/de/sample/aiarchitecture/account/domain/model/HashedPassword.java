package de.sample.aiarchitecture.account.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.marker.Value;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Value Object representing a securely hashed password.
 *
 * <p>This value object:
 * <ul>
 *   <li>Never stores plaintext passwords</li>
 *   <li>Uses BCrypt for hashing (delegated to Spring's PasswordEncoder)</li>
 *   <li>Provides timing-safe comparison via BCrypt</li>
 * </ul>
 *
 * <p><b>Security:</b>
 * <ul>
 *   <li>Passwords are hashed with BCrypt (cost factor 12)</li>
 *   <li>Each hash includes a random salt</li>
 *   <li>toString() does not reveal the hash</li>
 *   <li>Comparison is timing-safe</li>
 * </ul>
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * // Create from plaintext (for registration)
 * HashedPassword password = HashedPassword.fromPlaintext("MyP@ssw0rd!", encoder);
 *
 * // Verify (for login)
 * boolean valid = password.matches("MyP@ssw0rd!", encoder);
 *
 * // Restore from database
 * HashedPassword stored = HashedPassword.fromHash("$2a$12$...");
 * }</pre>
 */
public record HashedPassword(@NonNull String hash) implements Value {

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
   * Creates a HashedPassword from a plaintext password.
   *
   * <p>This method validates the password strength and hashes it using BCrypt.
   *
   * @param plaintext the plaintext password
   * @param encoder the password encoder to use
   * @return a new HashedPassword with the BCrypt hash
   * @throws IllegalArgumentException if the password doesn't meet strength requirements
   */
  public static HashedPassword fromPlaintext(
      @NonNull final String plaintext,
      @NonNull final PasswordEncoder encoder) {
    validatePasswordStrength(plaintext);
    return new HashedPassword(encoder.encode(plaintext));
  }

  /**
   * Restores a HashedPassword from an existing hash (from database).
   *
   * <p>This method does not validate the hash format - it assumes the hash
   * was previously created by this class.
   *
   * @param hash the existing BCrypt hash
   * @return a HashedPassword wrapping the hash
   */
  public static HashedPassword fromHash(final String hash) {
    return new HashedPassword(hash);
  }

  /**
   * Checks if a plaintext password matches this hashed password.
   *
   * <p>Uses timing-safe comparison provided by BCrypt.
   *
   * @param plaintext the plaintext password to check
   * @param encoder the password encoder to use
   * @return true if the password matches
   */
  public boolean matches(@NonNull final String plaintext, @NonNull final PasswordEncoder encoder) {
    return encoder.matches(plaintext, hash);
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
  private static void validatePasswordStrength(final String plaintext) {
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
