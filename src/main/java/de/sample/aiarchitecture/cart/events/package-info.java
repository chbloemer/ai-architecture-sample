/**
 * Cart Events — published integration events for cross-module consumption.
 *
 * <p>Contains integration events published by the Cart context when cart-related state changes
 * occur. Other modules consume these events via {@code @ApplicationModuleListener}.
 */
@NamedInterface("events")
@NullMarked
package de.sample.aiarchitecture.cart.events;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
