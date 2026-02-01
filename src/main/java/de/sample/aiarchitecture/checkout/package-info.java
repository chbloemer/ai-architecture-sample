/**
 * Checkout Bounded Context.
 *
 * <p>Responsible for checkout process, order placement, and payment orchestration.
 */
@BoundedContext(
    name = "Checkout",
    description = "Checkout process, order placement, and payment orchestration")
package de.sample.aiarchitecture.checkout;

import de.sample.aiarchitecture.sharedkernel.marker.strategic.BoundedContext;
