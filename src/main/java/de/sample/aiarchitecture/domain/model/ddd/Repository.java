package de.sample.aiarchitecture.domain.model.ddd;

import java.util.Optional;
import org.jspecify.annotations.NonNull;

/**
 * Base interface for Repositories.
 *
 * <p>Repositories provide a collection-like interface for accessing Aggregate Roots. They
 * encapsulate the logic for retrieving and persisting aggregates, presenting the illusion of an
 * in-memory collection.
 *
 * <p><b>Key Principles:</b>
 *
 * <ul>
 *   <li>One Repository per Aggregate Root (not per Entity)
 *   <li>Repository interfaces belong in the domain layer
 *   <li>Repository implementations belong in secondary adapters (infrastructure layer)
 *   <li>Use domain language in method names (not generic CRUD)
 *   <li>Return domain objects, never infrastructure objects
 * </ul>
 *
 * <p><b>Characteristics:</b>
 *
 * <ul>
 *   <li>Interface resides in domain layer (e.g., {@code domain.model.product.ProductRepository})
 *   <li>Implementation resides in secondary adapters (e.g., {@code
 *       portadapter.secondary.product.InMemoryProductRepository})
 *   <li>Methods use ubiquitous language (e.g., {@code findBySku()}, {@code findByCategory()})
 *   <li>Should NOT have Spring annotations in the interface
 *   <li>Collections should be immutable when returned
 * </ul>
 *
 * <p><b>Common Methods:</b>
 *
 * <ul>
 *   <li>{@code findById(ID)} - Retrieve aggregate by its unique identifier
 *   <li>{@code save(T)} - Add or update an aggregate (collection metaphor)
 *   <li>{@code deleteById(ID)} - Remove an aggregate from the collection
 * </ul>
 *
 * <p><b>Example:</b>
 *
 * <pre>
 * // Domain layer interface
 * public interface ProductRepository extends Repository&lt;Product, ProductId&gt; {
 *   Optional&lt;Product&gt; findBySku(SKU sku);
 *   List&lt;Product&gt; findByCategory(Category category);
 *   boolean existsBySku(SKU sku);
 * }
 *
 * // Secondary adapter implementation
 * {@literal @}Repository
 * public class InMemoryProductRepository implements ProductRepository {
 *   // Implementation using in-memory storage
 * }
 * </pre>
 *
 * <p><b>Pattern:</b> Repositories mediate between the domain and data mapping layers using a
 * collection-like interface for accessing domain objects.
 *
 * <p><b>References:</b>
 *
 * <ul>
 *   <li>Eric Evans' Domain-Driven Design (2003), Chapter 6: "The Life Cycle of a Domain Object"
 *   <li>Vaughn Vernon's Implementing Domain-Driven Design (2013), Chapter 12: "Repositories"
 *   <li>Martin Fowler's <a href="https://martinfowler.com/eaaCatalog/repository.html">Repository
 *       Pattern</a>
 * </ul>
 *
 * @param <T> the aggregate root type
 * @param <ID> the aggregate root ID type
 * @see <a href="https://www.domainlanguage.com/ddd/">Domain-Driven Design Reference</a>
 */
public interface Repository<T extends AggregateRoot<T, ID>, ID extends Id> {

  /**
   * Finds an aggregate by its unique identifier.
   *
   * @param id the aggregate ID
   * @return an Optional containing the aggregate if found, empty otherwise
   */
  Optional<T> findById(@NonNull ID id);

  /**
   * Saves an aggregate to the repository.
   *
   * <p>This method adds a new aggregate or updates an existing one. The repository handles the
   * distinction based on the aggregate's identity.
   *
   * <p>After saving, domain events should be published by the application service.
   *
   * @param aggregate the aggregate to save
   * @return the saved aggregate
   */
  T save(@NonNull T aggregate);

  /**
   * Deletes an aggregate from the repository by its ID.
   *
   * <p>This method removes the aggregate from the collection. If the aggregate doesn't exist,
   * the behavior is implementation-specific (may throw exception or silently succeed).
   *
   * @param id the ID of the aggregate to delete
   */
  void deleteById(@NonNull ID id);
}
