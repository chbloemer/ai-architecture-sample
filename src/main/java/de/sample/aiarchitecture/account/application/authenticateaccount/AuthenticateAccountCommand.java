package de.sample.aiarchitecture.account.application.authenticateaccount;

import org.jspecify.annotations.NonNull;

/**
 * Command to authenticate an account (login).
 *
 * @param email the user's email address
 * @param password the user's plaintext password
 */
public record AuthenticateAccountCommand(
    @NonNull String email,
    @NonNull String password) {

  public AuthenticateAccountCommand {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email is required");
    }
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("Password is required");
    }
  }
}
