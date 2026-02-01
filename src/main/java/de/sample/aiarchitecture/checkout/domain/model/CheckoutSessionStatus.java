package de.sample.aiarchitecture.checkout.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Enum representing the status of a checkout session.
 *
 * <p>Status lifecycle:
 * <ul>
 *   <li>ACTIVE - Session is in progress, can accept step submissions</li>
 *   <li>CONFIRMED - Order has been confirmed, awaiting completion</li>
 *   <li>COMPLETED - Order has been successfully processed</li>
 *   <li>ABANDONED - Session was explicitly abandoned by the user</li>
 *   <li>EXPIRED - Session timed out due to inactivity</li>
 * </ul>
 */
public enum CheckoutSessionStatus implements Value {
  ACTIVE,
  CONFIRMED,
  COMPLETED,
  ABANDONED,
  EXPIRED;

  public boolean isModifiable() {
    return this == ACTIVE;
  }

  public boolean isTerminal() {
    return this == COMPLETED || this == ABANDONED || this == EXPIRED;
  }

  public boolean canConfirm() {
    return this == ACTIVE;
  }

  public boolean canComplete() {
    return this == CONFIRMED;
  }
}
