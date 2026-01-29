package de.sample.aiarchitecture.cart.domain.specificationificationificationificationification;

import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.specificationificationificationificationification.CompositeSpecification;
import de.sample.aiarchitecture.sharedkernel.domain.specificationificationificationificationification.SpecificationVisitor;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Legacy adapter wrapper. No longer needed now that repositories accept
 * {@link CompositeSpecification <ShoppingCart>} directly. Kept for source
 * compatibility if referenced elsewhere.
 */
public final class ComposedCartSpecification implements CompositeSpecification<ShoppingCart> {

  private final CompositeSpecification<ShoppingCart> delegate;

  public ComposedCartSpecification(@NonNull final CompositeSpecification<ShoppingCart> delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
  }

  @Override
  public boolean isSatisfiedBy(@NonNull final ShoppingCart candidate) {
    return delegate.isSatisfiedBy(candidate);
  }

  @Override
  public <R> R accept(@NonNull final SpecificationVisitor<ShoppingCart, R> visitor) {
    return delegate.accept(visitor);
  }
}
