/**
 * Inventory Bounded Context.
 *
 * <p>Responsible for managing stock levels and inventory tracking.
 */
@NullMarked
@BoundedContext(
    name = "Inventory",
    description = "Stock level management and inventory tracking")
package de.sample.aiarchitecture.inventory;

import de.sample.aiarchitecture.sharedkernel.marker.strategic.BoundedContext;
import org.jspecify.annotations.NullMarked;

