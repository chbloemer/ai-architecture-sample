package de.sample.aiarchitecture.account.adapter.incoming.web;

import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountCommand;
import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountInputPort;
import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountResponse;
import de.sample.aiarchitecture.infrastructure.security.jwt.JwtAuthenticationFilter;
import de.sample.aiarchitecture.infrastructure.security.jwt.JwtTokenService;
import de.sample.aiarchitecture.sharedkernel.domain.model.UserId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.IdentityProvider;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for login page.
 *
 * <p>This controller handles server-side rendered login pages using Pug templates.
 *
 * <p><b>Clean Architecture:</b> This controller only depends on its own bounded context
 * (account). After successful login, it redirects to the cart merge page with URL parameters,
 * allowing the cart context to decide if a merge is actually needed. This avoids cross-context
 * dependencies.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/account/login.pug}
 */
@Controller
@RequestMapping("/login")
public class LoginPageController {

  private final AuthenticateAccountInputPort authenticateAccountUseCase;
  private final JwtTokenService tokenService;
  private final IdentityProvider identityProvider;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public LoginPageController(
      final AuthenticateAccountInputPort authenticateAccountUseCase,
      final JwtTokenService tokenService,
      final IdentityProvider identityProvider,
      final JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.authenticateAccountUseCase = authenticateAccountUseCase;
    this.tokenService = tokenService;
    this.identityProvider = identityProvider;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  /**
   * Displays the login page.
   *
   * @param model Spring MVC model
   * @param returnUrl optional URL to redirect to after login
   * @return view name "account/login"
   */
  @GetMapping
  public String showLoginPage(
      final Model model,
      @RequestParam(required = false) final String returnUrl) {

    model.addAttribute("title", "Login");
    model.addAttribute("returnUrl", returnUrl);
    return "account/login";
  }

  /**
   * Handles login form submission.
   *
   * <p>After successful login, always redirects to the cart merge page with the anonymous
   * user ID as a URL parameter. The cart context will determine if a merge is actually
   * required and redirect appropriately if not.
   *
   * @param email the user's email
   * @param password the user's password
   * @param returnUrl optional URL to redirect to after login
   * @param response HTTP response for setting cookies
   * @param redirectAttributes for passing flash messages
   * @param model Spring MVC model
   * @return redirect to cart merge page on success, login page on failure
   */
  @PostMapping
  public String handleLogin(
      @RequestParam final String email,
      @RequestParam final String password,
      @RequestParam(required = false) final String returnUrl,
      final HttpServletResponse response,
      final RedirectAttributes redirectAttributes,
      final Model model) {

    // Capture anonymous user ID before authentication changes the identity
    final String anonymousUserId = identityProvider.getCurrentIdentity().userId().value();

    try {
      final AuthenticateAccountCommand command = new AuthenticateAccountCommand(email, password);
      final AuthenticateAccountResponse result = authenticateAccountUseCase.execute(command);

      if (!result.success()) {
        model.addAttribute("title", "Login");
        model.addAttribute("error", result.errorMessage());
        model.addAttribute("email", email);
        model.addAttribute("returnUrl", returnUrl);
        return "account/login";
      }

      final String registeredUserId = result.userId();

      final String token = tokenService.generateRegisteredToken(
          UserId.of(registeredUserId),
          result.email(),
          result.roles());

      jwtAuthenticationFilter.setRegisteredUserCookie(response, token);

      // Always redirect to merge page - let cart context decide if merge is needed
      // This avoids cross-context dependency (account → cart)
      String mergeUrl = "/cart/merge?anonymousUserId=" + anonymousUserId;
      if (returnUrl != null && !returnUrl.isBlank()) {
        mergeUrl += "&returnUrl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
      }
      return "redirect:" + mergeUrl;

    } catch (final IllegalArgumentException e) {
      model.addAttribute("title", "Login");
      model.addAttribute("error", e.getMessage());
      model.addAttribute("email", email);
      model.addAttribute("returnUrl", returnUrl);
      return "account/login";
    }
  }
}
