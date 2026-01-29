package de.sample.aiarchitecture.sharedkernel.domain.specificationificationificationificationification;

import org.jspecify.annotations.NonNull;

/** Logical NOT for a domain specification. */
public final class NotSpecification<T> implements CompositeSpecification<T> {
  private final CompositeSpecification<T> inner;

  public NotSpecification(@NonNull CompositeSpecification<T> inner) {
    this.inner = inner;
  }

  @Override
  public boolean isSatisfiedBy(@NonNull T candidate) {
    return !inner.isSatisfiedBy(candidate);
  }

  @Override
  public <R> R accept(@NonNull SpecificationVisitor<T, R> visitor) {
    return visitor.visit(this);
  }

  public CompositeSpecification<T> inner() { return inner; }
}
