package de.sample.aiarchitecture.checkout.application.confirmcheckout;

import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.DomainEventPublisher;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for confirming a checkout session.
 *
 * <p>This use case handles the confirmation step by:
 * <ul>
 *   <li>Loading and validating the checkout session</li>
 *   <li>Calling the domain method to confirm the checkout</li>
 *   <li>Persisting the updated session</li>
 *   <li>The domain raises the CheckoutConfirmed integration event</li>
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link ConfirmCheckoutInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class ConfirmCheckoutUseCase implements ConfirmCheckoutInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final DomainEventPublisher domainEventPublisher;

  public ConfirmCheckoutUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final DomainEventPublisher domainEventPublisher) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.domainEventPublisher = domainEventPublisher;
  }

  @Override
  public @NonNull ConfirmCheckoutResult execute(@NonNull final ConfirmCheckoutCommand command) {
    // Load session
    final CheckoutSessionId sessionId = CheckoutSessionId.of(command.sessionId());
    final CheckoutSession session =
        checkoutSessionRepository
            .findById(sessionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Session not found: " + command.sessionId()));

    // Confirm checkout (domain validates session state, step, and completeness)
    // This raises the CheckoutConfirmed integration event
    session.confirm();

    // Save session
    checkoutSessionRepository.save(session);

    // Publish domain events (triggers CheckoutEventConsumer to complete the cart)
    domainEventPublisher.publishAndClearEvents(session);

    // Map to response
    return mapToResponse(session);
  }

  private ConfirmCheckoutResult mapToResponse(final CheckoutSession session) {
    return new ConfirmCheckoutResult(
        session.id().value().toString(),
        session.currentStep().name(),
        session.status().name(),
        session.cartId().value().toString(),
        session.customerId().value(),
        session.totals().total().amount().toPlainString(),
        session.totals().total().currency().getCurrencyCode(),
        session.orderReference());
  }
}
