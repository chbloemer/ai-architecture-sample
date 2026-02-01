package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Enum representing the steps in the checkout flow.
 *
 * <p>The checkout follows a 5-step flow:
 * <ol>
 *   <li>BUYER_INFO - Collect buyer contact information</li>
 *   <li>DELIVERY - Select delivery address and shipping option</li>
 *   <li>PAYMENT - Select payment method</li>
 *   <li>REVIEW - Review order before confirmation</li>
 *   <li>CONFIRMATION - Order confirmed</li>
 * </ol>
 */
public enum CheckoutStep implements Value {
  BUYER_INFO(1),
  DELIVERY(2),
  PAYMENT(3),
  REVIEW(4),
  CONFIRMATION(5);

  private final int order;

  CheckoutStep(final int order) {
    this.order = order;
  }

  public int getOrder() {
    return order;
  }

  public boolean isBefore(final CheckoutStep other) {
    return this.order < other.order;
  }

  public boolean isAfter(final CheckoutStep other) {
    return this.order > other.order;
  }

  public boolean isTerminal() {
    return this == CONFIRMATION;
  }

  public CheckoutStep next() {
    if (isTerminal()) {
      throw new IllegalStateException("Cannot advance from terminal step: " + this);
    }
    return values()[this.ordinal() + 1];
  }

  public CheckoutStep previous() {
    if (this == BUYER_INFO) {
      throw new IllegalStateException("Cannot go back from first step: " + this);
    }
    return values()[this.ordinal() - 1];
  }
}
