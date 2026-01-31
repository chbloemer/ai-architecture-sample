package de.sample.aiarchitecture.checkout.application.getactivecheckoutsession;

import de.sample.aiarchitecture.sharedkernel.application.port.UseCase;

/**
 * Input Port for getting an active checkout session for a customer.
 *
 * <p>This port defines the contract for the "Get Active Checkout Session" use case,
 * which retrieves the active checkout session for a customer based on their identity.
 */
public interface GetActiveCheckoutSessionInputPort
    extends UseCase<GetActiveCheckoutSessionQuery, GetActiveCheckoutSessionResponse> {}
