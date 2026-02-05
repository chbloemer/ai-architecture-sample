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
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutTotals;
import de.sample.aiarchitecture.checkout.domain.model.DeliveryAddress;
import de.sample.aiarchitecture.checkout.domain.model.PaymentSelection;
import de.sample.aiarchitecture.checkout.domain.model.ShippingOption;
import de.sample.aiarchitecture.checkout.domain.readmodel.CheckoutCartBuilder;
import de.sample.aiarchitecture.checkout.domain.readmodel.CheckoutCartSnapshot;
import de.sample.aiarchitecture.checkout.domain.readmodel.LineItemSnapshot;
import java.util.List;
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
        .map(this::mapToResponse)
        .orElseGet(GetCheckoutSessionResult::notFound);
  }

  private GetCheckoutSessionResult mapToResponse(final CheckoutSession session) {
    // Use the builder pattern with Interest Interface
    final CheckoutCartBuilder builder = new CheckoutCartBuilder();
    session.provideStateTo(builder);
    final CheckoutCartSnapshot snapshot = builder.build();

    // Get additional data from builder that's not in the snapshot
    final CheckoutTotals totals = builder.getTotals();

    return new GetCheckoutSessionResult(
        true,
        snapshot.sessionId().value(),
        snapshot.cartId().value(),
        snapshot.customerId().value(),
        snapshot.step().name(),
        builder.getStatus() != null ? builder.getStatus().name() : null,
        mapLineItems(snapshot.lineItems()),
        mapTotals(totals),
        mapBuyerInfo(snapshot.buyerInfo()),
        mapDelivery(snapshot.deliveryAddress(), snapshot.shippingOption()),
        mapPayment(snapshot.paymentSelection()),
        builder.getOrderReference());
  }

  private List<LineItemData> mapLineItems(final List<LineItemSnapshot> lineItems) {
    return lineItems.stream().map(this::mapLineItem).toList();
  }

  private LineItemData mapLineItem(final LineItemSnapshot item) {
    return new LineItemData(
        item.lineItemId().value(),
        item.productId().value(),
        item.name(),
        item.price().amount(),
        item.price().currency().getCurrencyCode(),
        item.quantity(),
        item.lineTotal().amount());
  }

  private TotalsData mapTotals(final CheckoutTotals totals) {
    if (totals == null) {
      return null;
    }
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
