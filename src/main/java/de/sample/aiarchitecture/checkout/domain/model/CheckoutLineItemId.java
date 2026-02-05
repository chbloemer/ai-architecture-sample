package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.UUID;

/**
 * Value Object representing a checkout line item's unique identifier.
 */
public record CheckoutLineItemId(String value) implements Id, Value {

  public CheckoutLineItemId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("CheckoutLineItemId cannot be null or blank");
    }
  }

  public static CheckoutLineItemId generate() {
    return new CheckoutLineItemId(UUID.randomUUID().toString());
  }

  public static CheckoutLineItemId of(final String value) {
    return new CheckoutLineItemId(value);
  }
}
