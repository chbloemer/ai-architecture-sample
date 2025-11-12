package de.sample.aiarchitecture.cart.application.removeitemfromcart;

import de.sample.aiarchitecture.cart.application.removeitemfromcart.RemoveItemFromCartCommand;
import de.sample.aiarchitecture.cart.application.removeitemfromcart.RemoveItemFromCartResponse;
import de.sample.aiarchitecture.sharedkernel.application.marker.InputPort;

/**
 * Input Port for removing an item from a shopping cart.
 *
 * <p>This port defines the contract for the "Remove Item from Cart" use case,
 * following the Hexagonal Architecture pattern where ports define boundaries
 * between the application core and adapters.
 */
public interface RemoveItemFromCartInputPort
    extends InputPort<RemoveItemFromCartCommand, RemoveItemFromCartResponse> {}
