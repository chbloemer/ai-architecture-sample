package de.sample.aiarchitecture.account.adapter.incoming.web;

import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountCommand;
import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountInputPort;
import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountResponse;
import de.sample.aiarchitecture.infrastructure.security.jwt.JwtAuthenticationFilter;
import de.sample.aiarchitecture.sharedkernel.application.port.security.TokenService;
import de.sample.aiarchitecture.sharedkernel.domain.common.UserId;
import jakarta.servlet.http.HttpServletResponse;
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
 * <p><b>Template Location:</b> {@code src/main/resources/templates/account/login.pug}
 */
@Controller
@RequestMapping("/login")
public class LoginPageController {

  private final AuthenticateAccountInputPort authenticateAccountUseCase;
  private final TokenService tokenService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public LoginPageController(
      final AuthenticateAccountInputPort authenticateAccountUseCase,
      final TokenService tokenService,
      final JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.authenticateAccountUseCase = authenticateAccountUseCase;
    this.tokenService = tokenService;
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
   * @param email the user's email
   * @param password the user's password
   * @param returnUrl optional URL to redirect to after login
   * @param response HTTP response for setting cookies
   * @param redirectAttributes for passing flash messages
   * @param model Spring MVC model
   * @return redirect to home or returnUrl on success, login page on failure
   */
  @PostMapping
  public String handleLogin(
      @RequestParam final String email,
      @RequestParam final String password,
      @RequestParam(required = false) final String returnUrl,
      final HttpServletResponse response,
      final RedirectAttributes redirectAttributes,
      final Model model) {

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

      final String token = tokenService.generateRegisteredToken(
          UserId.of(result.userId()),
          result.email(),
          result.roles());

      jwtAuthenticationFilter.setRegisteredUserCookie(response, token);

      redirectAttributes.addFlashAttribute("message", "Welcome back!");

      if (returnUrl != null && !returnUrl.isBlank()) {
        return "redirect:" + returnUrl;
      }
      return "redirect:/";

    } catch (final IllegalArgumentException e) {
      model.addAttribute("title", "Login");
      model.addAttribute("error", e.getMessage());
      model.addAttribute("email", email);
      model.addAttribute("returnUrl", returnUrl);
      return "account/login";
    }
  }
}
