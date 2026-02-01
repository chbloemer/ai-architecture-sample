package de.sample.aiarchitecture.account.adapter.incoming.api;

import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountCommand;
import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountInputPort;
import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountResponse;
import de.sample.aiarchitecture.account.application.registeraccount.RegisterAccountCommand;
import de.sample.aiarchitecture.account.application.registeraccount.RegisterAccountInputPort;
import de.sample.aiarchitecture.account.application.registeraccount.RegisterAccountResponse;
import de.sample.aiarchitecture.infrastructure.security.jwt.JwtAuthenticationFilter;
import de.sample.aiarchitecture.infrastructure.security.jwt.JwtTokenService;
import de.sample.aiarchitecture.sharedkernel.domain.model.UserId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.IdentityProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API Resource for Authentication operations.
 *
 * <p>This is a primary adapter (incoming) in Hexagonal Architecture that exposes
 * authentication functionality via REST API.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/auth/login - Authenticate user and return JWT token</li>
 *   <li>POST /api/auth/register - Register new user and return JWT token</li>
 *   <li>POST /api/auth/logout - Clear authentication cookie</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthResource {

  private final AuthenticateAccountInputPort authenticateAccountUseCase;
  private final RegisterAccountInputPort registerAccountUseCase;
  private final JwtTokenService tokenService;
  private final IdentityProvider identityProvider;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public AuthResource(
      final AuthenticateAccountInputPort authenticateAccountUseCase,
      final RegisterAccountInputPort registerAccountUseCase,
      final JwtTokenService tokenService,
      final IdentityProvider identityProvider,
      final JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.authenticateAccountUseCase = authenticateAccountUseCase;
    this.registerAccountUseCase = registerAccountUseCase;
    this.tokenService = tokenService;
    this.identityProvider = identityProvider;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginApiResult> login(
      @Valid @RequestBody final LoginRequest request,
      final HttpServletResponse response) {

    final AuthenticateAccountCommand command = new AuthenticateAccountCommand(
        request.email(),
        request.password());

    final AuthenticateAccountResponse result = authenticateAccountUseCase.execute(command);

    if (!result.success()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(LoginApiResult.failure(result.errorMessage()));
    }

    final String token = tokenService.generateRegisteredToken(
        UserId.of(result.userId()),
        result.email(),
        result.roles());

    jwtAuthenticationFilter.setRegisteredUserCookie(response, token);

    return ResponseEntity.ok(LoginApiResult.success(token, result.email()));
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterApiResult> register(
      @Valid @RequestBody final RegisterRequest request,
      final HttpServletResponse response) {

    final String currentUserId = identityProvider.getCurrentIdentity().userId().value();

    final RegisterAccountCommand command = new RegisterAccountCommand(
        request.email(),
        request.password(),
        currentUserId);

    try {
      final RegisterAccountResponse result = registerAccountUseCase.execute(command);

      final String token = tokenService.generateRegisteredToken(
          UserId.of(result.userId()),
          result.email(),
          result.roles());

      jwtAuthenticationFilter.setRegisteredUserCookie(response, token);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(RegisterApiResult.success(token, result.email()));

    } catch (final IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(RegisterApiResult.failure(e.getMessage()));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(final HttpServletResponse response) {
    jwtAuthenticationFilter.clearIdentityCookie(response);
    return ResponseEntity.ok().build();
  }
}
