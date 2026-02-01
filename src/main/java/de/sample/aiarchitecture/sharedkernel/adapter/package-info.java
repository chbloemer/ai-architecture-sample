/**
 * Adapters in the shared kernel.
 *
 * <p>This package contains adapters that are shared across all bounded contexts.
 * Following hexagonal architecture, adapters implement ports defined in the application layer.
 *
 * <p><b>Subpackages:</b>
 * <ul>
 *   <li>{@code outgoing} - Outgoing adapters (implement output ports)</li>
 * </ul>
 *
 * <p><b>Note:</b> The shared kernel adapter layer contains only cross-cutting infrastructure
 * that all bounded contexts need. Context-specific adapters remain in their respective contexts.
 */
@org.jspecify.annotations.NullMarked
package de.sample.aiarchitecture.sharedkernel.adapter;
