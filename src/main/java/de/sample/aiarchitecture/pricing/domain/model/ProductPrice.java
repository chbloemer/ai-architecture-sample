package de.sample.aiarchitecture.pricing.domain.model;

import de.sample.aiarchitecture.pricing.domain.event.PriceChanged;
import de.sample.aiarchitecture.pricing.domain.event.PriceCreated;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.BaseAggregateRoot;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * ProductPrice Aggregate Root.
 *
 * <p>Represents a product's price in the pricing bounded context. This aggregate manages price
 * changes for products and ensures pricing invariants are maintained.
 *
 * <p><b>Business Rules:</b>
 *
 * <ul>
 *   <li>Price must be greater than zero
 *   <li>Price changes are tracked with effective dates
 * </ul>
 *
 * <p><b>Domain Events:</b>
 *
 * <ul>
 *   <li>{@link PriceCreated} - when a new price is created
 *   <li>{@link PriceChanged} - when the price is updated
 * </ul>
 */
public final class ProductPrice extends BaseAggregateRoot<ProductPrice, PriceId> {

  private final PriceId id;
  private final ProductId productId;
  private Money currentPrice;
  private Instant effectiveFrom;

  private ProductPrice(
      final PriceId id,
      final ProductId productId,
      final Money currentPrice,
      final Instant effectiveFrom) {
    this.id = id;
    this.productId = productId;
    this.currentPrice = currentPrice;
    this.effectiveFrom = effectiveFrom;
  }

  /**
   * Creates a new ProductPrice aggregate.
   *
   * <p>Raises a {@link PriceCreated} domain event.
   *
   * @param productId the product ID
   * @param price the initial price (must be greater than zero)
   * @return the new ProductPrice aggregate
   * @throws IllegalArgumentException if price is not greater than zero
   */
  public static ProductPrice create(
      final ProductId productId, final Money price) {
    validatePriceGreaterThanZero(price);
    final PriceId priceId = PriceId.generate();
    final Instant effectiveFrom = Instant.now();
    final ProductPrice productPrice = new ProductPrice(priceId, productId, price, effectiveFrom);
    productPrice.registerEvent(PriceCreated.now(priceId, productId, price, effectiveFrom));
    return productPrice;
  }

  @Override
  public PriceId id() {
    return id;
  }

  public ProductId productId() {
    return productId;
  }

  public Money currentPrice() {
    return currentPrice;
  }

  public Instant effectiveFrom() {
    return effectiveFrom;
  }

  /**
   * Updates the price to a new value.
   *
   * <p>Raises a {@link PriceChanged} domain event.
   *
   * @param newPrice the new price (must be greater than zero)
   * @throws IllegalArgumentException if newPrice is not greater than zero
   */
  public void updatePrice(final Money newPrice) {
    validatePriceGreaterThanZero(newPrice);

    final Money oldPrice = this.currentPrice;
    final Instant newEffectiveFrom = Instant.now();

    this.currentPrice = newPrice;
    this.effectiveFrom = newEffectiveFrom;

    registerEvent(PriceChanged.now(this.id, this.productId, oldPrice, newPrice, newEffectiveFrom));
  }

  private static void validatePriceGreaterThanZero(final Money price) {
    if (price.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price must be greater than zero");
    }
  }
}
