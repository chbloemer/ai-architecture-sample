package de.sample.aiarchitecture.sharedkernel.application.port.security;

import de.sample.aiarchitecture.sharedkernel.application.common.security.Identity;
import org.jspecify.annotations.NonNull;

/**
 * Port for retrieving the current user's identity.
 *
 * <p>This is the primary way for application services and controllers
 * to access the current user's identity without depending on infrastructure
 * details like HTTP sessions or JWT tokens.
 *
 * <p>The implementation (in the infrastructure layer) extracts the identity
 * from the security context, which is populated by the JWT filter.
 *
 * <p><b>Usage in Controllers:</b>
 * <pre>{@code
 * @GetMapping("/cart")
 * public String showCart(Model model) {
 *     UserId userId = identityProvider.getCurrentIdentity().userId();
 *     CustomerId customerId = CustomerId.of(userId.value());
 *     // ... use customerId to find cart
 * }
 * }</pre>
 *
 * <p><b>Usage in Use Cases:</b>
 * <pre>{@code
 * public class SomeUseCase {
 *     private final IdentityProvider identityProvider;
 *
 *     public void execute() {
 *         Identity identity = identityProvider.getCurrentIdentity();
 *         if (identity.isAnonymous()) {
 *             // handle anonymous user
 *         }
 *     }
 * }
 * }</pre>
 */
public interface IdentityProvider {

  /**
   * Retrieves the current user's identity from the security context.
   *
   * <p>This method always returns a valid identity because:
   * <ul>
   *   <li>The JWT filter creates an anonymous identity for first-time visitors</li>
   *   <li>Existing users have their identity extracted from the JWT</li>
   * </ul>
   *
   * @return the current user's identity (never null)
   * @throws IllegalStateException if called outside of a request context
   */
  @NonNull
  Identity getCurrentIdentity();
}
