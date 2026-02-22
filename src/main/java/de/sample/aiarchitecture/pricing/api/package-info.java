/**
 * Pricing API — published interface for cross-module access.
 *
 * <p>Exposes pricing information (Open Host Service pattern). Consuming modules should define their
 * own output ports and implement adapters that delegate to this service.
 */
@NamedInterface("api")
@NullMarked
package de.sample.aiarchitecture.pricing.api;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
