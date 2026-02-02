package de.sample.aiarchitecture.cart.application.removeitemfromcart;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;

/**
 * Input Port for removing an item from a shopping cart.
 *
 * <p>This port defines the contract for the "Remove Item from Cart" use case,
 * following the Hexagonal Architecture pattern where ports define boundaries
 * between the application core and adapters.
 */
public interface RemoveItemFromCartInputPort
    extends UseCase<RemoveItemFromCartCommand, RemoveItemFromCartResult> {}
