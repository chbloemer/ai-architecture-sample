package de.sample.aiarchitecture.domain.model.cart;

import de.sample.aiarchitecture.domain.model.ddd.DomainService;
import de.sample.aiarchitecture.domain.model.shared.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.jspecify.annotations.NonNull;

/**
 * Domain Service for calculating shopping cart totals with tax and other charges.
 *
 * <p>Encapsulates complex cart total calculations that don't belong within the
 * ShoppingCart aggregate itself.
 */
public final class CartTotalCalculator implements DomainService {

  private static final BigDecimal DEFAULT_TAX_RATE = BigDecimal.valueOf(0.19); // 19% VAT

  /**
   * Calculates the total with tax applied.
   *
   * @param subtotal the subtotal before tax
   * @param taxRate the tax rate (e.g., 0.19 for 19%)
   * @return the total including tax
   */
  public Money calculateTotalWithTax(
      @NonNull final Money subtotal, @NonNull final BigDecimal taxRate) {
    if (taxRate.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Tax rate cannot be negative");
    }

    final BigDecimal taxAmount = subtotal.amount().multiply(taxRate);
    final BigDecimal totalAmount = subtotal.amount().add(taxAmount);

    return Money.of(totalAmount.setScale(2, RoundingMode.HALF_UP), subtotal.currency());
  }

  /**
   * Calculates the total with default tax rate (19% VAT).
   *
   * @param subtotal the subtotal before tax
   * @return the total including default tax
   */
  public Money calculateTotalWithDefaultTax(@NonNull final Money subtotal) {
    return calculateTotalWithTax(subtotal, DEFAULT_TAX_RATE);
  }

  /**
   * Calculates the tax amount for a given subtotal.
   *
   * @param subtotal the subtotal
   * @param taxRate the tax rate
   * @return the tax amount
   */
  public Money calculateTaxAmount(
      @NonNull final Money subtotal, @NonNull final BigDecimal taxRate) {
    if (taxRate.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Tax rate cannot be negative");
    }

    final BigDecimal taxAmount = subtotal.amount().multiply(taxRate);
    return Money.of(taxAmount.setScale(2, RoundingMode.HALF_UP), subtotal.currency());
  }

  /**
   * Calculates the total including tax and shipping.
   *
   * @param subtotal the subtotal
   * @param shipping the shipping cost
   * @param taxRate the tax rate
   * @return the grand total
   */
  public Money calculateGrandTotal(
      @NonNull final Money subtotal,
      @NonNull final Money shipping,
      @NonNull final BigDecimal taxRate) {

    final Money subtotalWithShipping = subtotal.add(shipping);
    return calculateTotalWithTax(subtotalWithShipping, taxRate);
  }
}
