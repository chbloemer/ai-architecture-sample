/**
 * Read Models for the Cart bounded context.
 *
 * <p>This package contains read models and builders that construct query-optimized
 * views of cart state. Read models are built using the Interest Interface Pattern,
 * where aggregates push their state to builders via {@code provideStateTo()} methods.
 *
 * <p>The {@link de.sample.aiarchitecture.cart.domain.readmodel.EnrichedCartBuilder}
 * combines snapshot state from the {@link de.sample.aiarchitecture.cart.domain.model.ShoppingCart}
 * aggregate with current article data from external services to build enriched read models.
 *
 * @see de.sample.aiarchitecture.cart.domain.model.CartStateInterest
 * @see de.sample.aiarchitecture.sharedkernel.marker.tactical.ReadModelBuilder
 */
@NullMarked
package de.sample.aiarchitecture.cart.domain.readmodel;

import org.jspecify.annotations.NullMarked;
