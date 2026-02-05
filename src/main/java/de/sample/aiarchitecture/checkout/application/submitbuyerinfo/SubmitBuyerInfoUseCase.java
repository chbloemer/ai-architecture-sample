package de.sample.aiarchitecture.checkout.application.submitbuyerinfo;

import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.domain.model.BuyerInfo;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for submitting buyer contact information during checkout.
 *
 * <p>This use case handles the buyer info step by:
 * <ul>
 *   <li>Loading and validating the checkout session</li>
 *   <li>Creating BuyerInfo value object from command data</li>
 *   <li>Calling the domain method to submit buyer info</li>
 *   <li>Persisting the updated session</li>
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SubmitBuyerInfoInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class SubmitBuyerInfoUseCase implements SubmitBuyerInfoInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;

  public SubmitBuyerInfoUseCase(final CheckoutSessionRepository checkoutSessionRepository) {
    this.checkoutSessionRepository = checkoutSessionRepository;
  }

  @Override
  public SubmitBuyerInfoResult execute(final SubmitBuyerInfoCommand command) {
    // Load session
    final CheckoutSessionId sessionId = CheckoutSessionId.of(command.sessionId());
    final CheckoutSession session =
        checkoutSessionRepository
            .findById(sessionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Session not found: " + command.sessionId()));

    // Create buyer info value object
    final BuyerInfo buyerInfo =
        BuyerInfo.of(command.email(), command.firstName(), command.lastName(), command.phone());

    // Submit buyer info (domain validates session state and step)
    session.submitBuyerInfo(buyerInfo);

    // Save session
    checkoutSessionRepository.save(session);

    // Map to response
    return mapToResponse(session);
  }

  private SubmitBuyerInfoResult mapToResponse(final CheckoutSession session) {
    final BuyerInfo buyerInfo = session.buyerInfo();
    return new SubmitBuyerInfoResult(
        session.id().value().toString(),
        session.currentStep().name(),
        session.status().name(),
        buyerInfo.email(),
        buyerInfo.firstName(),
        buyerInfo.lastName(),
        buyerInfo.phone());
  }
}
