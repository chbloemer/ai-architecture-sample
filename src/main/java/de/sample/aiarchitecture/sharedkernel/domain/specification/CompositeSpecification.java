package de.sample.aiarchitecture.sharedkernel.domain.specification;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Specification;

/**
 * Generic, framework-agnostic Specification interface for domain use.
 *
 * <p>This extends the marker {@link Specification} interface with composition
 * and visitor support. Prefer this interface for new, composable specifications
 * that can be translated by adapters (e.g., to JPA predicates).
 */
public interface CompositeSpecification<T> extends Specification<T> {

  /** Returns true if the candidate satisfies this specification when evaluated in-memory. */
  boolean isSatisfiedBy(T candidate);

  /**
   * Accepts a visitor to translate this specification into another representation (e.g.,
   * a JPA Specification for pushdown or a Mongo filter).
   */
  <R> R accept(SpecificationVisitor<T, R> visitor);

  default CompositeSpecification<T> and(CompositeSpecification<T> other) {
    return new AndSpecification<>(this, other);
  }

  default CompositeSpecification<T> or(CompositeSpecification<T> other) {
    return new OrSpecification<>(this, other);
  }

  default CompositeSpecification<T> not() {
    return new NotSpecification<>(this);
  }
}
