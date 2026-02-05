package de.sample.aiarchitecture.sharedkernel.domain.specification;

/**
 * Visitor for translating domain specifications to other representations.
 *
 * <p>Adapters implement this to convert a domain {@link CompositeSpecification} into a persistence- or
 * transport-specific representation (e.g., JPA Specification, Querydsl predicate, Mongo filter,
 * Elasticsearch query, etc.).
 */
public interface SpecificationVisitor<T, R> {
  R visit(AndSpecification<T> spec);
  R visit(OrSpecification<T> spec);
  R visit(NotSpecification<T> spec);
}
