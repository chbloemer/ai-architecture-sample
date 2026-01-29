package de.sample.aiarchitecture.cart.domain.specificationificationificationificationification;

import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.specificationificationificationificationification.AndSpecification;
import de.sample.aiarchitecture.sharedkernel.domain.specificationificationificationificationification.SpecificationVisitor;
import org.jspecify.annotations.NonNull;

/**
 * Customer owning the cart has opted-in to receive marketing communication.
 *
 * <p>Domain aggregate doesn't expose customer preferences; in-memory evaluation is
 * neutral (true). Persistence adapters can push this to the DB if a customer
 * read-model exists; otherwise it can be a no-op predicate.
 */
public record CustomerAllowsMarketing() implements CartSpecification {

  @Override
  public boolean isSatisfiedBy(@NonNull ShoppingCart candidate) {
    // No visibility into customer preferences at domain aggregate level.
    return true;
  }

  @Override
  public <R> R accept(@NonNull SpecificationVisitor<ShoppingCart, R> visitor) {
    if (visitor instanceof CartSpecificationVisitor<?> v) {
      @SuppressWarnings("unchecked")
      final CartSpecificationVisitor<R> cv = (CartSpecificationVisitor<R>) v;
      return cv.visit(this);
    }
    return visitor.visit(new AndSpecification<>(this, this));
  }
}
