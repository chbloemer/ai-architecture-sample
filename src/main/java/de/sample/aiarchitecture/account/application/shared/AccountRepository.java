package de.sample.aiarchitecture.account.application.shared;

import de.sample.aiarchitecture.account.domain.model.Account;
import de.sample.aiarchitecture.account.domain.model.AccountId;
import de.sample.aiarchitecture.account.domain.model.Email;
import de.sample.aiarchitecture.sharedkernel.application.port.Repository;
import de.sample.aiarchitecture.sharedkernel.domain.common.UserId;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

/**
 * Repository interface for Account aggregate.
 *
 * <p>Provides collection-like access to Account aggregates using domain language.
 * Implementation resides in the secondary adapter layer.
 *
 * <p>Extends the base {@link Repository} interface which provides:
 * <ul>
 *   <li>{@code findById(AccountId)} - find by aggregate ID</li>
 *   <li>{@code save(Account)} - persist an account</li>
 *   <li>{@code deleteById(AccountId)} - remove an account</li>
 * </ul>
 */
public interface AccountRepository extends Repository<Account, AccountId> {

  /**
   * Finds an account by email address.
   *
   * <p>Email addresses are unique across all accounts.
   *
   * @param email the email address
   * @return the account if found, empty otherwise
   */
  Optional<Account> findByEmail(@NonNull Email email);

  /**
   * Finds an account by its linked UserId.
   *
   * <p>Each account is linked to exactly one UserId.
   *
   * @param userId the linked user ID
   * @return the account if found, empty otherwise
   */
  Optional<Account> findByLinkedUserId(@NonNull UserId userId);

  /**
   * Checks if an email is already registered.
   *
   * @param email the email to check
   * @return true if an account with this email exists
   */
  default boolean existsByEmail(@NonNull final Email email) {
    return findByEmail(email).isPresent();
  }
}
