package de.sample.aiarchitecture.account.adapter.outgoing.security;

import de.sample.aiarchitecture.account.domain.gateway.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Spring Security implementation of the {@link PasswordHasher} domain gateway.
 *
 * <p>This adapter bridges the domain gateway to Spring Security's {@link PasswordEncoder}, keeping
 * the domain layer framework-independent while routing the actual cryptographic work to BCrypt.
 *
 * <p><b>Implementation Details:</b>
 *
 * <ul>
 *   <li>Uses the configured {@link PasswordEncoder} bean (typically BCrypt)
 *   <li>BCrypt provides: random salt, configurable cost factor, timing-safe comparison
 * </ul>
 *
 * <p><b>Configuration:</b> The {@link PasswordEncoder} is configured in {@code SecurityConfig} with
 * BCrypt (cost factor 12).
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
