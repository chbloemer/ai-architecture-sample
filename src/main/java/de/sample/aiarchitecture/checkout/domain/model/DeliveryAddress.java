package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Value Object representing a delivery address for checkout.
 */
public record DeliveryAddress(
    @NonNull String street,
    @Nullable String streetLine2,
    @NonNull String city,
    @NonNull String postalCode,
    @NonNull String country,
    @Nullable String state)
    implements Value {

  public DeliveryAddress {
    if (street == null || street.isBlank()) {
      throw new IllegalArgumentException("Street cannot be null or blank");
    }
    if (city == null || city.isBlank()) {
      throw new IllegalArgumentException("City cannot be null or blank");
    }
    if (postalCode == null || postalCode.isBlank()) {
      throw new IllegalArgumentException("Postal code cannot be null or blank");
    }
    if (country == null || country.isBlank()) {
      throw new IllegalArgumentException("Country cannot be null or blank");
    }
  }

  public static DeliveryAddress of(
      final String street,
      final String streetLine2,
      final String city,
      final String postalCode,
      final String country,
      final String state) {
    return new DeliveryAddress(street, streetLine2, city, postalCode, country, state);
  }

  public static DeliveryAddress of(
      final String street,
      final String city,
      final String postalCode,
      final String country) {
    return new DeliveryAddress(street, null, city, postalCode, country, null);
  }

  public String formattedAddress() {
    var sb = new StringBuilder();
    sb.append(street);
    if (streetLine2 != null && !streetLine2.isBlank()) {
      sb.append(", ").append(streetLine2);
    }
    sb.append(", ").append(postalCode).append(" ").append(city);
    if (state != null && !state.isBlank()) {
      sb.append(", ").append(state);
    }
    sb.append(", ").append(country);
    return sb.toString();
  }
}
