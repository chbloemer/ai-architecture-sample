package de.sample.aiarchitecture.account.adapter.incoming.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for login endpoint.
 *
 * @param email the user's email address
 * @param password the user's password
 */
public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "Password is required")
    String password) {
}
