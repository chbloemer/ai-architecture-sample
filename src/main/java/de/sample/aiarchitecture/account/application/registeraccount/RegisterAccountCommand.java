package de.sample.aiarchitecture.account.application.registeraccount;

import org.jspecify.annotations.NonNull;

/**
 * Command to register a new account.
 *
 * @param email the user's email address (will be their login credential)
 * @param password the user's plaintext password (will be hashed)
 * @param currentUserId the current user's ID (from their anonymous JWT)
 */
public record RegisterAccountCommand(
    @NonNull String email,
    @NonNull String password,
    @NonNull String currentUserId) {

  public RegisterAccountCommand {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email is required");
    }
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("Password is required");
    }
    if (currentUserId == null || currentUserId.isBlank()) {
      throw new IllegalArgumentException("Current user ID is required");
    }
  }
}
