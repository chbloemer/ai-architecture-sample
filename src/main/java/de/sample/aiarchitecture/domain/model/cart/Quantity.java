package de.sample.aiarchitecture.domain.model.cart;

import de.sample.aiarchitecture.domain.model.shared.ddd.Value;

/**
 * Value Object representing a quantity of items.
 */
public record Quantity(int value) implements Value {

  public Quantity {
    if (value <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than zero");
    }
  }

  public static Quantity of(final int value) {
    return new Quantity(value);
  }

  public Quantity add(final int amount) {
    return new Quantity(this.value + amount);
  }

  public Quantity subtract(final int amount) {
    if (this.value - amount <= 0) {
      throw new IllegalArgumentException("Resulting quantity must be greater than zero");
    }
    return new Quantity(this.value - amount);
  }

  public Quantity increase() {
    return new Quantity(this.value + 1);
  }

  public Quantity decrease() {
    if (this.value <= 1) {
      throw new IllegalArgumentException("Cannot decrease quantity below 1");
    }
    return new Quantity(this.value - 1);
  }
}
