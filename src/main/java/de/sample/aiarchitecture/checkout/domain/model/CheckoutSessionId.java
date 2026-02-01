package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Checkout Session's unique identifier.
 */
public record CheckoutSessionId(@NonNull String value) implements Id, Value {

  public CheckoutSessionId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("CheckoutSessionId cannot be null or blank");
    }
  }

  public static CheckoutSessionId generate() {
    return new CheckoutSessionId(UUID.randomUUID().toString());
  }

  public static CheckoutSessionId of(final String value) {
    return new CheckoutSessionId(value);
  }
}
