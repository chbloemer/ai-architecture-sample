package de.sample.aiarchitecture.product.adapter.incoming.event.acl;

import de.sample.aiarchitecture.cart.domain.event.CartCheckedOut;
import de.sample.aiarchitecture.product.application.reduceproductstock.ReduceProductStockCommand;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * Anti-Corruption Layer for translating Cart context events into Product context commands.
 *
 * <p><b>Purpose:</b> This ACL protects the Product context from changes in the Cart context's
 * event schema. It translates Cart's ubiquitous language into Product's ubiquitous language,
 * maintaining bounded context isolation.
 *
 * <p><b>Why Anti-Corruption Layer?</b>
 *
 * <ul>
 *   <li><b>Isolation:</b> Product context doesn't directly depend on Cart's event structure
 *   <li><b>Evolution:</b> Cart can change its event schema without breaking Product
 *   <li><b>Versioning:</b> Handles multiple event versions transparently
 *   <li><b>Translation:</b> Converts Cart's language to Product's language
 *   <li><b>Testability:</b> ACL can be tested independently
 * </ul>
 *
 * <p><b>Without ACL (tight coupling):</b>
 *
 * <pre>
 * // Product context directly uses Cart's event structure
 * event.items().forEach(item -&gt; {
 *   // If Cart changes ItemInfo structure, Product breaks!
 *   reduceStock(item.productId(), item.quantity());
 * });
 * </pre>
 *
 * <p><b>With ACL (loose coupling):</b>
 *
 * <pre>
 * // Product context uses ACL to translate
 * List&lt;ReduceProductStockCommand&gt; commands = acl.translate(event);
 * commands.forEach(command -&gt; reduceStock(command));
 * // Cart can change event structure, only ACL needs updating
 * </pre>
 *
 * <p><b>Version Handling:</b> This ACL supports multiple versions of CartCheckedOut events.
 * When Cart evolves the event schema (e.g., adds warehouse info in v2), the ACL handles
 * both versions:
 *
 * <ul>
 *   <li>v1: Basic cart checkout with product IDs and quantities
 *   <li>v2: Enhanced with warehouse allocation, batch numbers, etc. (future)
 * </ul>
 *
 * <p><b>DDD Pattern:</b> Anti-Corruption Layer from Eric Evans' Domain-Driven Design.
 * Recommended for all cross-context integrations to prevent upstream contexts from
 * "corrupting" downstream contexts with their domain models.
 *
 * <p><b>References:</b>
 *
 * <ul>
 *   <li>Eric Evans' "Domain-Driven Design" (2003), Chapter 14: "Maintaining Model Integrity"
 *   <li>Vaughn Vernon's "Implementing Domain-Driven Design" (2013), Chapter 13: "Integrating
 *       Bounded Contexts"
 * </ul>
 */
@Component
public class CartEventTranslator {

  /**
   * Translates CartCheckedOut event into Product context commands.
   *
   * <p>This method handles version-specific translation, allowing Cart to evolve its event
   * schema without breaking Product context. The ACL isolates Product from Cart's changes.
   *
   * @param event the cart checked out event from Cart context
   * @return list of commands in Product's ubiquitous language
   * @throws UnsupportedEventVersionException if the event version is not supported
   */
  @NonNull
  public List<ReduceProductStockCommand> translate(@NonNull final CartCheckedOut event) {
    return switch (event.version()) {
      case 1 -> translateV1(event);
      case 2 -> translateV2(event);
      default ->
          throw new UnsupportedEventVersionException(
              String.format(
                  "Unsupported CartCheckedOut event version: %d. Supported versions: 1, 2",
                  event.version()));
    };
  }

  /**
   * Translates version 1 of CartCheckedOut event.
   *
   * <p>Version 1 structure:
   *
   * <ul>
   *   <li>items: List of ItemInfo(ProductId, quantity)
   *   <li>Basic checkout information
   * </ul>
   *
   * @param event version 1 event
   * @return list of reduce stock commands
   */
  @NonNull
  private List<ReduceProductStockCommand> translateV1(@NonNull final CartCheckedOut event) {
    return event.items().stream()
        .map(
            item ->
                new ReduceProductStockCommand(
                    item.productId().value().toString(), item.quantity()))
        .toList();
  }

  /**
   * Translates version 2 of CartCheckedOut event (future implementation).
   *
   * <p>Version 2 (future) might include:
   *
   * <ul>
   *   <li>Warehouse allocation information
   *   <li>Batch numbers for inventory tracking
   *   <li>Reservation IDs for optimistic locking
   *   <li>Delivery location for multi-warehouse scenarios
   * </ul>
   *
   * <p>The ACL will extract only the data Product context needs (productId, quantity) and
   * ignore the rest, maintaining Product's focus on stock management.
   *
   * @param event version 2 event
   * @return list of reduce stock commands
   */
  @NonNull
  private List<ReduceProductStockCommand> translateV2(@NonNull final CartCheckedOut event) {
    // For now, v2 translates the same as v1
    // When v2 is actually implemented with additional fields, this method will:
    // 1. Extract the additional fields (warehouse, batch, etc.)
    // 2. Filter/map to only what Product context needs
    // 3. Potentially create enhanced commands if Product needs the extra data

    return translateV1(event);
  }

  /**
   * Exception thrown when an unsupported event version is encountered.
   *
   * <p>This prevents the system from processing events it doesn't understand, failing fast
   * instead of silently producing incorrect results.
   */
  public static class UnsupportedEventVersionException extends RuntimeException {
    public UnsupportedEventVersionException(final String message) {
      super(message);
    }
  }
}
