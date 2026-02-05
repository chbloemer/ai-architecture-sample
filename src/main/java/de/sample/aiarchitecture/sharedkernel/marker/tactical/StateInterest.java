package de.sample.aiarchitecture.sharedkernel.marker.tactical;

/**
 * Marker interface for State Interest interfaces (Interest Interface Pattern).
 *
 * <p>The Interest Interface Pattern (also known as State Mediator) is a technique for building Read
 * Models from Domain Events. A StateInterest interface declares the events that a Read Model is
 * interested in receiving.
 *
 * <p><b>Pattern Overview:</b>
 *
 * <ul>
 *   <li>Domain Events are published when aggregate state changes
 *   <li>StateInterest interfaces declare which events a Read Model cares about
 *   <li>{@link ReadModelBuilder} classes implement StateInterest interfaces
 *   <li>Event handlers invoke the corresponding method on the Read Model Builder
 * </ul>
 *
 * <p><b>When to Use:</b>
 *
 * <ul>
 *   <li>Building optimized Read Models from Domain Events
 *   <li>CQRS implementations with separate read and write models
 *   <li>Projecting event streams into queryable views
 *   <li>Decoupling Read Model construction from event infrastructure
 * </ul>
 *
 * <p><b>Example:</b>
 *
 * <pre>
 * // StateInterest interface declares events of interest
 * public interface ProductCatalogInterest extends StateInterest {
 *     void on(ProductCreated event);
 *     void on(ProductPriceChanged event);
 *     void on(ProductDiscontinued event);
 * }
 *
 * // ReadModelBuilder implements the interest interface
 * public class ProductCatalogProjection
 *         implements ProductCatalogInterest, ReadModelBuilder {
 *
 *     &#64;Override
 *     public void on(ProductCreated event) {
 *         // Update read model with new product
 *     }
 *
 *     &#64;Override
 *     public void on(ProductPriceChanged event) {
 *         // Update product price in read model
 *     }
 * }
 * </pre>
 *
 * <p><b>Benefits:</b>
 *
 * <ul>
 *   <li>Type-safe event handling - compiler catches missing handlers
 *   <li>Clear contract - interface documents which events affect the read model
 *   <li>Testability - can verify read model behavior with mock events
 *   <li>Decoupling - separates event routing from read model logic
 * </ul>
 *
 * <p><b>Reference:</b> Vaughn Vernon's Implementing Domain-Driven Design (2013), Chapter 14:
 * "Application" - State Mediator Pattern (Interest Interface)
 *
 * @see ReadModelBuilder
 * @see DomainEvent
 * @see <a href="https://www.domainlanguage.com/ddd/">Domain-Driven Design Reference</a>
 */
public interface StateInterest {}
