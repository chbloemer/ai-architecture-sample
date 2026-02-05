package de.sample.aiarchitecture.inventory.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.UUID;
import lombok.NonNull;

public record StockLevelId(String value) implements Id, Value {

  public StockLevelId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("StockLevelId cannot be null or blank");
    }
  }

  public static StockLevelId generate() {
    return new StockLevelId(UUID.randomUUID().toString());
  }

  public static StockLevelId of(final String value) {
    return new StockLevelId(value);
  }
}
