package de.sample.aiarchitecture.sharedkernel.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Product's price.
 *
 * <p>This is part of the Shared Kernel and wraps Money with domain-specific validation. Both
 * Product and Cart contexts use Price to represent monetary values with business rules (e.g., price
 * must be greater than zero).
 */
public record Price(@NonNull Money value) implements Value {

  public Price {
    if (value == null) {
      throw new IllegalArgumentException("Price value cannot be null");
    }
    if (value.isZero()) {
      throw new IllegalArgumentException("Price must be greater than zero");
    }
  }

  public static Price of(final Money money) {
    return new Price(money);
  }

  public boolean isHigherThan(final Price other) {
    return this.value.isGreaterThan(other.value);
  }

  public Money multiply(final int quantity) {
    return this.value.multiply(quantity);
  }
}
