package de.sample.aiarchitecture.account.domain.model;

import de.sample.aiarchitecture.account.domain.event.AccountLinkedToIdentity;
import de.sample.aiarchitecture.account.domain.event.AccountRegistered;
import de.sample.aiarchitecture.account.domain.service.PasswordHasher;
import de.sample.aiarchitecture.sharedkernel.domain.model.UserId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.BaseAggregateRoot;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.jspecify.annotations.NonNull;

/**
 * Account Aggregate Root.
 *
 * <p>Represents a registered user account with credentials and profile information.
 * An Account is linked to a UserId which is shared across all bounded contexts.
 *
 * <p><b>Key Concepts:</b>
 * <ul>
 *   <li>AccountId - The aggregate root identity (internal)</li>
 *   <li>UserId - The cross-context identity (shared, in JWT)</li>
 *   <li>Email - The login credential (unique)</li>
 *   <li>HashedPassword - BCrypt hashed password (never stored as plaintext)</li>
 * </ul>
 *
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Email must be unique across all accounts</li>
 *   <li>Password must meet strength requirements (validated by domain)</li>
 *   <li>Each account is linked to exactly one UserId</li>
 *   <li>Cannot login if account is suspended or closed</li>
 * </ul>
 *
 * <p><b>Domain Events:</b>
 * <ul>
 *   <li>{@link AccountRegistered} - when account is created</li>
 *   <li>{@link AccountLinkedToIdentity} - when UserId is linked</li>
 * </ul>
 */
public final class Account extends BaseAggregateRoot<Account, AccountId> {

  private final AccountId id;
  private final Email email;
  private final UserId linkedUserId;
  private HashedPassword password;
  private AccountStatus status;
  private Set<String> roles;
  private final Instant createdAt;
  private Instant lastLoginAt;

  private Account(
      @NonNull final AccountId id,
      @NonNull final Email email,
      @NonNull final UserId linkedUserId,
      @NonNull final HashedPassword password,
      @NonNull final Set<String> roles) {
    this.id = id;
    this.email = email;
    this.linkedUserId = linkedUserId;
    this.password = password;
    this.status = AccountStatus.ACTIVE;
    this.roles = new HashSet<>(roles);
    this.createdAt = Instant.now();
  }

  /**
   * Factory method to register a new account.
   *
   * <p>This method:
   * <ol>
   *   <li>Validates password strength</li>
   *   <li>Hashes the password</li>
   *   <li>Generates a new AccountId</li>
   *   <li>Links the existing UserId (preserving cart and checkout session)</li>
   *   <li>Raises AccountRegistered and AccountLinkedToIdentity events</li>
   * </ol>
   *
   * <p>The UserId remains unchanged during registration. This ensures continuity
   * of cart items and checkout session for users who register during checkout.
   *
   * @param email the user's email address (login credential)
   * @param plainPassword the plaintext password (will be validated and hashed)
   * @param currentUserId the UserId to link to (from the user's JWT)
   * @param passwordHasher the password hasher domain service
   * @return a new Account instance
   * @throws IllegalArgumentException if email or password is invalid
   */
  public static Account register(
      @NonNull final Email email,
      @NonNull final String plainPassword,
      @NonNull final UserId currentUserId,
      @NonNull final PasswordHasher passwordHasher) {

    final HashedPassword hashedPassword = HashedPassword.fromPlaintext(plainPassword, passwordHasher);
    final AccountId accountId = AccountId.generate();

    // UserId remains unchanged - no prefix conversion needed
    // This preserves cart and checkout session data
    final Set<String> defaultRoles = Set.of("CUSTOMER");

    final Account account = new Account(
        accountId, email, currentUserId, hashedPassword, defaultRoles);

    // Raise domain events
    account.registerEvent(AccountRegistered.now(accountId, email, currentUserId));
    account.registerEvent(AccountLinkedToIdentity.now(accountId, currentUserId));

    return account;
  }

