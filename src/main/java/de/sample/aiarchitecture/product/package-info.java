/**
 * Product Catalog Bounded Context.
 *
 * <p>Responsible for product management, catalog browsing, and inventory tracking.
 */
@NullMarked
@BoundedContext(
    name = "Product Catalog",
    description = "Product management, catalog browsing, and inventory tracking")
package de.sample.aiarchitecture.product;

import de.sample.aiarchitecture.sharedkernel.marker.strategic.BoundedContext;
import org.jspecify.annotations.NullMarked;

