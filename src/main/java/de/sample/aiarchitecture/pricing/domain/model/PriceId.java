package de.sample.aiarchitecture.pricing.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Price's unique identifier.
 */
public record PriceId(@NonNull String value) implements Id, Value {

  public PriceId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("PriceId cannot be null or blank");
    }
  }

  public static PriceId generate() {
    return new PriceId(UUID.randomUUID().toString());
  }

  public static PriceId of(final String value) {
    return new PriceId(value);
  }
}
