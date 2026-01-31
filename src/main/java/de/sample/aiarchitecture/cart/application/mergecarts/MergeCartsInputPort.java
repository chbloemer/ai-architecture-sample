package de.sample.aiarchitecture.cart.application.mergecarts;

import de.sample.aiarchitecture.sharedkernel.application.port.UseCase;

/**
 * Input Port for merging carts based on user's choice.
 *
 * <p>This port defines the contract for the "Merge Carts" use case,
 * which executes the user's chosen strategy for handling conflicting
 * carts when logging in.
 *
 * <p><b>Hexagonal Architecture:</b> This is a primary/driving port that adapters
 * (e.g., web controllers) use to invoke the use case.
 */
public interface MergeCartsInputPort
    extends UseCase<MergeCartsCommand, MergeCartsResponse> {}
