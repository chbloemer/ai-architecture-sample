/**
 * Read Models for the Checkout bounded context.
 *
 * <p>This package contains read models and builders that construct query-optimized
 * views of checkout state. Read models are built using the Interest Interface Pattern,
 * where aggregates push their state to builders via {@code provideStateTo()} methods.
 *
 * @see de.sample.aiarchitecture.checkout.domain.model.CheckoutStateInterest
 * @see de.sample.aiarchitecture.sharedkernel.marker.tactical.ReadModelBuilder
 */
@NullMarked
package de.sample.aiarchitecture.checkout.domain.readmodel;

import org.jspecify.annotations.NullMarked;
