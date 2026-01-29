package de.sample.aiarchitecture.cart.domain.specificationificationificationificationification;

import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.specificationificationificationificationification.SpecificationVisitor;
import org.jspecify.annotations.NonNull;

/**
 * Visitor for translating cart specifications to adapter-specific forms.
 *
 * <p>Adapters (e.g., JPA) implement this interface to translate a domain-level cart
 * specification into a query predicate or similar construct.
 */
public interface CartSpecificationVisitor<R> extends SpecificationVisitor<ShoppingCart, R> {
  R visit(@NonNull ActiveCart spec);
  R visit(@NonNull LastUpdatedBefore spec);
  R visit(@NonNull HasMinTotal spec);
  R visit(@NonNull HasAnyAvailableItem spec);
  R visit(@NonNull CustomerAllowsMarketing spec);
}
