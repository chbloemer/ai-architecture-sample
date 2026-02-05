package de.sample.aiarchitecture.cart.domain.specification;

import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.specification.AndSpecification;
import de.sample.aiarchitecture.sharedkernel.domain.specification.SpecificationVisitor;

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
  public boolean isSatisfiedBy(ShoppingCart candidate) {
    // Domain model has no product availability view. Keep neutral in memory.
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