  /**
   * Reconstructs an Account from persistence.
   *
   * <p>Used by repositories when loading accounts from storage.
   *
   * @param id the account ID
   * @param email the email
   * @param linkedUserId the linked user ID
   * @param hashedPassword the BCrypt password hash
   * @param status the account status
   * @param roles the account roles
   * @param createdAt when the account was created
   * @param lastLoginAt when the user last logged in
   * @return the reconstructed Account
   */
  public static Account reconstitute(
      final AccountId id,
      final Email email,
      final UserId linkedUserId,
      final HashedPassword hashedPassword,
      final AccountStatus status,
      final Set<String> roles,
      final Instant createdAt,
      final Instant lastLoginAt) {
    final Account account = new Account(id, email, linkedUserId, hashedPassword, roles);
    account.status = status;
    account.lastLoginAt = lastLoginAt;
    return account;
  }

  @Override
  public AccountId id() {
    return id;
  }

  public Email email() {
    return email;
  }

  public UserId linkedUserId() {
    return linkedUserId;
  }

  public AccountStatus status() {
    return status;
  }

  public Set<String> roles() {
    return Set.copyOf(roles);
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant lastLoginAt() {
    return lastLoginAt;
  }

  /**
   * Gets the hashed password for persistence.
   *
   * <p>Use {@link #checkPassword(String, PasswordHasher)} for authentication.
   *
   * @return the hashed password
   */
  public HashedPassword password() {
    return password;
  }

  /**
   * Verifies if the given plaintext password matches the account's password.
   *
   * @param plainPassword the plaintext password to verify
   * @param passwordHasher the password hasher domain service
   * @return true if the password matches
   */
  public boolean checkPassword(
      @NonNull final String plainPassword,
      @NonNull final PasswordHasher passwordHasher) {
    return password.matches(plainPassword, passwordHasher);
  }

  /**
   * Records a successful login.
   *
   * <p>Updates the last login timestamp.
   *
   * @throws IllegalStateException if the account cannot login
   */
  public void recordLogin() {
    if (!status.canLogin()) {
      throw new IllegalStateException("Cannot login with account status: " + status);
    }
    this.lastLoginAt = Instant.now();
  }

  /**
   * Changes the account password.
   *
   * @param newPlainPassword the new plaintext password (will be validated and hashed)
   * @param passwordHasher the password hasher domain service
   * @throws IllegalStateException if the account is closed
   * @throws IllegalArgumentException if the password doesn't meet requirements
   */
  public void changePassword(
      @NonNull final String newPlainPassword,
      @NonNull final PasswordHasher passwordHasher) {
    if (status.isTerminal()) {
      throw new IllegalStateException("Cannot change password on closed account");
    }
    this.password = HashedPassword.fromPlaintext(newPlainPassword, passwordHasher);
  }

  /**
   * Suspends the account.
   *
   * @throws IllegalStateException if the account is already closed
   */
  public void suspend() {
    if (status.isTerminal()) {
      throw new IllegalStateException("Cannot suspend closed account");
    }
    this.status = AccountStatus.SUSPENDED;
  }

  /**
   * Reactivates a suspended account.
   *
   * @throws IllegalStateException if the account is not suspended
   */
  public void reactivate() {
    if (status != AccountStatus.SUSPENDED) {
      throw new IllegalStateException("Can only reactivate suspended accounts");
    }
    this.status = AccountStatus.ACTIVE;
  }

  /**
   * Closes the account permanently.
   *
   * @throws IllegalStateException if the account is already closed
   */
  public void close() {
    if (status.isTerminal()) {
      throw new IllegalStateException("Account is already closed");
    }
    this.status = AccountStatus.CLOSED;
  }

  /**
   * Adds a role to the account.
   *
   * @param role the role to add
   */
  public void addRole(final String role) {
    this.roles.add(role);
  }

  /**
   * Removes a role from the account.
   *
   * @param role the role to remove
   */
  public void removeRole(final String role) {
    this.roles.remove(role);
  }
}
