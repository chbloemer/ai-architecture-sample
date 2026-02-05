/**
 * Shopping Cart Bounded Context.
 *
 * <p>Responsible for cart management, item additions/removals, and cart lifecycle.
 */
@NullMarked
@BoundedContext(
    name = "Shopping Cart",
    description = "Cart management, item additions/removals, and cart lifecycle")
package de.sample.aiarchitecture.cart;

import de.sample.aiarchitecture.sharedkernel.marker.strategic.BoundedContext;
import org.jspecify.annotations.NullMarked;

