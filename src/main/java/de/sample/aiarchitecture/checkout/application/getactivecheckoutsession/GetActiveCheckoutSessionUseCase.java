package de.sample.aiarchitecture.checkout.application.getactivecheckoutsession;

import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for getting an active checkout session for a customer.
 *
 * <p>This use case retrieves the active checkout session for a customer
 * based on their customer ID (derived from JWT identity).
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetActiveCheckoutSessionInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetActiveCheckoutSessionUseCase implements GetActiveCheckoutSessionInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;

  public GetActiveCheckoutSessionUseCase(final CheckoutSessionRepository checkoutSessionRepository) {
    this.checkoutSessionRepository = checkoutSessionRepository;
  }

  @Override
  public @NonNull GetActiveCheckoutSessionResponse execute(
      @NonNull final GetActiveCheckoutSessionQuery query) {
    final CustomerId customerId = CustomerId.of(query.customerId());

    return checkoutSessionRepository
        .findActiveByCustomerId(customerId)
        .map(session -> GetActiveCheckoutSessionResponse.of(
            session.id().value(),
            session.customerId().value()))
        .orElseGet(GetActiveCheckoutSessionResponse::notFound);
  }
}
