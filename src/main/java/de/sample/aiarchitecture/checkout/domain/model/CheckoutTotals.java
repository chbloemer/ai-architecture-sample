package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.Currency;

/**
 * Value Object representing the calculated totals for a checkout session.
 *
 * <p>Contains subtotal, shipping, tax, and grand total amounts.
 */
public record CheckoutTotals(
    Money subtotal, Money shipping, Money tax, Money total)
    implements Value {

  public CheckoutTotals {
    if (subtotal == null) {
      throw new IllegalArgumentException("Subtotal cannot be null");
    }
    if (shipping == null) {
      throw new IllegalArgumentException("Shipping cannot be null");
    }
    if (tax == null) {
      throw new IllegalArgumentException("Tax cannot be null");
    }
    if (total == null) {
      throw new IllegalArgumentException("Total cannot be null");
    }
  }

  public static CheckoutTotals of(
      final Money subtotal, final Money shipping, final Money tax, final Money total) {
    return new CheckoutTotals(subtotal, shipping, tax, total);
  }

  public static CheckoutTotals calculate(
      final Money subtotal, final Money shipping, final Money tax) {
    var total = subtotal.add(shipping).add(tax);
    return new CheckoutTotals(subtotal, shipping, tax, total);
  }

  public static CheckoutTotals zero(final Currency currency) {
    var zero = Money.zero(currency);
    return new CheckoutTotals(zero, zero, zero, zero);
  }

  public CheckoutTotals withShipping(final Money newShipping) {
    return CheckoutTotals.calculate(this.subtotal, newShipping, this.tax);
  }

  public CheckoutTotals withTax(final Money newTax) {
    return CheckoutTotals.calculate(this.subtotal, this.shipping, newTax);
  }
}
