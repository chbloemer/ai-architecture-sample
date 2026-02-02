package de.sample.aiarchitecture.checkout.application.submitpayment;

import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.application.shared.PaymentProvider;
import de.sample.aiarchitecture.checkout.application.shared.PaymentProviderRegistry;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.PaymentProviderId;
import de.sample.aiarchitecture.checkout.domain.model.PaymentSelection;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for submitting payment information during checkout.
 *
 * <p>This use case handles the payment step by:
 * <ul>
 *   <li>Loading and validating the checkout session</li>
 *   <li>Validating the payment provider exists and is available</li>
 *   <li>Creating PaymentSelection value object from command data</li>
 *   <li>Calling the domain method to submit payment info</li>
 *   <li>Persisting the updated session</li>
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SubmitPaymentInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class SubmitPaymentUseCase implements SubmitPaymentInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final PaymentProviderRegistry paymentProviderRegistry;

  public SubmitPaymentUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final PaymentProviderRegistry paymentProviderRegistry) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.paymentProviderRegistry = paymentProviderRegistry;
  }

  @Override
  public @NonNull SubmitPaymentResult execute(@NonNull final SubmitPaymentCommand command) {
    // Load session
    final CheckoutSessionId sessionId = CheckoutSessionId.of(command.sessionId());
    final CheckoutSession session =
        checkoutSessionRepository
            .findById(sessionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Session not found: " + command.sessionId()));

    // Validate payment provider exists
    final PaymentProviderId providerId = PaymentProviderId.of(command.providerId());
    final PaymentProvider provider =
        paymentProviderRegistry
            .findById(providerId)
            .orElseThrow(
                () -> new IllegalArgumentException("Payment provider not found: " + command.providerId()));

    // Create payment selection value object
    final PaymentSelection paymentSelection =
        PaymentSelection.of(providerId, command.providerReference());

    // Submit payment (domain validates session state and step)
    session.submitPayment(paymentSelection);

    // Save session
    checkoutSessionRepository.save(session);

    // Map to response
    return mapToResponse(session, provider);
  }

  private SubmitPaymentResult mapToResponse(
      final CheckoutSession session, final PaymentProvider provider) {
    final PaymentSelection payment = session.paymentSelection();
    return new SubmitPaymentResult(
        session.id().value().toString(),
        session.currentStep().name(),
        session.status().name(),
        payment.providerId().value(),
        provider.displayName(),
        payment.providerReference());
  }
}
