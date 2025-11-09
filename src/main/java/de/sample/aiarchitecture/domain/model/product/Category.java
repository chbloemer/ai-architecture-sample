package de.sample.aiarchitecture.domain.model.product;

import de.sample.aiarchitecture.domain.model.shared.ddd.Value;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Product's category.
 */
public record Category(@NonNull String name) implements Value {

  public Category {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Category name cannot be null or blank");
    }
  }

  public static Category of(final String name) {
    return new Category(name);
  }

  // Predefined categories for e-commerce
  public static Category electronics() {
    return new Category("Electronics");
  }

  public static Category clothing() {
    return new Category("Clothing");
  }

  public static Category books() {
    return new Category("Books");
  }

  public static Category homeAndGarden() {
    return new Category("Home & Garden");
  }

  public static Category sports() {
    return new Category("Sports & Outdoors");
  }
}
