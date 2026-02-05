package de.sample.aiarchitecture.checkout.application.getcheckoutsession;

import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResult.AddressData;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResult.BuyerInfoData;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResult.DeliveryData;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResult.LineItemData;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResult.PaymentData;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResult.ShippingOptionData;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResult.TotalsData;
import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.domain.model.BuyerInfo;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItem;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutTotals;
import de.sample.aiarchitecture.checkout.domain.model.DeliveryAddress;
import de.sample.aiarchitecture.checkout.domain.model.PaymentSelection;
import de.sample.aiarchitecture.checkout.domain.model.ShippingOption;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a checkout session by ID.
 *
 * <p>This use case loads all session data for display, including line items,
 * totals, buyer info, delivery, and payment information.
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
        .map(this::mapToResponse)
        .orElseGet(GetCheckoutSessionResult::notFound);
  }

  private GetCheckoutSessionResult mapToResponse(final CheckoutSession session) {
    return new GetCheckoutSessionResult(
        true,
        session.id().value(),
        session.cartId().value(),
        session.customerId().value(),
        session.currentStep().name(),
        session.status().name(),
        mapLineItems(session.lineItems()),
        mapTotals(session.totals()),
        mapBuyerInfo(session.buyerInfo()),
        mapDelivery(session.deliveryAddress(), session.shippingOption()),
        mapPayment(session.paymentSelection()),
        session.orderReference());
  }

  private List<LineItemData> mapLineItems(final List<CheckoutLineItem> lineItems) {
    return lineItems.stream().map(this::mapLineItem).toList();
  }

  private LineItemData mapLineItem(final CheckoutLineItem item) {
    return new LineItemData(
        item.id().value(),
        item.productId().value(),
        item.productName(),
        item.unitPrice().amount(),
        item.unitPrice().currency().getCurrencyCode(),
        item.quantity(),
        item.lineTotal().amount());
  }

  private TotalsData mapTotals(final CheckoutTotals totals) {
    return new TotalsData(
        totals.subtotal().amount(),
        totals.shipping().amount(),
        totals.tax().amount(),
        totals.total().amount(),
        totals.total().currency().getCurrencyCode());
  }

  private BuyerInfoData mapBuyerInfo(final BuyerInfo buyerInfo) {
    if (buyerInfo == null) {
      return null;
    }
    return new BuyerInfoData(
        buyerInfo.email(), buyerInfo.firstName(), buyerInfo.lastName(), buyerInfo.phone());
  }

  private DeliveryData mapDelivery(
      final DeliveryAddress address, final ShippingOption shippingOption) {
    if (address == null || shippingOption == null) {
      return null;
    }
    return new DeliveryData(mapAddress(address), mapShippingOption(shippingOption));
  }

  private AddressData mapAddress(final DeliveryAddress address) {
    return new AddressData(
        address.street(),
        address.streetLine2(),
        address.city(),
        address.postalCode(),
        address.country(),
        address.state());
  }

  private ShippingOptionData mapShippingOption(final ShippingOption option) {
    return new ShippingOptionData(
        option.id(),
        option.name(),
        option.estimatedDelivery(),
        option.cost().amount(),
        option.cost().currency().getCurrencyCode());
  }

  private PaymentData mapPayment(final PaymentSelection payment) {
    if (payment == null) {
      return null;
    }
    return new PaymentData(payment.providerId().value(), payment.providerReference());
  }
}
