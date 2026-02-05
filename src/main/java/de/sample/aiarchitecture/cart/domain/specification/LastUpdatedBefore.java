package de.sample.aiarchitecture.cart.domain.specification;

import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.specification.AndSpecification;
import de.sample.aiarchitecture.sharedkernel.domain.specification.SpecificationVisitor;
import java.time.Instant;

/**
 * Cart was last updated before the given threshold (exclusive).
 *
 * <p>Note: The domain aggregate currently doesn't expose an updatedAt timestamp,
 * so in-memory checks are neutral (returning true). Persistence adapters push
 * this predicate down to the database using entity timestamps.
 */
public record LastUpdatedBefore(Instant threshold) implements CartSpecification {
  @Override
  public boolean isSatisfiedBy(ShoppingCart candidate) {
    // Domain model lacks updatedAt, so we cannot evaluate accurately in-memory.
    return true;
  }

  @Override
  public <R> R accept(SpecificationVisitor<ShoppingCart, R> visitor) {
    if (visitor instanceof CartSpecificationVisitor<?> v) {
      @SuppressWarnings("unchecked")
      final CartSpecificationVisitor<R> cv = (CartSpecificationVisitor<R>) v;
      return cv.visit(this);
    }
    return visitor.visit(new AndSpecification<>(this, this));
  }
}
