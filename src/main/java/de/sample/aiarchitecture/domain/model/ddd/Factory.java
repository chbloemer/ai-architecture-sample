package de.sample.aiarchitecture.domain.model.ddd;

/**
 * Marker interface for Factories.
 *
 * <p>Factories encapsulate complex object creation logic, particularly for Aggregates and Entities.
 * They ensure invariants are maintained from the moment of creation and hide complex construction
 * details.
 *
 * <p><b>When to Use:</b>
 *
 * <ul>
 *   <li>Object construction is complex and involves multiple steps
 *   <li>Creation requires knowledge not belonging to the object itself
 *   <li>The constructor would violate the object's invariants
 *   <li>Need to create different configurations of the same type
 * </ul>
 *
 * <p><b>Characteristics:</b>
 *
 * <ul>
 *   <li>Stateless or with minimal state
 *   <li>Return fully formed, valid objects
 *   <li>Should NOT have Spring annotations (@Component, @Service)
 *   <li>Part of the domain model
 * </ul>
 *
 * <p><b>Example:</b>
 *
 * <pre>
 * public class ProductFactory implements Factory {
 *   public Product createProduct(ProductName name, SKU sku, Price price) {
 *     // Complex construction logic and validation
 *     return new Product(...);
 *   }
 * }
 * </pre>
 *
 * <p><b>Alternative:</b> For simple cases, static factory methods on the domain object itself
 * (e.g., {@code Product.of(...)}) are preferred over separate Factory classes.
 *
 * <p><b>Reference:</b> Eric Evans' Domain-Driven Design (2003), Chapter 6: "The Life Cycle of a
 * Domain Object"
 *
 * @see <a href="https://www.domainlanguage.com/ddd/">Domain-Driven Design Reference</a>
 */
public interface Factory {}
