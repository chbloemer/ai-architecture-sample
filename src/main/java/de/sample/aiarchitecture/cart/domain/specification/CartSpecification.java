package de.sample.aiarchitecture.cart.domain.specification;

import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.specification.CompositeSpecification;

/**
 * Marker interface for cart-related specifications.
 *
 * <p>Extends the generic {@link CompositeSpecification} for {@link ShoppingCart} so that
 * adapters can translate the specific leaf specifications without leaking JPA into the domain.
 */
public sealed interface CartSpecification extends CompositeSpecification<ShoppingCart>
    permits ActiveCart, LastUpdatedBefore, HasMinTotal, HasAnyAvailableItem, CustomerAllowsMarketing {
}
