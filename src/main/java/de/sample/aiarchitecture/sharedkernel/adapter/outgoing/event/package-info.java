/**
 * Outgoing adapters for domain event publishing.
 *
 * <p>This package contains implementations of output ports related to domain event publishing.
 * These adapters bridge the domain layer to Spring's event infrastructure.
 *
 * <p><b>Contents:</b>
 * <ul>
 *   <li>{@link de.sample.aiarchitecture.sharedkernel.adapter.outgoing.event.SpringDomainEventPublisher}
 *       - Spring-based implementation of DomainEventPublisher</li>
 * </ul>
 *
 * <p><b>Architectural Location:</b> Outgoing adapters in the shared kernel provide cross-cutting
 * infrastructure that all bounded contexts can use without depending on context-specific code.
 */
@org.jspecify.annotations.NullMarked
package de.sample.aiarchitecture.sharedkernel.adapter.outgoing.event;
