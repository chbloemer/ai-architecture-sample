package de.sample.aiarchitecture.account.application.authenticateaccount;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;

/**
 * Input port for the Authenticate Account use case.
 */
public interface AuthenticateAccountInputPort
    extends UseCase<AuthenticateAccountCommand, AuthenticateAccountResult> {
}
