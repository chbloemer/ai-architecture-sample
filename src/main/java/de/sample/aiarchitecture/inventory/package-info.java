/**
 * Inventory Bounded Context.
 *
 * <p>Responsible for managing stock levels and inventory tracking.
 */
@NullMarked
@BoundedContext(name = "Inventory", description = "Stock level management and inventory tracking")
@ApplicationModule(allowedDependencies = {"sharedkernel", "infrastructure"})
package de.sample.aiarchitecture.inventory;

import de.sample.aiarchitecture.sharedkernel.marker.strategic.BoundedContext;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
