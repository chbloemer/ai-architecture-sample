package de.sample.aiarchitecture.account.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.regex.Pattern;

/**
 * Value Object representing an email address.
 *
 * <p>This value object ensures email addresses are:
 * <ul>
 *   <li>Non-null and non-blank</li>
 *   <li>Properly formatted (basic validation)</li>
 *   <li>Normalized to lowercase</li>
 * </ul>
 *
 * <p>Note: Full RFC 5322 compliance is not attempted; this provides
 * practical validation for common email formats.
 */
public record Email(String value) implements Value {

  /**
   * Basic email pattern for validation.
   * Matches: local@domain.tld
   */
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

  public Email {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Email cannot be null or blank");
    }
    // Normalize to lowercase
    value = value.toLowerCase().trim();
    if (!EMAIL_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid email format: " + value);
    }
  }

  /**
   * Creates an Email from a string value.
   *
   * @param value the email address
   * @return a new Email
   * @throws IllegalArgumentException if the email format is invalid
   */
  public static Email of(final String value) {
    return new Email(value);
  }

  /**
   * Returns the local part of the email (before @).
   *
   * @return the local part
   */
  public String localPart() {
    return value.substring(0, value.indexOf('@'));
  }

  /**
   * Returns the domain part of the email (after @).
   *
   * @return the domain part
   */
  public String domain() {
    return value.substring(value.indexOf('@') + 1);
  }
}
