package de.sample.aiarchitecture.account.application.registeraccount;

import de.sample.aiarchitecture.account.application.shared.AccountRepository;
import de.sample.aiarchitecture.account.domain.model.Account;
import de.sample.aiarchitecture.account.domain.model.Email;
import de.sample.aiarchitecture.account.domain.service.PasswordHasher;
import de.sample.aiarchitecture.sharedkernel.application.port.DomainEventPublisher;
import de.sample.aiarchitecture.sharedkernel.domain.common.UserId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for registering a new account.
 *
 * <p>This use case:
 * <ol>
 *   <li>Validates the email is not already registered</li>
 *   <li>Validates password strength requirements (delegated to domain)</li>
 *   <li>Hashes the password (delegated to domain)</li>
 *   <li>Creates a new Account</li>
 *   <li>Links the Account to the user's current UserId</li>
 *   <li>Publishes domain events (AccountRegistered, AccountLinkedToIdentity)</li>
 *   <li>Returns information needed to issue a new JWT</li>
 * </ol>
 *
 * <p>After registration, the controller should:
 * <ol>
 *   <li>Generate a new registered JWT with the returned userId, email, and roles</li>
 *   <li>Set the JWT cookie to upgrade the user from anonymous to registered</li>
 * </ol>
 */
@Service
public class RegisterAccountUseCase implements RegisterAccountInputPort {

  private final AccountRepository accountRepository;
  private final PasswordHasher passwordHasher;
  private final DomainEventPublisher eventPublisher;

  public RegisterAccountUseCase(
      final AccountRepository accountRepository,
      final PasswordHasher passwordHasher,
      final DomainEventPublisher eventPublisher) {
    this.accountRepository = accountRepository;
    this.passwordHasher = passwordHasher;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  @NonNull
  public RegisterAccountResponse execute(@NonNull final RegisterAccountCommand command) {
    final Email email = Email.of(command.email());

    // Check if email is already registered
    if (accountRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Email is already registered: " + email.value());
    }

    final UserId currentUserId = UserId.of(command.currentUserId());

    // Check if this user already has an account
    if (accountRepository.findByLinkedUserId(currentUserId).isPresent()) {
      throw new IllegalStateException("User already has an account");
    }

    // Create the account (password validation and hashing done by domain)
    final Account account = Account.register(email, command.password(), currentUserId, passwordHasher);

    // Persist the account
    accountRepository.save(account);

    // Publish domain events
    account.domainEvents().forEach(eventPublisher::publish);
    account.clearDomainEvents();

    return RegisterAccountResponse.of(
        account.id().value(),
        account.linkedUserId().value(),
        account.email().value(),
        account.roles());
  }
}
