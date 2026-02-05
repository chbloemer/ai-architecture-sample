package de.sample.aiarchitecture.cart.application.getcartbyid;

import de.sample.aiarchitecture.cart.domain.model.EnrichedCart;
import org.jspecify.annotations.Nullable;

/**
 * Output model for cart retrieval by ID.
 *
 * <p>Wraps the {@link EnrichedCart} read model. Check {@link #found()} to determine
 * if the cart exists before accessing {@link #cart()}.
 *
 * @param found whether the cart was found
 * @param cart the enriched cart read model (null if not found)
 */
public record GetCartByIdResult(
    boolean found,
    @Nullable EnrichedCart cart
) {

  /**
   * Creates a result for a cart that was not found.
   */
  public static GetCartByIdResult notFound() {
    return new GetCartByIdResult(false, null);
  }

  /**
   * Creates a result for a cart that was found.
   */
  public static GetCartByIdResult found(final EnrichedCart cart) {
    return new GetCartByIdResult(true, cart);
  }
}
