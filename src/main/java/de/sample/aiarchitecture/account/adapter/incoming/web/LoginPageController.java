package de.sample.aiarchitecture.account.adapter.incoming.web;

import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountCommand;
import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountInputPort;
import de.sample.aiarchitecture.account.application.authenticateaccount.AuthenticateAccountResponse;
import de.sample.aiarchitecture.cart.adapter.incoming.web.CartMergePageController;
import de.sample.aiarchitecture.cart.application.getcartmergeoptions.GetCartMergeOptionsInputPort;
import de.sample.aiarchitecture.cart.application.getcartmergeoptions.GetCartMergeOptionsQuery;
import de.sample.aiarchitecture.cart.application.getcartmergeoptions.GetCartMergeOptionsResponse;
import de.sample.aiarchitecture.sharedkernel.application.port.security.IdentityCookieService;
import de.sample.aiarchitecture.sharedkernel.application.port.security.IdentityProvider;
import de.sample.aiarchitecture.sharedkernel.application.port.security.TokenService;
import de.sample.aiarchitecture.sharedkernel.domain.common.UserId;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
  private final GetCartMergeOptionsInputPort getCartMergeOptionsUseCase;
  private final TokenService tokenService;
  private final IdentityProvider identityProvider;
  private final IdentityCookieService identityCookieService;

  public LoginPageController(
      final AuthenticateAccountInputPort authenticateAccountUseCase,
      final GetCartMergeOptionsInputPort getCartMergeOptionsUseCase,
      final TokenService tokenService,
      final IdentityProvider identityProvider,
      final IdentityCookieService identityCookieService) {
    this.authenticateAccountUseCase = authenticateAccountUseCase;
    this.getCartMergeOptionsUseCase = getCartMergeOptionsUseCase;
    this.tokenService = tokenService;
    this.identityProvider = identityProvider;
    this.identityCookieService = identityCookieService;
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
   * <p>After successful login, checks if the user has items in both their anonymous
   * cart and account cart. If so, redirects to the cart merge options page.
   *
   * @param email the user's email
   * @param password the user's password
   * @param returnUrl optional URL to redirect to after login
   * @param response HTTP response for setting cookies
   * @param session HTTP session for storing anonymous user ID for merge
   * @param redirectAttributes for passing flash messages
   * @param model Spring MVC model
   * @return redirect to home, returnUrl, or cart merge page on success, login page on failure
   */
  @PostMapping
  public String handleLogin(
      @RequestParam final String email,
      @RequestParam final String password,
      @RequestParam(required = false) final String returnUrl,
      final HttpServletResponse response,
      final HttpSession session,
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

      identityCookieService.setRegisteredUserCookie(response, token);

      // Check if cart merge is required
      final GetCartMergeOptionsQuery mergeQuery = new GetCartMergeOptionsQuery(anonymousUserId, registeredUserId);
      final GetCartMergeOptionsResponse mergeOptions = getCartMergeOptionsUseCase.execute(mergeQuery);

      if (mergeOptions.mergeRequired()) {
        // Store anonymous user ID in session for the merge page
        session.setAttribute(CartMergePageController.SESSION_ATTR_ANONYMOUS_USER_ID, anonymousUserId);
        if (returnUrl != null && !returnUrl.isBlank()) {
          session.setAttribute(CartMergePageController.SESSION_ATTR_RETURN_URL, returnUrl);
        }
        return "redirect:/cart/merge";
      }

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
