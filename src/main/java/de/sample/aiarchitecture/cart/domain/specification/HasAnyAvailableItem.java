package de.sample.aiarchitecture.cart.domain.specificationificationificationificationification;

import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.specificationificationificationificationification.AndSpecification;
import de.sample.aiarchitecture.sharedkernel.domain.specificationificationificationificationification.SpecificationVisitor;
import org.jspecify.annotations.NonNull;

/**
 * Cart contains at least one item that is considered "available".
 *
 * <p>Domain aggregate does not know product availability/discontinued flags;
 * therefore in-memory evaluation is neutral (true). The persistence adapter is
 * expected to push this down to the database (e.g., EXISTS over items and
 * optionally a join to products to check stock/discontinued) where possible.
 */
public record HasAnyAvailableItem() implements CartSpecification {

  @Override
  public boolean isSatisfiedBy(@NonNull ShoppingCart candidate) {
    // Domain model has no product availability view. Keep neutral in memory.
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
