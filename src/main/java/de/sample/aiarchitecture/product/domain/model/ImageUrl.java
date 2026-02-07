package de.sample.aiarchitecture.product.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Value Object representing a Product's image URL.
 */
public record ImageUrl(String value) implements Value {

  public ImageUrl {
    if (value == null) {
      throw new IllegalArgumentException("Image URL cannot be null");
    }
    if (value.length() > 2000) {
      throw new IllegalArgumentException("Image URL cannot exceed 2000 characters");
    }
  }

  public static ImageUrl of(final String value) {
    return new ImageUrl(value);
  }

  public static ImageUrl empty() {
    return new ImageUrl("");
  }
}
