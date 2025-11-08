package de.sample.aiarchitecture.cart.application.port.in;

import de.sample.aiarchitecture.cart.application.usecase.getallcarts.GetAllCartsQuery;
import de.sample.aiarchitecture.cart.application.usecase.getallcarts.GetAllCartsResponse;
import de.sample.aiarchitecture.sharedkernel.application.marker.InputPort;

/**
 * Input Port for retrieving all shopping carts.
 *
 * <p>This port defines the contract for the "Get All Carts" use case.
 */
public interface GetAllCartsInputPort extends InputPort<GetAllCartsQuery, GetAllCartsResponse> {}
