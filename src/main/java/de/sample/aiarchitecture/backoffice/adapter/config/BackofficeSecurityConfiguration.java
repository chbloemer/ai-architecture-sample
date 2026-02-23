package de.sample.aiarchitecture.backoffice.adapter.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the backoffice module.
 *
 * <p>Provides a separate {@link SecurityFilterChain} scoped to {@code /backoffice/**} with
 * form-based login and session-based authentication. This chain runs at higher priority
 * ({@code @Order(1)}) than the storefront's JWT chain.
 *
 * <p><b>Design rationale:</b> The backoffice uses form login with sessions because:
 *
 * <ul>
 *   <li>Simpler than JWT for admin-only UI
 *   <li>Session-based auth is natural for server-rendered pages
 *   <li>Easy to migrate to OAuth2/Keycloak later by swapping this configuration
 * </ul>
 *
 * <p>The admin user is configured via {@link BackofficeSecurityProperties} with sensible dev-only
 * defaults. In production, override via environment variables.
 */
@Configuration
@EnableConfigurationProperties(BackofficeSecurityProperties.class)
public class BackofficeSecurityConfiguration {

  private final BackofficeSecurityProperties properties;

  public BackofficeSecurityConfiguration(final BackofficeSecurityProperties properties) {
    this.properties = properties;
  }

  /**
   * Security filter chain for backoffice URLs.
   *
   * <p>Intercepts only {@code /backoffice/**} requests. Uses form login with a custom login page
   * and session-based authentication.
   *
   * @param http the HTTP security builder
   * @return the configured filter chain
   * @throws Exception if configuration fails
   */
  @Bean
  @Order(1)
  public SecurityFilterChain backofficeSecurityFilterChain(final HttpSecurity http)
      throws Exception {
    http.securityMatcher("/backoffice/**")
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/backoffice/login")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/backoffice/login")
                    .loginProcessingUrl("/backoffice/login")
                    .defaultSuccessUrl("/backoffice/events", true)
                    .failureUrl("/backoffice/login?error=true"))
        .logout(
            logout ->
                logout
                    .logoutUrl("/backoffice/logout")
                    .logoutSuccessUrl("/backoffice/login?logout=true"));

    return http.build();
  }

  /**
   * In-memory user details service for backoffice admin authentication.
   *
   * <p>Creates a single admin user with the {@code BACKOFFICE_ADMIN} role. Credentials are
   * configured via {@code app.security.backoffice.*} properties.
   *
   * @param passwordEncoder the password encoder (shared with the account module)
   * @return the user details service
   */
  @Bean
  public UserDetailsService backofficeUserDetailsService(final PasswordEncoder passwordEncoder) {
    final var admin =
        User.withUsername(properties.username())
            .password(passwordEncoder.encode(properties.password()))
            .roles("BACKOFFICE_ADMIN")
            .build();

    return new InMemoryUserDetailsManager(admin);
  }
}
