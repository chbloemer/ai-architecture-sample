package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.marker.Value;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing buyer contact information for checkout.
 *
 * <p>Contains the essential contact details needed to process an order.
 */
public record BuyerInfo(
    @NonNull String email,
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull String phone)
    implements Value {

  public BuyerInfo {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email cannot be null or blank");
    }
    if (!email.contains("@")) {
      throw new IllegalArgumentException("Email must be a valid email address");
    }
    if (firstName == null || firstName.isBlank()) {
      throw new IllegalArgumentException("First name cannot be null or blank");
    }
    if (lastName == null || lastName.isBlank()) {
      throw new IllegalArgumentException("Last name cannot be null or blank");
    }
    if (phone == null || phone.isBlank()) {
      throw new IllegalArgumentException("Phone cannot be null or blank");
    }
  }

  public static BuyerInfo of(
      final String email, final String firstName, final String lastName, final String phone) {
    return new BuyerInfo(email, firstName, lastName, phone);
  }

  public String fullName() {
    return firstName + " " + lastName;
  }
}
