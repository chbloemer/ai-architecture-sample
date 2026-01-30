package de.sample.aiarchitecture.checkout.adapter.incoming.web;

import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResponse;
import de.sample.aiarchitecture.checkout.application.getpaymentproviders.GetPaymentProvidersInputPort;
import de.sample.aiarchitecture.checkout.application.getpaymentproviders.GetPaymentProvidersQuery;
import de.sample.aiarchitecture.checkout.application.getpaymentproviders.GetPaymentProvidersResponse;
import de.sample.aiarchitecture.checkout.application.submitpayment.SubmitPaymentCommand;
import de.sample.aiarchitecture.checkout.application.submitpayment.SubmitPaymentInputPort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
  private final GetPaymentProvidersInputPort getPaymentProvidersInputPort;
  private final SubmitPaymentInputPort submitPaymentInputPort;

  public PaymentPageController(
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final GetPaymentProvidersInputPort getPaymentProvidersInputPort,
      final SubmitPaymentInputPort submitPaymentInputPort) {
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.getPaymentProvidersInputPort = getPaymentProvidersInputPort;
    this.submitPaymentInputPort = submitPaymentInputPort;
  }

  /**
   * Displays the payment method selection form.
   *
   * <p>This endpoint retrieves the checkout session and available payment providers,
   * then displays the payment form. If the session is not found, redirects to home with an error.
   *
   * @param id the checkout session ID
   * @param model the Spring MVC model
   * @param redirectAttributes for passing flash messages on error
   * @return the payment.pug template or redirect on error
   */
  @GetMapping("/{id}/payment")
  public String showPaymentForm(
      @PathVariable final String id,
      final Model model,
      final RedirectAttributes redirectAttributes) {

    final GetCheckoutSessionResponse session =
        getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(id));

    if (!session.found()) {
      redirectAttributes.addFlashAttribute("error", "Checkout session not found");
      return "redirect:/";
    }

    final GetPaymentProvidersResponse paymentProviders =
        getPaymentProvidersInputPort.execute(GetPaymentProvidersQuery.create());

    model.addAttribute("session", session);
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
   * @param id the checkout session ID
   * @param providerId the selected payment provider ID
   * @param providerReference optional provider-specific reference
   * @param redirectAttributes for passing flash messages
   * @return redirect to review step or back to payment on error
   */
  @PostMapping("/{id}/payment")
  public String submitPayment(
      @PathVariable final String id,
      @RequestParam final String providerId,
      @RequestParam(required = false) final String providerReference,
      final RedirectAttributes redirectAttributes) {

    try {
      submitPaymentInputPort.execute(
          new SubmitPaymentCommand(id, providerId, providerReference));

      return "redirect:/checkout/" + id + "/review";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/checkout/" + id + "/payment";
    }
  }
}
