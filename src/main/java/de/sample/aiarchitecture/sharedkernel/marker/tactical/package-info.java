/**
 * DDD Tactical Patterns - Building Blocks.
 *
 * <p>Marker interfaces for Domain-Driven Design tactical patterns that guide
 * domain model implementation:
 * <ul>
 *   <li>{@link Id} - Marker for identity value objects</li>
 *   <li>{@link Entity} - Objects with identity</li>
 *   <li>{@link Value} - Immutable objects defined by attributes</li>
 *   <li>{@link AggregateRoot} - Entry point to aggregate clusters</li>
 *   <li>{@link DomainEvent} - Important domain occurrences</li>
 *   <li>{@link DomainService} - Stateless domain operations</li>
 *   <li>{@link Factory} - Complex object creation</li>
 *   <li>{@link Specification} - Business rules as objects</li>
 *   <li>{@link StateInterest} - Interest Interface Pattern for Read Models</li>
 *   <li>{@link ReadModelBuilder} - Builds Read Models from events</li>
 * </ul>
 *
 * <p><b>References:</b>
 * <ul>
 *   <li>Eric Evans' Domain-Driven Design (2003)</li>
 *   <li>Vaughn Vernon's Implementing Domain-Driven Design (2013)</li>
 * </ul>
 */
package de.sample.aiarchitecture.sharedkernel.marker.tactical;
