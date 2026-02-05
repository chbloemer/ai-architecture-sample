package de.sample.aiarchitecture.checkout.adapter.incoming.web;

import de.sample.aiarchitecture.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionResult;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResult;
import de.sample.aiarchitecture.checkout.application.getpaymentproviders.GetPaymentProvidersInputPort;
import de.sample.aiarchitecture.checkout.application.getpaymentproviders.GetPaymentProvidersQuery;
import de.sample.aiarchitecture.checkout.application.getpaymentproviders.GetPaymentProvidersResult;
import de.sample.aiarchitecture.checkout.application.submitpayment.SubmitPaymentCommand;
import de.sample.aiarchitecture.checkout.application.submitpayment.SubmitPaymentInputPort;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.IdentityProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for the payment method page in checkout.
 *
 * <p>This controller handles the payment step of checkout where the customer
 * selects their payment method.
 *
 * <p>The active checkout session is identified via JWT identity, removing the need
 * for session IDs in URLs.
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and
 * end with "Controller" suffix.
 */
@Controller
@RequestMapping("/checkout")
public class PaymentPageController {

  private final GetCheckoutSessionInputPort getCheckoutSessionInputPort;
  private final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort;
  private final GetPaymentProvidersInputPort getPaymentProvidersInputPort;
  private final SubmitPaymentInputPort submitPaymentInputPort;
  private final IdentityProvider identityProvider;

  public PaymentPageController(
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort,
      final GetPaymentProvidersInputPort getPaymentProvidersInputPort,
      final SubmitPaymentInputPort submitPaymentInputPort,
      final IdentityProvider identityProvider) {
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.getActiveCheckoutSessionInputPort = getActiveCheckoutSessionInputPort;
    this.getPaymentProvidersInputPort = getPaymentProvidersInputPort;
    this.submitPaymentInputPort = submitPaymentInputPort;
    this.identityProvider = identityProvider;
  }

  /**
   * Displays the payment method selection form.
   *
   * <p>This endpoint retrieves the active checkout session for the current user
   * (via JWT identity) and available payment providers, then displays the payment form.
   * If no active session is found, redirects to cart with an error.
   *
   * @param model the Spring MVC model
   * @param redirectAttributes for passing flash messages on error
   * @return the payment.pug template or redirect on error
   */
  @GetMapping("/payment")
  public String showPaymentForm(
      final Model model,
      final RedirectAttributes redirectAttributes) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final CustomerId customerId = CustomerId.of(identity.userId().value());

    // Find active checkout session for the user
    final GetActiveCheckoutSessionResult activeSession =
        getActiveCheckoutSessionInputPort.execute(
            GetActiveCheckoutSessionQuery.of(customerId.value()));

    if (!activeSession.found()) {
      redirectAttributes.addFlashAttribute("error", "No active checkout session found");
      return "redirect:/cart";
    }

    // Get full session details
    final GetCheckoutSessionResult result =
        getCheckoutSessionInputPort.execute(
            GetCheckoutSessionQuery.of(activeSession.sessionId()));

    if (!result.found()) {
      redirectAttributes.addFlashAttribute("error", "Checkout session not found");
      return "redirect:/cart";
    }

    // Convert to page-specific ViewModel
    final PaymentPageViewModel viewModel = PaymentPageViewModel.fromSnapshot(result.session());

    final GetPaymentProvidersResult paymentProviders =
        getPaymentProvidersInputPort.execute(GetPaymentProvidersQuery.create());

    model.addAttribute("paymentPage", viewModel);
    model.addAttribute("paymentProviders", paymentProviders.paymentProviders());
    model.addAttribute("title", "Payment - Checkout");

    return "checkout/payment";
  }

  /**
   * Submits payment method selection and advances to review step.
   *
   * <p>This endpoint processes the payment method selection and redirects
   * to the review step on success.
   *
   * @param providerId the selected payment provider ID
   * @param providerReference optional provider-specific reference
   * @param redirectAttributes for passing flash messages
   * @return redirect to review step or back to payment on error
   */
  @PostMapping("/payment")
  public String submitPayment(
      @RequestParam final String providerId,
      @RequestParam(required = false) final String providerReference,
      final RedirectAttributes redirectAttributes) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final CustomerId customerId = CustomerId.of(identity.userId().value());

    // Find active checkout session for the user
    final GetActiveCheckoutSessionResult activeSession =
        getActiveCheckoutSessionInputPort.execute(
            GetActiveCheckoutSessionQuery.of(customerId.value()));

    if (!activeSession.found()) {
      redirectAttributes.addFlashAttribute("error", "No active checkout session found");
      return "redirect:/cart";
    }

    try {
      submitPaymentInputPort.execute(
          new SubmitPaymentCommand(activeSession.sessionId(), providerId, providerReference));

      return "redirect:/checkout/review";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/checkout/payment";
    }
  }
}
