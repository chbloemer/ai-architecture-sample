package de.sample.aiarchitecture.domain.model.product;

import de.sample.aiarchitecture.domain.model.ddd.Value;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Product's name.
 */
public record ProductName(@NonNull String value) implements Value {

  public ProductName {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Product name cannot be null or blank");
    }
    if (value.length() > 255) {
      throw new IllegalArgumentException("Product name cannot exceed 255 characters");
    }
  }

  public static ProductName of(final String value) {
    return new ProductName(value);
  }
}
