package de.sample.aiarchitecture.product.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.marker.Value;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Stock Keeping Unit (SKU).
 * SKUs are unique identifiers for products in inventory management.
 */
public record SKU(@NonNull String value) implements Value {

  public SKU {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("SKU cannot be null or blank");
    }
    if (!value.matches("^[A-Z0-9-]+$")) {
      throw new IllegalArgumentException("SKU must contain only uppercase letters, numbers, and hyphens");
    }
  }

  public static SKU of(final String value) {
    return new SKU(value);
  }
}
