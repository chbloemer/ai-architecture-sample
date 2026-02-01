package de.sample.aiarchitecture.account.adapter.outgoing.persistence;

import de.sample.aiarchitecture.account.application.shared.AccountRepository;
import de.sample.aiarchitecture.account.domain.model.Account;
import de.sample.aiarchitecture.account.domain.model.AccountId;
import de.sample.aiarchitecture.account.domain.model.Email;
import de.sample.aiarchitecture.sharedkernel.domain.model.UserId;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of AccountRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for accounts using
 * ConcurrentHashMap. In a production system, this would be replaced with a database implementation.
 *
 * <p>Note: No profile restriction - this is the default implementation until JPA/JDBC
 * implementations are added for the account bounded context.
 */
@Repository
public class InMemoryAccountRepository implements AccountRepository {

  private final ConcurrentHashMap<AccountId, Account> accounts = new ConcurrentHashMap<>();

  // Secondary indexes for efficient lookups
  private final ConcurrentHashMap<String, AccountId> emailIndex = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, AccountId> userIdIndex = new ConcurrentHashMap<>();

  @Override
  public Optional<Account> findById(@NonNull final AccountId id) {
    return Optional.ofNullable(accounts.get(id));
  }

  @Override
  public Optional<Account> findByEmail(@NonNull final Email email) {
    final AccountId accountId = emailIndex.get(email.value());
    if (accountId == null) {
      return Optional.empty();
    }
    return findById(accountId);
  }

  @Override
  public Optional<Account> findByLinkedUserId(@NonNull final UserId userId) {
    final AccountId accountId = userIdIndex.get(userId.value());
    if (accountId == null) {
      return Optional.empty();
    }
    return findById(accountId);
  }

  @Override
  public Account save(@NonNull final Account account) {
    // Update main storage
    accounts.put(account.id(), account);

    // Update indexes
    emailIndex.put(account.email().value(), account.id());
    userIdIndex.put(account.linkedUserId().value(), account.id());

    return account;
  }

  @Override
  public void deleteById(@NonNull final AccountId id) {
    final Account account = accounts.remove(id);
    if (account != null) {
      emailIndex.remove(account.email().value());
      userIdIndex.remove(account.linkedUserId().value());
    }
  }
}
