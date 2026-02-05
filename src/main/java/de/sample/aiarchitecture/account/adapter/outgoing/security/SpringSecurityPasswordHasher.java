package de.sample.aiarchitecture.account.adapter.outgoing.security;

import de.sample.aiarchitecture.account.domain.service.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Spring Security implementation of the PasswordHasher domain service.
 *
 * <p>This adapter bridges the domain's PasswordHasher interface to Spring Security's
 * PasswordEncoder, keeping the domain layer framework-independent.
 *
 * <p><b>Implementation Details:</b>
 * <ul>
 *   <li>Uses the configured PasswordEncoder bean (typically BCrypt)</li>
 *   <li>BCrypt provides: random salt, configurable cost factor, timing-safe comparison</li>
 * </ul>
 *
 * <p><b>Configuration:</b>
 * The PasswordEncoder is configured in SecurityConfig with BCrypt (cost factor 12).
 *
 * @see PasswordHasher
 * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 */
@Component
public class SpringSecurityPasswordHasher implements PasswordHasher {

  private final PasswordEncoder passwordEncoder;

  public SpringSecurityPasswordHasher(final PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  
  public String hash(final String plaintext) {
    return passwordEncoder.encode(plaintext);
  }

  @Override
  public boolean matches(final String plaintext, final String hash) {
    return passwordEncoder.matches(plaintext, hash);
  }
}
