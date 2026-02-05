package de.sample.aiarchitecture.cart.domain.specification;

import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.specification.CompositeSpecification;
import de.sample.aiarchitecture.sharedkernel.domain.specification.SpecificationVisitor;
import java.util.Objects;

/**
 * Legacy adapter wrapper. No longer needed now that repositories accept
 * {@link CompositeSpecification <ShoppingCart>} directly. Kept for source
 * compatibility if referenced elsewhere.
 */
public final class ComposedCartSpecification implements CompositeSpecification<ShoppingCart> {

  private final CompositeSpecification<ShoppingCart> delegate;

  public ComposedCartSpecification(final CompositeSpecification<ShoppingCart> delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
  }

  @Override
  public boolean isSatisfiedBy(final ShoppingCart candidate) {
    return delegate.isSatisfiedBy(candidate);
  }

  @Override
  public <R> R accept(final SpecificationVisitor<ShoppingCart, R> visitor) {
    return delegate.accept(visitor);
  }
}
