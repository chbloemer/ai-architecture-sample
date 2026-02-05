package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Value Object representing current pricing and availability information for an article.
 *
 * <p>This is used by the cart domain to receive fresh pricing data from external sources
 * without coupling to specific infrastructure implementations.
 */
public record ArticlePrice(
    Money price,
    boolean isAvailable,
    int availableStock) implements Value {

  public ArticlePrice {
    if (price == null) {
      throw new IllegalArgumentException("Price cannot be null");
    }
    if (availableStock < 0) {
      throw new IllegalArgumentException("Available stock cannot be negative");
    }
  }
}
