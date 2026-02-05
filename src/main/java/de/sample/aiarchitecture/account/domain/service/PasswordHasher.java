package de.sample.aiarchitecture.account.domain.service;

/**
 * Domain service interface for password hashing operations.
 *
 * <p>This interface abstracts password hashing, allowing domain objects
 * to work with passwords without knowing the specific hashing algorithm.
 * Implementations reside in the adapter layer.
 *
 * <p><b>Security Requirements:</b>
 * <ul>
 *   <li>Use BCrypt or another secure hashing algorithm</li>
 *   <li>Include random salt with each hash</li>
 *   <li>Use timing-safe comparison to prevent timing attacks</li>
 * </ul>
 *
 * @see de.sample.aiarchitecture.account.adapter.outgoing.security.SpringSecurityPasswordHasher
 */
public interface PasswordHasher {

  /**
   * Hashes a plaintext password using a secure hashing algorithm.
   *
   * <p>The returned hash includes a random salt, so hashing the same
   * password twice will produce different results.
   *
   * @param plaintext the plaintext password to hash
   * @return the secure hash (including salt)
   */
  
  String hash(String plaintext);

  /**
   * Checks if a plaintext password matches a previously hashed password.
   *
   * <p>This comparison must be timing-safe to prevent timing attacks.
   *
   * @param plaintext the plaintext password to check
   * @param hash the previously generated hash to compare against
   * @return true if the password matches the hash
   */
  boolean matches(String plaintext, String hash);
}
