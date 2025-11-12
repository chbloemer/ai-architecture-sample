package de.sample.aiarchitecture.cart.application.getallcarts;

import de.sample.aiarchitecture.sharedkernel.application.marker.UseCase;

/**
 * Input Port for retrieving all shopping carts.
 *
 * <p>This port defines the contract for the "Get All Carts" use case.
 */
public interface GetAllCartsInputPort extends UseCase<GetAllCartsQuery, GetAllCartsResponse> {}
