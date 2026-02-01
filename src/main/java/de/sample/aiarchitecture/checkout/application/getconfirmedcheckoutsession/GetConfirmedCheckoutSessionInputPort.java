package de.sample.aiarchitecture.checkout.application.getconfirmedcheckoutsession;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;

/**
 * Input Port for getting a confirmed or completed checkout session for a customer.
 *
 * <p>This port defines the contract for the "Get Confirmed Checkout Session" use case,
 * which retrieves a confirmed or completed checkout session for displaying the
 * confirmation/thank you page.
 */
public interface GetConfirmedCheckoutSessionInputPort
    extends UseCase<GetConfirmedCheckoutSessionQuery, GetConfirmedCheckoutSessionResponse> {}
