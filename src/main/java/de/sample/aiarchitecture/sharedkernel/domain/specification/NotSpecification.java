package de.sample.aiarchitecture.sharedkernel.domain.specification;

/** Logical NOT for a domain specification. */
public final class NotSpecification<T> implements CompositeSpecification<T> {
  private final CompositeSpecification<T> inner;

  public NotSpecification(CompositeSpecification<T> inner) {
    this.inner = inner;
  }

  @Override
  public boolean isSatisfiedBy(T candidate) {
    return !inner.isSatisfiedBy(candidate);
  }

  @Override
  public <R> R accept(SpecificationVisitor<T, R> visitor) {
    return visitor.visit(this);
  }

  public CompositeSpecification<T> inner() { return inner; }
}
