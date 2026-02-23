package de.sample.aiarchitecture.account.infrastructure;

import de.sample.aiarchitecture.account.adapter.outgoing.security.JwtAuthenticationFilter;
import de.sample.aiarchitecture.account.adapter.outgoing.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the application.
 *
 * <p>This configuration sets up JWT-based authentication with the following features:
 *
 * <ul>
 *   <li>Stateless session management (no HTTP sessions)
 *   <li>JWT filter for extracting/creating identity tokens
 *   <li>BCrypt password encoding for account passwords
 *   <li>CSRF disabled (using JWT cookies with SameSite=Lax)
 * </ul>
 *
 * <p><b>Authentication Flow:</b>
 *
 * <ol>
 *   <li>Request arrives at server
 *   <li>JwtAuthenticationFilter extracts/creates JWT from cookie
 *   <li>Identity is placed in SecurityContext
 *   <li>Controllers access identity via IdentityProvider
 * </ol>
 *
 * <p><b>URL Security:</b>
 *
 * <ul>
 *   <li>Public: /, /products, /api/products, /h2-console, /actuator/health
 *   <li>Authenticated: /cart, /checkout, /api/carts, /api/checkout-sessions
 *   <li>Note: "Authenticated" means having a valid JWT (anonymous or registered)
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfiguration {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfiguration(final JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  @Order(2)
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http
        // Stateless session management - no HTTP sessions
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Disable CSRF - we use JWT in cookies with SameSite=Lax for protection
        .csrf(csrf -> csrf.disable())

        // Configure authorization rules
        .authorizeHttpRequests(
            authorize ->
                authorize
                    // Public endpoints
                    .requestMatchers("/")
                    .permitAll()
                    .requestMatchers("/products/**")
                    .permitAll()
                    .requestMatchers("/api/products/**")
                    .permitAll()
                    .requestMatchers("/auth/**")
                    .permitAll()
                    .requestMatchers("/login", "/register")
                    .permitAll()

                    // H2 console (development only)
                    .requestMatchers("/h2-console/**")
                    .permitAll()

                    // Actuator health endpoint
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()

                    // MCP endpoints
                    .requestMatchers("/mcp/**")
                    .permitAll()

                    // Static resources
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico")
                    .permitAll()

                    // All other requests require authentication (JWT - anonymous or registered)
                    .anyRequest()
                    .authenticated())

        // Allow H2 console frames (development only)
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * BCrypt password encoder for secure password hashing.
   *
   * <p>BCrypt with strength 12 provides:
   *
   * <ul>
   *   <li>Automatic salt generation
   *   <li>2^12 = 4096 iterations
   *   <li>Timing-safe comparison
   *   <li>OWASP-recommended security level
   * </ul>
   *
   * @return the password encoder bean
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  /**
   * Prevents the JWT filter from being auto-registered as a Servlet filter.
   *
   * <p>Without this, Spring Boot registers the {@code @Component}-annotated filter as both a
   * Security chain filter (via {@code addFilterBefore}) AND a standalone Servlet filter. The
   * standalone registration applies to ALL requests, including those handled by other security
   * chains (e.g., the backoffice form-login chain).
   *
   * @param filter the JWT filter bean
   * @return registration bean with auto-registration disabled
   */
  @Bean
  public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
      final JwtAuthenticationFilter filter) {
    final FilterRegistrationBean<JwtAuthenticationFilter> registration =
        new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }
}
