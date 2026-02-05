package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Value Object representing a shipping option for checkout.
 *
 * <p>Contains shipping method details including identifier, display name,
 * estimated delivery time, and cost.
 */
public record ShippingOption(
    String id,
    String name,
    String estimatedDelivery,
    Money cost)
    implements Value {

  public ShippingOption {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Shipping option id cannot be null or blank");
    }
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Shipping option name cannot be null or blank");
    }
    if (estimatedDelivery == null || estimatedDelivery.isBlank()) {
      throw new IllegalArgumentException("Estimated delivery cannot be null or blank");
    }
    if (cost == null) {
      throw new IllegalArgumentException("Shipping cost cannot be null");
    }
  }

  public static ShippingOption of(
      final String id, final String name, final String estimatedDelivery, final Money cost) {
    return new ShippingOption(id, name, estimatedDelivery, cost);
  }

  public boolean isFree() {
    return cost.isZero();
  }
}
