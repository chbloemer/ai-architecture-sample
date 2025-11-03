package de.sample.aiarchitecture.domain.model.product;

import de.sample.aiarchitecture.domain.model.shared.ddd.Value;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Product's description.
 */
public record ProductDescription(@NonNull String value) implements Value {

  public ProductDescription {
    if (value == null) {
      throw new IllegalArgumentException("Product description cannot be null");
    }
    if (value.length() > 2000) {
      throw new IllegalArgumentException("Product description cannot exceed 2000 characters");
    }
  }

  public static ProductDescription of(final String value) {
    return new ProductDescription(value);
  }

  public static ProductDescription empty() {
    return new ProductDescription("");
  }
}
