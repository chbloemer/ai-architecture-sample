package de.sample.aiarchitecture.product.adapter.incoming.event.acl;

import de.sample.aiarchitecture.checkout.domain.event.CheckoutConfirmed;
import de.sample.aiarchitecture.product.application.reduceproductstock.ReduceProductStockCommand;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * Anti-Corruption Layer for translating Checkout context events into Product context commands.
 *
 * <p><b>Purpose:</b> This ACL protects the Product context from changes in the Checkout context's
 * event schema. It translates Checkout's ubiquitous language into Product's ubiquitous language,
 * maintaining bounded context isolation.
 *
 * <p><b>Why Anti-Corruption Layer?</b>
 *
 * <ul>
 *   <li><b>Isolation:</b> Product context doesn't directly depend on Checkout's event structure
 *   <li><b>Evolution:</b> Checkout can change its event schema without breaking Product
 *   <li><b>Versioning:</b> Handles multiple event versions transparently
 *   <li><b>Translation:</b> Converts Checkout's language to Product's language
 * </ul>
 *
 * <p><b>DDD Pattern:</b> Anti-Corruption Layer from Eric Evans' Domain-Driven Design.
 */
@Component
public class CheckoutEventTranslator {

  /**
   * Translates CheckoutConfirmed event into Product context commands.
   *
   * <p>This method handles version-specific translation, allowing Checkout to evolve its event
   * schema without breaking Product context. The ACL isolates Product from Checkout's changes.
   *
   * @param event the checkout confirmed event from Checkout context
   * @return list of commands in Product's ubiquitous language
   * @throws UnsupportedEventVersionException if the event version is not supported
   */
  @NonNull
  public List<ReduceProductStockCommand> translate(@NonNull final CheckoutConfirmed event) {
    return switch (event.version()) {
      case 1 -> translateV1(event);
      default ->
          throw new UnsupportedEventVersionException(
              String.format(
                  "Unsupported CheckoutConfirmed event version: %d. Supported versions: 1",
                  event.version()));
    };
  }

  /**
   * Translates version 1 of CheckoutConfirmed event.
   *
   * @param event version 1 event
   * @return list of reduce stock commands
   */
  @NonNull
  private List<ReduceProductStockCommand> translateV1(@NonNull final CheckoutConfirmed event) {
    return event.items().stream()
        .map(
            item ->
                new ReduceProductStockCommand(
                    item.productId().value().toString(), item.quantity()))
        .toList();
  }

  /**
   * Exception thrown when an unsupported event version is encountered.
   */
  public static class UnsupportedEventVersionException extends RuntimeException {
    public UnsupportedEventVersionException(final String message) {
      super(message);
    }
  }
}
