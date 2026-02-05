package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Value Object representing a Customer's unique identifier.
 * References a customer from the Customer bounded context by ID only.
 */
public record CustomerId(String value) implements Id, Value {

  public CustomerId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("CustomerId cannot be null or blank");
    }
  }

  public static CustomerId of(final String value) {
    return new CustomerId(value);
  }
}
