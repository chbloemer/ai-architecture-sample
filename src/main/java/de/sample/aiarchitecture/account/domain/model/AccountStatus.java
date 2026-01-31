package de.sample.aiarchitecture.account.domain.model;

/**
 * Status of an Account.
 *
 * <p>Accounts can have the following statuses:
 * <ul>
 *   <li>ACTIVE - Normal operating state, can login</li>
 *   <li>SUSPENDED - Temporarily disabled, cannot login</li>
 *   <li>CLOSED - Permanently closed by user request</li>
 * </ul>
 */
public enum AccountStatus {

  /**
   * Account is active and can be used for login.
   */
  ACTIVE,

  /**
   * Account is temporarily suspended (e.g., suspicious activity).
   */
  SUSPENDED,

  /**
   * Account has been permanently closed.
   */
  CLOSED;

  /**
   * Checks if the account can be used for login.
   *
   * @return true if the account is active
   */
  public boolean canLogin() {
    return this == ACTIVE;
  }

  /**
   * Checks if the account is in a terminal state.
   *
   * @return true if the account is closed
   */
  public boolean isTerminal() {
    return this == CLOSED;
  }
}
