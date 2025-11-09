package de.sample.aiarchitecture.sharedkernel.domain.spec;

import de.sample.aiarchitecture.sharedkernel.domain.marker.Specification;
import org.jspecify.annotations.NonNull;

/**
 * Generic, framework-agnostic Specification interface for domain use.
 *
 * <p>This co-exists with the legacy marker {@code de.sample.aiarchitecture.sharedkernel.domain.marker.Specification}
 * without breaking existing code. Prefer this interface for new, composable specifications that
 * can be translated by adapters (e.g., to JPA predicates).
 */
public interface CompositeSpecification<T> extends Specification<T> {

  /** Returns true if the candidate satisfies this specification when evaluated in-memory. */
  boolean isSatisfiedBy(@NonNull T candidate);

  /**
   * Accepts a visitor to translate this specification into another representation (e.g.,
   * a JPA Specification for pushdown or a Mongo filter).
   */
  <R> R accept(@NonNull SpecificationVisitor<T, R> visitor);

  default CompositeSpecification<T> and(@NonNull CompositeSpecification<T> other) {
    return new AndSpecification<>(this, other);
  }

  default CompositeSpecification<T> or(@NonNull CompositeSpecification<T> other) {
    return new OrSpecification<>(this, other);
  }

  default CompositeSpecification<T> not() {
    return new NotSpecification<>(this);
  }
}
