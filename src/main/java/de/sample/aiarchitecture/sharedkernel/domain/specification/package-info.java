/**
 * Composable Specification Pattern Implementation.
 *
 * <p>This package provides a framework-agnostic implementation of the Specification pattern
 * that supports composition (AND, OR, NOT) and visitor-based translation to persistence
 * representations.
 *
 * <p><b>Contents:</b>
 * <ul>
 *   <li>{@link CompositeSpecification} - Base interface for composable specifications</li>
 *   <li>{@link AndSpecification} - Logical AND composition</li>
 *   <li>{@link OrSpecification} - Logical OR composition</li>
 *   <li>{@link NotSpecification} - Logical NOT composition</li>
 *   <li>{@link SpecificationVisitor} - Visitor for translating to persistence queries</li>
 * </ul>
 *
 * <p><b>Usage:</b> Adapters implement {@link SpecificationVisitor} to translate domain
 * specifications to JPA predicates, MongoDB filters, Elasticsearch queries, etc.
 */
package de.sample.aiarchitecture.sharedkernel.domain.specification;
