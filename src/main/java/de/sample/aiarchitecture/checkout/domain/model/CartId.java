package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.UUID;

/**
 * Value Object representing a Shopping Cart's unique identifier within the Checkout context.
 *
 * <p>This is the Checkout bounded context's own representation of a cart ID,
 * avoiding direct coupling to the Cart bounded context's domain model.
 */
public record CartId(String value) implements Id, Value {

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
