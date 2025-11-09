package de.sample.aiarchitecture.domain.model.cart;

import de.sample.aiarchitecture.domain.model.shared.ddd.Id;
import de.sample.aiarchitecture.domain.model.shared.ddd.Value;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Cart Item's unique identifier.
 */
public record CartItemId(@NonNull String value) implements Id, Value {

  public CartItemId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("CartItemId cannot be null or blank");
    }
  }

  public static CartItemId generate() {
    return new CartItemId(UUID.randomUUID().toString());
  }

  public static CartItemId of(final String value) {
    return new CartItemId(value);
  }
}
