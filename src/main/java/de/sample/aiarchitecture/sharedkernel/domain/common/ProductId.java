package de.sample.aiarchitecture.sharedkernel.domain.common;

import de.sample.aiarchitecture.sharedkernel.domain.marker.Id;
import de.sample.aiarchitecture.sharedkernel.domain.marker.Value;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing a Product's unique identifier.
 *
 * <p>This is part of the Shared Kernel and allows the Cart bounded context to reference products
 * without creating a direct dependency on the Product aggregate.
 */
public record ProductId(@NonNull String value) implements Id, Value {

  public ProductId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ProductId cannot be null or blank");
    }
  }

  public static ProductId generate() {
    return new ProductId(UUID.randomUUID().toString());
  }

  public static ProductId of(final String value) {
    return new ProductId(value);
  }
}
