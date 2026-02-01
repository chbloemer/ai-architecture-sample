package de.sample.aiarchitecture.product.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing the stock quantity of a product.
 */
public record ProductStock(int quantity) implements Value {

  public ProductStock {
    if (quantity < 0) {
      throw new IllegalArgumentException("Stock quantity cannot be negative");
    }
  }

  public static ProductStock of(final int quantity) {
    return new ProductStock(quantity);
  }

  public static ProductStock empty() {
    return new ProductStock(0);
  }

  public ProductStock add(final int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Cannot add negative amount to stock");
    }
    return new ProductStock(this.quantity + amount);
  }

  public ProductStock subtract(final int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Cannot subtract negative amount from stock");
    }
    if (this.quantity < amount) {
      throw new IllegalArgumentException("Insufficient stock quantity");
    }
    return new ProductStock(this.quantity - amount);
  }

  public boolean isAvailable() {
    return quantity > 0;
  }

  public boolean hasStock(final int requiredQuantity) {
    return quantity >= requiredQuantity;
  }
}
