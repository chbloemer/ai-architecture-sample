package de.sample.aiarchitecture.checkout.application.getcheckoutsession;

import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.readmodel.CheckoutCartBuilder;
import de.sample.aiarchitecture.checkout.domain.readmodel.CheckoutCartSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a checkout session by ID.
 *
 * <p>This use case loads all session data for display, including line items,
 * totals, buyer info, delivery, and payment information.
 *
 * <p><b>Interest Interface Pattern:</b> This use case uses {@link CheckoutCartBuilder}
 * to receive state from the aggregate via the Interest Interface Pattern, avoiding
 * direct getter calls on the aggregate.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetCheckoutSessionInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetCheckoutSessionUseCase implements GetCheckoutSessionInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;

  public GetCheckoutSessionUseCase(final CheckoutSessionRepository checkoutSessionRepository) {
    this.checkoutSessionRepository = checkoutSessionRepository;
  }

  @Override
  public GetCheckoutSessionResult execute(final GetCheckoutSessionQuery query) {
    return checkoutSessionRepository
        .findById(query.sessionId())
        .map(this::buildSnapshot)
        .map(GetCheckoutSessionResult::found)
        .orElseGet(GetCheckoutSessionResult::notFound);
  }

  private CheckoutCartSnapshot buildSnapshot(final CheckoutSession session) {
    final CheckoutCartBuilder builder = new CheckoutCartBuilder();
    session.provideStateTo(builder);
    return builder.build();
  }
}
