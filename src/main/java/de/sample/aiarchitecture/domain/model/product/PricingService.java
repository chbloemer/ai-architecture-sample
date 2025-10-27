package de.sample.aiarchitecture.domain.model.product;

import de.sample.aiarchitecture.domain.model.ddd.DomainService;
import de.sample.aiarchitecture.domain.model.shared.Money;
import de.sample.aiarchitecture.domain.model.shared.Price;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

/**
 * Domain Service for pricing calculations.
 *
 * <p>Encapsulates complex pricing logic including discounts, promotional pricing,
 * and price calculations that don't belong to a single entity.
 */
public final class PricingService implements DomainService {

  /**
   * Applies a percentage discount to a price.
   *
   * @param originalPrice the original price
   * @param discountPercentage the discount percentage (0-100)
   * @return the discounted price
   * @throws IllegalArgumentException if discount percentage is invalid
   */
  public Price applyDiscount(
      @NonNull final Price originalPrice, final int discountPercentage) {
    if (discountPercentage < 0 || discountPercentage > 100) {
      throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
    }

    if (discountPercentage == 0) {
      return originalPrice;
    }

    final BigDecimal discountMultiplier =
        BigDecimal.ONE.subtract(BigDecimal.valueOf(discountPercentage).divide(BigDecimal.valueOf(100)));

    final Money discountedAmount = originalPrice.value().multiply(discountMultiplier);

    return Price.of(discountedAmount);
  }

  /**
   * Calculates a bulk discount price based on quantity.
   *
   * @param originalPrice the original price per unit
   * @param quantity the quantity being purchased
   * @return the discounted price per unit
   */
  public Price calculateBulkDiscount(
      @NonNull final Price originalPrice, final int quantity) {
    if (quantity < 1) {
      throw new IllegalArgumentException("Quantity must be at least 1");
    }

    // Bulk discount tiers
    int discountPercentage = 0;
    if (quantity >= 100) {
      discountPercentage = 20;
    } else if (quantity >= 50) {
      discountPercentage = 15;
    } else if (quantity >= 20) {
      discountPercentage = 10;
    } else if (quantity >= 10) {
      discountPercentage = 5;
    }

    return applyDiscount(originalPrice, discountPercentage);
  }

  /**
   * Compares two prices and returns the lower one.
   *
   * @param price1 first price
   * @param price2 second price
   * @return the lower price
   */
  public Price lowerPrice(@NonNull final Price price1, @NonNull final Price price2) {
    return price1.isHigherThan(price2) ? price2 : price1;
  }
}
