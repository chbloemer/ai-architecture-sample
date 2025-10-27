package de.sample.aiarchitecture.domain.model.cart;

import de.sample.aiarchitecture.domain.model.ddd.Value;

/**
 * Value Object representing the status of a shopping cart.
 */
public enum CartStatus implements Value {
  /**
   * Cart is active and can be modified.
   */
  ACTIVE,

  /**
   * Cart has been checked out and cannot be modified.
   */
  CHECKED_OUT,

  /**
   * Cart has been abandoned by the customer.
   */
  ABANDONED
}
