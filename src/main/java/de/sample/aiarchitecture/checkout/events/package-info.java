/**
 * Checkout Events — published integration events for cross-module consumption.
 *
 * <p>Contains integration events published by the Checkout context when checkout-related state
 * changes occur. Other modules consume these events via {@code @ApplicationModuleListener}.
 */
@NamedInterface("events")
@NullMarked
package de.sample.aiarchitecture.checkout.events;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
