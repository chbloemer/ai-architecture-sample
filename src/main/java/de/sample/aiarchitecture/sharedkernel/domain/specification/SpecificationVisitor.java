package de.sample.aiarchitecture.sharedkernel.domain.specificationificationificationificationification;

import org.jspecify.annotations.NonNull;

/**
 * Visitor for translating domain specifications to other representations.
 *
 * <p>Adapters implement this to convert a domain {@link CompositeSpecification} into a persistence- or
 * transport-specific representation (e.g., JPA Specification, Querydsl predicate, Mongo filter,
 * Elasticsearch query, etc.).
 */
public interface SpecificationVisitor<T, R> {
  R visit(@NonNull AndSpecification<T> spec);
  R visit(@NonNull OrSpecification<T> spec);
  R visit(@NonNull NotSpecification<T> spec);
}
