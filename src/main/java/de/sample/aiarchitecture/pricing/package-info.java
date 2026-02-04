/**
 * Pricing Bounded Context.
 *
 * <p>Responsible for managing product prices, price changes, and exposing pricing
 * information to other bounded contexts via Open Host Service.
 */
@BoundedContext(
    name = "Pricing",
    description = "Product pricing management and price change tracking")
package de.sample.aiarchitecture.pricing;

import de.sample.aiarchitecture.sharedkernel.marker.strategic.BoundedContext;
