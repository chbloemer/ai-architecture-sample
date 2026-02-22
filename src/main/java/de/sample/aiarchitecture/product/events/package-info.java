/**
 * Product Events — published integration events for cross-module consumption.
 *
 * <p>Contains integration events published by the Product context when product-related state
 * changes occur. Other modules consume these events via {@code @ApplicationModuleListener}.
 */
@NamedInterface("events")
@NullMarked
package de.sample.aiarchitecture.product.events;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
