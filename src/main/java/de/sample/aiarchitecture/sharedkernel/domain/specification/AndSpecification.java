package de.sample.aiarchitecture.sharedkernel.domain.specification;

/** Logical AND for two domain specifications. */
public final class AndSpecification<T> implements CompositeSpecification<T> {
  private final CompositeSpecification<T> left;
  private final CompositeSpecification<T> right;

  public AndSpecification(CompositeSpecification<T> left, CompositeSpecification<T> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public boolean isSatisfiedBy(T candidate) {
    return left.isSatisfiedBy(candidate) && right.isSatisfiedBy(candidate);
  }

  @Override
  public <R> R accept(SpecificationVisitor<T, R> visitor) {
    return visitor.visit(this);
  }

  public CompositeSpecification<T> left() { return left; }
  public CompositeSpecification<T> right() { return right; }
}
