package de.sample.aiarchitecture.sharedkernel.application.common.security;

/**
 * Represents the type of user identity.
 *
 * <p>A user starts as ANONYMOUS on first visit and becomes REGISTERED
 * after creating an account.
 */
public enum IdentityType {

  /**
   * Anonymous user with soft identity (JWT without account).
   */
  ANONYMOUS,

  /**
   * Registered user with hard identity (JWT linked to account).
   */
  REGISTERED
}
