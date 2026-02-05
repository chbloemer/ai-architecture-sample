package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Value Object representing article information needed for cart state exposure.
 *
 * <p>This is used by the cart domain to receive product names and current pricing
 * when exposing cart state through the Interest Interface pattern, without coupling
 * to specific infrastructure implementations.
 */
public record ArticleInfo(
    String name,
    Money price) implements Value {

  public ArticleInfo {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    if (price == null) {
      throw new IllegalArgumentException("Price cannot be null");
    }
  }
}
