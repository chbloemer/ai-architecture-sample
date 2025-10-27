package de.sample.aiarchitecture.domain.model.cart;

import de.sample.aiarchitecture.domain.model.ddd.Id;
import de.sample.aiarchitecture.domain.model.ddd.Value;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Customer's unique identifier.
 * References a customer from the Customer bounded context by ID only.
 */
public record CustomerId(@NonNull String value) implements Id, Value {

  public CustomerId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("CustomerId cannot be null or blank");
    }
  }

  public static CustomerId of(final String value) {
    return new CustomerId(value);
  }
}
