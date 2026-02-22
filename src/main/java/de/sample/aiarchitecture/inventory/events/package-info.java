/**
 * Inventory Events — published trigger interfaces for cross-module consumption.
 *
 * <p>Contains trigger interfaces that other modules' events implement (Interface Inversion
 * pattern). This allows inventory to listen to its own interfaces without depending on the
 * producing module.
 */
@NamedInterface("events")
@NullMarked
package de.sample.aiarchitecture.inventory.events;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
