package de.sample.aiarchitecture.inventory.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

public record StockQuantity(int value) implements Value {

  public StockQuantity {
    if (value < 0) {
      throw new IllegalArgumentException("Stock quantity cannot be negative");
    }
  }

  public static StockQuantity of(final int value) {
    return new StockQuantity(value);
  }
}
