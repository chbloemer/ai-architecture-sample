package de.sample.aiarchitecture.account.adapter.outgoing.security;

import de.sample.aiarchitecture.account.application.shared.AccountRepository;
import de.sample.aiarchitecture.sharedkernel.application.port.security.RegisteredUserValidator;
import de.sample.aiarchitecture.sharedkernel.domain.common.UserId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * Implementation of RegisteredUserValidator that checks account existence.
 *
 * <p>This secondary adapter validates that a registered user's account exists
 * in the repository. It is used by the security infrastructure to detect
 * stale JWT tokens after application restart (when in-memory accounts are lost).
 */
@Component
public class AccountBasedRegisteredUserValidator implements RegisteredUserValidator {

  private final AccountRepository accountRepository;

  public AccountBasedRegisteredUserValidator(final AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public boolean existsForUserId(@NonNull final UserId userId) {
    return accountRepository.findByLinkedUserId(userId).isPresent();
  }
}
