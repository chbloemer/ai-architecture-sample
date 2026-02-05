package de.sample.aiarchitecture.pricing.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.UUID;

/**
 * Value Object representing a Price's unique identifier.
 */
public record PriceId(String value) implements Id, Value {

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
