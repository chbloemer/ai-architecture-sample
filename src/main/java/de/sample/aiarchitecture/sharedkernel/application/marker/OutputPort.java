package de.sample.aiarchitecture.sharedkernel.application.marker;

/**
 * Marker interface for Output Ports (Hexagonal Architecture).
 *
 * <p>An output port represents an interface that the application layer uses to interact
 * with external systems or infrastructure. In Hexagonal Architecture, output ports are
 * implemented by secondary/driven adapters (e.g., repository adapters, API clients,
 * event publishers).
 *
 * <p><b>Characteristics of Output Ports:</b>
 * <ul>
 *   <li>Define infrastructure needs using domain language</li>
 *   <li>Are interfaces defined in the application layer</li>
 *   <li>Are implemented by outgoing adapters</li>
 *   <li>Use domain types, not infrastructure types</li>
 *   <li>Enable Dependency Inversion Principle</li>
 *   <li>Technology-agnostic (no framework dependencies)</li>
 * </ul>
 *
 * <p><b>Examples of Output Ports:</b>
 * <ul>
 *   <li>Repository interfaces (e.g., ProductRepository, OrderRepository)</li>
 *   <li>Gateway interfaces (e.g., PaymentGateway, InventoryService)</li>
 *   <li>Event publisher interfaces (e.g., DomainEventPublisher)</li>
 *   <li>External API client interfaces (e.g., EmailService, NotificationService)</li>
 * </ul>
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * // Output Port (interface in application layer)
 * public interface ProductRepository extends OutputPort {
 *     Product save(Product product);
 *     Optional<Product> findById(ProductId id);
 *     List<Product> findAll();
 * }
 *
 * // Output Adapter (implementation in adapter layer)
 * public class ProductRepositoryAdapter implements ProductRepository {
 *     // Implementation using JPA, MongoDB, etc.
 * }
 * }</pre>
 *
 * <p><b>Dependency Inversion:</b>
 * Output ports enable the Dependency Inversion Principle by having the application layer
 * define the interface (what it needs) while the adapter layer provides the implementation
 * (how it's done). This allows the domain and application layers to remain independent
 * of infrastructure details.
 *
 * <p><b>References:</b>
 * <ul>
 *   <li>Alistair Cockburn - Hexagonal Architecture (Ports & Adapters)</li>
 *   <li>Robert C. Martin - Clean Architecture (The Dependency Rule)</li>
 *   <li>Tom Hombergs - Get Your Hands Dirty on Clean Architecture</li>
 * </ul>
 */
public interface OutputPort {
  // Marker interface - no methods
}
