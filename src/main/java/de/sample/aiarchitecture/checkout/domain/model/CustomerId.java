package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.marker.Id;
import de.sample.aiarchitecture.sharedkernel.domain.marker.Value;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Customer's unique identifier within the Checkout context.
 *
 * <p>This is the Checkout bounded context's own representation of a customer ID,
 * avoiding direct coupling to other bounded contexts' domain models.
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
