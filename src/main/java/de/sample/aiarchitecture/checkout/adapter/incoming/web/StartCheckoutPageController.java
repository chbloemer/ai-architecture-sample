package de.sample.aiarchitecture.checkout.adapter.incoming.web;

import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartCommand;
import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartResponse;
import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartUseCase;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.checkout.application.startcheckout.StartCheckoutCommand;
import de.sample.aiarchitecture.checkout.application.startcheckout.StartCheckoutInputPort;
import de.sample.aiarchitecture.sharedkernel.application.port.security.Identity;
import de.sample.aiarchitecture.sharedkernel.application.port.security.IdentityProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for initiating checkout from a cart.
 *
 * <p>This controller handles the checkout initiation flow by:
 * <ul>
 *   <li>Getting the current user's identity from JWT</li>
 *   <li>Finding the active cart for the user</li>
 *   <li>Starting a new checkout session via the use case</li>
 *   <li>Redirecting to the buyer info step</li>
 * </ul>
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and
 * end with "Controller" suffix.
 */
@Controller
@RequestMapping("/checkout")
public class StartCheckoutPageController {

  private final StartCheckoutInputPort startCheckoutInputPort;
  private final GetOrCreateActiveCartUseCase getOrCreateActiveCartUseCase;
  private final IdentityProvider identityProvider;

  public StartCheckoutPageController(
      final StartCheckoutInputPort startCheckoutInputPort,
      final GetOrCreateActiveCartUseCase getOrCreateActiveCartUseCase,
      final IdentityProvider identityProvider) {
    this.startCheckoutInputPort = startCheckoutInputPort;
    this.getOrCreateActiveCartUseCase = getOrCreateActiveCartUseCase;
    this.identityProvider = identityProvider;
  }

  /**
   * Initiates checkout from the user's active cart.
   *
   * <p>This endpoint creates a new checkout session from the user's active cart
   * (identified via JWT identity) and redirects to the buyer info step.
   *
   * @param redirectAttributes for passing flash messages on error
   * @return redirect to buyer info step at /checkout/buyer
   */
  @GetMapping("/start")
  public String startCheckout(final RedirectAttributes redirectAttributes) {

    try {
      // Get customer ID from JWT identity
      final Identity identity = identityProvider.getCurrentIdentity();
      final CustomerId customerId = CustomerId.of(identity.userId().value());

      // Get or create active cart for the current user
      final GetOrCreateActiveCartResponse cartResponse =
          getOrCreateActiveCartUseCase.execute(
              new GetOrCreateActiveCartCommand(customerId.value()));

      // Start checkout from the user's cart
      startCheckoutInputPort.execute(new StartCheckoutCommand(cartResponse.cartId()));

      return "redirect:/checkout/buyer";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/cart";
    }
  }
}
