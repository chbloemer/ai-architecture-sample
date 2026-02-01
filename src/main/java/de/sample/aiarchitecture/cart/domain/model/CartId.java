package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Shopping Cart's unique identifier.
 */
public record CartId(@NonNull String value) implements Id, Value {

  public CartId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("CartId cannot be null or blank");
    }
  }

  public static CartId generate() {
    return new CartId(UUID.randomUUID().toString());
  }

  public static CartId of(final String value) {
    return new CartId(value);
  }
}
