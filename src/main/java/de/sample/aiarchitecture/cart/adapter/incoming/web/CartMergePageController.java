package de.sample.aiarchitecture.cart.adapter.incoming.web;

import de.sample.aiarchitecture.cart.application.getcartmergeoptions.GetCartMergeOptionsInputPort;
import de.sample.aiarchitecture.cart.application.getcartmergeoptions.GetCartMergeOptionsQuery;
import de.sample.aiarchitecture.cart.application.getcartmergeoptions.GetCartMergeOptionsResponse;
import de.sample.aiarchitecture.cart.application.mergecarts.CartMergeStrategy;
import de.sample.aiarchitecture.cart.application.mergecarts.MergeCartsCommand;
import de.sample.aiarchitecture.cart.application.mergecarts.MergeCartsInputPort;
import de.sample.aiarchitecture.cart.application.mergecarts.MergeCartsResponse;
import de.sample.aiarchitecture.sharedkernel.application.common.security.Identity;
import de.sample.aiarchitecture.sharedkernel.application.port.security.IdentityProvider;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for cart merge options page.
 *
 * <p>This controller handles the cart merge decision flow when a user logs in
 * and both their anonymous cart and account cart have items.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/cart/merge-options.pug}
 */
@Controller
@RequestMapping("/cart/merge")
public class CartMergePageController {

  public static final String SESSION_ATTR_ANONYMOUS_USER_ID = "anonymousUserIdForMerge";
  public static final String SESSION_ATTR_RETURN_URL = "mergeReturnUrl";

  private final GetCartMergeOptionsInputPort getCartMergeOptionsUseCase;
  private final MergeCartsInputPort mergeCartsUseCase;
  private final IdentityProvider identityProvider;

  public CartMergePageController(
      final GetCartMergeOptionsInputPort getCartMergeOptionsUseCase,
      final MergeCartsInputPort mergeCartsUseCase,
      final IdentityProvider identityProvider) {
    this.getCartMergeOptionsUseCase = getCartMergeOptionsUseCase;
    this.mergeCartsUseCase = mergeCartsUseCase;
    this.identityProvider = identityProvider;
  }

  /**
   * Displays the cart merge options page.
   *
   * <p>Shows the user both carts and lets them choose how to handle the conflict.
   * Requires anonymous user ID to be stored in session (set by login controller).
   *
   * @param model Spring MVC model
   * @param session HTTP session containing anonymous user ID
   * @param returnUrl optional URL to redirect to after merge
   * @return view name "cart/merge-options"
   */
  @GetMapping
  public String showMergeOptions(
      final Model model,
      final HttpSession session,
      @RequestParam(required = false) final String returnUrl) {

    final Identity identity = identityProvider.getCurrentIdentity();
    final String registeredUserId = identity.userId().value();

    // Get anonymous user ID from session
    final String anonymousUserId = (String) session.getAttribute(SESSION_ATTR_ANONYMOUS_USER_ID);

    if (anonymousUserId == null) {
      // No anonymous user ID stored - shouldn't happen normally
      // Redirect to cart
      return "redirect:/cart";
    }

    // Store return URL in session for after merge
    if (returnUrl != null && !returnUrl.isBlank()) {
      session.setAttribute(SESSION_ATTR_RETURN_URL, returnUrl);
    }

    // Get merge options
    final GetCartMergeOptionsQuery query = new GetCartMergeOptionsQuery(anonymousUserId, registeredUserId);
    final GetCartMergeOptionsResponse options = getCartMergeOptionsUseCase.execute(query);

    if (!options.mergeRequired()) {
      // No merge needed - clean up session and redirect
      session.removeAttribute(SESSION_ATTR_ANONYMOUS_USER_ID);
      final String storedReturnUrl = (String) session.getAttribute(SESSION_ATTR_RETURN_URL);
      session.removeAttribute(SESSION_ATTR_RETURN_URL);

      if (storedReturnUrl != null && !storedReturnUrl.isBlank()) {
        return "redirect:" + storedReturnUrl;
      }
      return "redirect:/cart";
    }

    model.addAttribute("title", "Cart Merge Options");
    model.addAttribute("anonymousCart", options.anonymousCart());
    model.addAttribute("accountCart", options.accountCart());
    model.addAttribute("returnUrl", returnUrl);

    return "cart/merge-options";
  }

  /**
   * Handles the user's cart merge decision.
   *
   * @param strategy the chosen merge strategy
   * @param session HTTP session containing anonymous user ID
   * @param redirectAttributes for passing flash messages
   * @return redirect to return URL or cart
   */
  @PostMapping
  public String handleMergeDecision(
      @RequestParam final String strategy,
      final HttpSession session,
      final RedirectAttributes redirectAttributes) {

    final Identity identity = identityProvider.getCurrentIdentity();
    final String registeredUserId = identity.userId().value();

    // Get anonymous user ID from session
    final String anonymousUserId = (String) session.getAttribute(SESSION_ATTR_ANONYMOUS_USER_ID);

    if (anonymousUserId == null) {
      return "redirect:/cart";
    }

    // Parse strategy
    final CartMergeStrategy mergeStrategy;
    try {
      mergeStrategy = CartMergeStrategy.valueOf(strategy);
    } catch (final IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", "Invalid merge option selected");
      return "redirect:/cart/merge";
    }

    // Execute merge
    final MergeCartsCommand command = new MergeCartsCommand(anonymousUserId, registeredUserId, mergeStrategy);
    final MergeCartsResponse response = mergeCartsUseCase.execute(command);

    // Clean up session
    session.removeAttribute(SESSION_ATTR_ANONYMOUS_USER_ID);
    final String returnUrl = (String) session.getAttribute(SESSION_ATTR_RETURN_URL);
    session.removeAttribute(SESSION_ATTR_RETURN_URL);

    // Add success message based on strategy
    final String message = switch (mergeStrategy) {
      case MERGE_BOTH -> "Carts merged successfully!";
      case USE_ACCOUNT_CART -> "Using your account cart.";
      case USE_ANONYMOUS_CART -> "Using your recent cart.";
    };
    redirectAttributes.addFlashAttribute("message", message);

    // Redirect to return URL or cart
    if (returnUrl != null && !returnUrl.isBlank()) {
      return "redirect:" + returnUrl;
    }
    return "redirect:/cart";
  }
}
