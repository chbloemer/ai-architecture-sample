package de.sample.aiarchitecture.infrastructure.config;

import de.sample.aiarchitecture.infrastructure.security.jwt.JwtAuthenticationFilter;
import de.sample.aiarchitecture.infrastructure.security.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * <ul>
 *   <li>Stateless session management (no HTTP sessions)</li>
 *   <li>JWT filter for extracting/creating identity tokens</li>
 *   <li>BCrypt password encoding for account passwords</li>
 *   <li>CSRF disabled (using JWT cookies with SameSite=Lax)</li>
 * </ul>
 *
 * <p><b>Authentication Flow:</b>
 * <ol>
 *   <li>Request arrives at server</li>
 *   <li>JwtAuthenticationFilter extracts/creates JWT from cookie</li>
 *   <li>Identity is placed in SecurityContext</li>
 *   <li>Controllers access identity via IdentityProvider</li>
 * </ol>
 *
 * <p><b>URL Security:</b>
 * <ul>
 *   <li>Public: /, /products, /api/products, /h2-console, /actuator/health</li>
 *   <li>Authenticated: /cart, /checkout, /api/carts, /api/checkout-sessions</li>
 *   <li>Note: "Authenticated" means having a valid JWT (anonymous or registered)</li>
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
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http
        // Stateless session management - no HTTP sessions
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Disable CSRF - we use JWT in cookies with SameSite=Lax for protection
        .csrf(csrf -> csrf.disable())

        // Configure authorization rules
        .authorizeHttpRequests(authorize -> authorize
            // Public endpoints
            .requestMatchers("/").permitAll()
            .requestMatchers("/products/**").permitAll()
            .requestMatchers("/api/products/**").permitAll()
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/login", "/register").permitAll()

            // H2 console (development only)
            .requestMatchers("/h2-console/**").permitAll()

            // Actuator health endpoint
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()

            // MCP endpoints
            .requestMatchers("/mcp/**").permitAll()

            // Static resources
            .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico").permitAll()

            // All other requests require authentication (JWT - anonymous or registered)
            .anyRequest().authenticated())

        // Allow H2 console frames (development only)
        .headers(headers -> headers
            .frameOptions(frame -> frame.sameOrigin()))

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * BCrypt password encoder for secure password hashing.
   *
   * <p>BCrypt with strength 12 provides:
   * <ul>
   *   <li>Automatic salt generation</li>
   *   <li>2^12 = 4096 iterations</li>
   *   <li>Timing-safe comparison</li>
   *   <li>OWASP-recommended security level</li>
   * </ul>
   *
   * @return the password encoder bean
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}
