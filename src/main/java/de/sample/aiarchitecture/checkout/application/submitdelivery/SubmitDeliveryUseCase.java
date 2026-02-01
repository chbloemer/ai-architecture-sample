package de.sample.aiarchitecture.checkout.application.submitdelivery;

import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.DeliveryAddress;
import de.sample.aiarchitecture.checkout.domain.model.ShippingOption;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import java.util.Currency;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for submitting delivery information during checkout.
 *
 * <p>This use case handles the delivery step by:
 * <ul>
 *   <li>Loading and validating the checkout session</li>
 *   <li>Creating DeliveryAddress and ShippingOption value objects from command data</li>
 *   <li>Calling the domain method to submit delivery info</li>
 *   <li>Persisting the updated session</li>
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SubmitDeliveryInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class SubmitDeliveryUseCase implements SubmitDeliveryInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;

  public SubmitDeliveryUseCase(final CheckoutSessionRepository checkoutSessionRepository) {
    this.checkoutSessionRepository = checkoutSessionRepository;
  }

  @Override
  public @NonNull SubmitDeliveryResponse execute(@NonNull final SubmitDeliveryCommand command) {
    // Load session
    final CheckoutSessionId sessionId = CheckoutSessionId.of(command.sessionId());
    final CheckoutSession session =
        checkoutSessionRepository
            .findById(sessionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Session not found: " + command.sessionId()));

    // Create delivery address value object
    final DeliveryAddress address =
        DeliveryAddress.of(
            command.street(),
            command.streetLine2(),
            command.city(),
            command.postalCode(),
            command.country(),
            command.state());

    // Create shipping option value object
    final Money shippingCost =
        Money.of(command.shippingCost(), Currency.getInstance(command.currencyCode()));
    final ShippingOption shippingOption =
        ShippingOption.of(
            command.shippingOptionId(),
            command.shippingOptionName(),
            command.estimatedDelivery(),
            shippingCost);

    // Submit delivery (domain validates session state and step)
    session.submitDelivery(address, shippingOption);

    // Save session
    checkoutSessionRepository.save(session);

    // Map to response
    return mapToResponse(session);
  }

  private SubmitDeliveryResponse mapToResponse(final CheckoutSession session) {
    final DeliveryAddress address = session.deliveryAddress();
    final ShippingOption shipping = session.shippingOption();
    return new SubmitDeliveryResponse(
        session.id().value().toString(),
        session.currentStep().name(),
        session.status().name(),
        address.street(),
        address.streetLine2(),
        address.city(),
        address.postalCode(),
        address.country(),
        address.state(),
        shipping.id(),
        shipping.name(),
        shipping.estimatedDelivery(),
        shipping.cost().amount(),
        shipping.cost().currency().getCurrencyCode());
  }
}
