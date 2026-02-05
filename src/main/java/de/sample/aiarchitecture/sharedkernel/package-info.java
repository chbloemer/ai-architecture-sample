/**
 * Shared Kernel.
 *
 * <p>Common value objects, DDD markers, and cross-cutting concerns shared across all bounded
 * contexts. Changes require coordination between all teams.
 *
 * <p><b>Package Structure:</b>
 * <ul>
 *   <li>{@code marker/tactical/} - DDD tactical building blocks (Entity, Value, Aggregate, etc.)</li>
 *   <li>{@code marker/strategic/} - DDD strategic patterns (BoundedContext, SharedKernel)</li>
 *   <li>{@code marker/port/in/} - Input ports (UseCase, InputPort)</li>
 *   <li>{@code marker/port/out/} - Output ports (Repository, DomainEventPublisher, IdentityProvider)</li>
 *   <li>{@code domain/model/} - Universal value objects (Money, Price, ProductId, UserId)</li>
 *   <li>{@code domain/specification/} - Composable specification pattern</li>
 * </ul>
 */
@NullMarked
@SharedKernel(description = "Common value objects, DDD markers, and cross-cutting concerns")
package de.sample.aiarchitecture.sharedkernel;

import de.sample.aiarchitecture.sharedkernel.marker.strategic.SharedKernel;
import org.jspecify.annotations.NullMarked;

