/**
 * Checkout Bounded Context.
 *
 * <p>Responsible for checkout process, order placement, and payment orchestration.
 */
@NullMarked
@BoundedContext(
    name = "Checkout",
    description = "Checkout process, order placement, and payment orchestration")
@ApplicationModule(
    allowedDependencies = {
      "sharedkernel",
      "infrastructure",
      "product :: api",
      "pricing :: api",
      "inventory :: api",
      "cart :: api",
      "cart :: events"
    })
package de.sample.aiarchitecture.checkout;

import de.sample.aiarchitecture.sharedkernel.marker.strategic.BoundedContext;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
