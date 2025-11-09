package de.sample.aiarchitecture.sharedkernel.domain.spec;

import org.jspecify.annotations.NonNull;

/** Logical OR for two domain specifications. */
public final class OrSpecification<T> implements CompositeSpecification<T> {
  private final CompositeSpecification<T> left;
  private final CompositeSpecification<T> right;

  public OrSpecification(@NonNull CompositeSpecification<T> left, @NonNull CompositeSpecification<T> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public boolean isSatisfiedBy(@NonNull T candidate) {
    return left.isSatisfiedBy(candidate) || right.isSatisfiedBy(candidate);
  }

  @Override
  public <R> R accept(@NonNull SpecificationVisitor<T, R> visitor) {
    return visitor.visit(this);
  }

  public CompositeSpecification<T> left() { return left; }
  public CompositeSpecification<T> right() { return right; }
}
