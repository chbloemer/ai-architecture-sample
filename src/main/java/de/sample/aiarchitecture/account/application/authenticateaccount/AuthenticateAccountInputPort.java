package de.sample.aiarchitecture.account.application.authenticateaccount;

import de.sample.aiarchitecture.sharedkernel.application.port.InputPort;

/**
 * Input port for the Authenticate Account use case.
 */
public interface AuthenticateAccountInputPort
    extends InputPort<AuthenticateAccountCommand, AuthenticateAccountResponse> {
}
