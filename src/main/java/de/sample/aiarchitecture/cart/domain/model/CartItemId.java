package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.UUID;

/**
 * Value Object representing a Cart Item's unique identifier.
 */
public record CartItemId(String value) implements Id, Value {

  public CartItemId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("CartItemId cannot be null or blank");
    }
  }

  public static CartItemId generate() {
    return new CartItemId(UUID.randomUUID().toString());
  }

  public static CartItemId of(final String value) {
    return new CartItemId(value);
  }
}
