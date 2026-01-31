package de.sample.aiarchitecture.account.application.registeraccount;

import de.sample.aiarchitecture.sharedkernel.application.port.InputPort;

/**
 * Input port for the Register Account use case.
 */
public interface RegisterAccountInputPort
    extends InputPort<RegisterAccountCommand, RegisterAccountResponse> {
}
