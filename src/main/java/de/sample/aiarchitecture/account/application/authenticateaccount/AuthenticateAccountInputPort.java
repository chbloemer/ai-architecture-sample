package de.sample.aiarchitecture.account.application.authenticateaccount;

import de.sample.aiarchitecture.sharedkernel.application.port.UseCase;

/**
 * Input port for the Authenticate Account use case.
 */
public interface AuthenticateAccountInputPort
    extends UseCase<AuthenticateAccountCommand, AuthenticateAccountResponse> {
}
