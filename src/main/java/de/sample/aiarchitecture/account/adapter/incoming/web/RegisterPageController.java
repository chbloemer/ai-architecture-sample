package de.sample.aiarchitecture.account.adapter.incoming.web;

import de.sample.aiarchitecture.account.application.registeraccount.RegisterAccountCommand;
import de.sample.aiarchitecture.account.application.registeraccount.RegisterAccountInputPort;
import de.sample.aiarchitecture.account.application.registeraccount.RegisterAccountResponse;
import de.sample.aiarchitecture.sharedkernel.domain.model.UserId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.IdentityProvider;
import de.sample.aiarchitecture.account.application.shared.IdentitySession;
import de.sample.aiarchitecture.account.application.shared.TokenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for registration page.
 *
 * <p>This controller handles server-side rendered registration pages using Pug templates.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/account/register.pug}
 */
@Controller
@RequestMapping("/register")
public class RegisterPageController {

  private final RegisterAccountInputPort registerAccountUseCase;
  private final TokenService tokenService;
  private final IdentityProvider identityProvider;
  private final IdentitySession identitySession;

  public RegisterPageController(
      final RegisterAccountInputPort registerAccountUseCase,
      final TokenService tokenService,
      final IdentityProvider identityProvider,
      final IdentitySession identitySession) {
    this.registerAccountUseCase = registerAccountUseCase;
    this.tokenService = tokenService;
    this.identityProvider = identityProvider;
    this.identitySession = identitySession;
  }

  /**
   * Displays the registration page.
   *
   * @param model Spring MVC model
   * @param returnUrl optional URL to redirect to after registration
   * @return view name "account/register"
   */
  @GetMapping
  public String showRegisterPage(
      final Model model,
      @RequestParam(required = false) final String returnUrl) {

    model.addAttribute("title", "Register");
    model.addAttribute("returnUrl", returnUrl);
    return "account/register";
  }

  /**
   * Handles registration form submission.
   *
   * @param email the user's email
   * @param password the user's password
   * @param confirmPassword password confirmation
   * @param returnUrl optional URL to redirect to after registration
   * @param redirectAttributes for passing flash messages
   * @param model Spring MVC model
   * @return redirect to home or returnUrl on success, register page on failure
   */
  @PostMapping
  public String handleRegistration(
      @RequestParam final String email,
      @RequestParam final String password,
      @RequestParam final String confirmPassword,
      @RequestParam(required = false) final String returnUrl,
      final RedirectAttributes redirectAttributes,
      final Model model) {

    if (!password.equals(confirmPassword)) {
      model.addAttribute("title", "Register");
      model.addAttribute("error", "Passwords do not match");
      model.addAttribute("email", email);
      model.addAttribute("returnUrl", returnUrl);
      return "account/register";
    }

    if (password.length() < 8) {
      model.addAttribute("title", "Register");
      model.addAttribute("error", "Password must be at least 8 characters");
      model.addAttribute("email", email);
      model.addAttribute("returnUrl", returnUrl);
      return "account/register";
    }

    try {
      final String currentUserId = identityProvider.getCurrentIdentity().userId().value();

      final RegisterAccountCommand command = new RegisterAccountCommand(
          email,
          password,
          currentUserId);

      final RegisterAccountResponse result = registerAccountUseCase.execute(command);

      final String token = tokenService.generateRegisteredToken(
          UserId.of(result.userId()),
          result.email(),
          result.roles());

      identitySession.setRegisteredIdentity(token);

      redirectAttributes.addFlashAttribute("message", "Account created successfully! Welcome!");

      if (returnUrl != null && !returnUrl.isBlank()) {
        return "redirect:" + returnUrl;
      }
      return "redirect:/";

    } catch (final IllegalArgumentException e) {
      model.addAttribute("title", "Register");
      model.addAttribute("error", e.getMessage());
      model.addAttribute("email", email);
      model.addAttribute("returnUrl", returnUrl);
      return "account/register";
    }
  }
}
