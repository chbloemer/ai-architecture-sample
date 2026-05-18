package de.sample.aiarchitecture.account.domain.gateway;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainGateway;

/**
 * Domain Gateway for password hashing operations.
 *
 * <p>This interface is owned by the domain and used by the {@code Account} aggregate (and the
 * {@code HashedPassword} value object) to delegate the technology-bound act of hashing a plaintext
 * password and verifying a candidate plaintext against a stored hash. The interface is
 * framework-free; the implementation lives in the outgoing adapter layer
 * ({@code SpringSecurityPasswordHasher} → BCrypt).
 *
 * <p><b>Classification:</b> This is a {@link DomainGateway}, not an Output Port. The aggregate
 * itself consults the gateway when it needs to apply a domain rule that requires hashed
 * credentials (registration, password change, login check), following the pattern Vaughn Vernon
 * uses in IDDD ({@code User} aggregate calling {@code EncryptionService} via {@code
 * DomainRegistry}). DCA replaces the service-locator lookup with a typed parameter passed by the
 * use case.
 *
 * <p><b>Security requirements for any implementation:</b>
 *
 * <ul>
 *   <li>Use a secure algorithm (BCrypt, Argon2, scrypt) with a per-hash random salt
 *   <li>{@link #matches(String, String)} must be timing-safe
 * </ul>
 */
public interface PasswordHasher extends DomainGateway {

  /**
   * Hashes a plaintext password with a per-hash random salt.
   *
   * @param plaintext the plaintext password to hash
   * @return the secure hash (salt embedded)
   */
  String hash(String plaintext);

  /**
   * Verifies a plaintext password against a previously stored hash in a timing-safe manner.
   *
   * @param plaintext the plaintext password to verify
   * @param hash the previously generated hash
   * @return true if the plaintext matches the hash
   */
  boolean matches(String plaintext, String hash);
}
