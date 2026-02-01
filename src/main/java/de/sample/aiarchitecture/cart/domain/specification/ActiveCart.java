package de.sample.aiarchitecture.cart.domain.specification;

import de.sample.aiarchitecture.cart.domain.model.CartStatus;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.specification.AndSpecification;
import de.sample.aiarchitecture.sharedkernel.domain.specification.SpecificationVisitor;
import org.jspecify.annotations.NonNull;

/**
 * Cart is in ACTIVE status.
 */
public record ActiveCart() implements CartSpecification {
  @Override
  public boolean isSatisfiedBy(@NonNull ShoppingCart candidate) {
    return candidate.status() == CartStatus.ACTIVE;
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
