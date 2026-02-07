package de.sample.aiarchitecture.checkout.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import de.sample.aiarchitecture.checkout.domain.model.BuyerInfo;
import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItem;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutLineItemId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutStep;
import de.sample.aiarchitecture.checkout.domain.model.DeliveryAddress;
import de.sample.aiarchitecture.checkout.domain.model.PaymentProviderId;
import de.sample.aiarchitecture.checkout.domain.model.PaymentSelection;
import de.sample.aiarchitecture.checkout.domain.model.ShippingOption;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CheckoutStepValidatorTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private CheckoutStepValidator validator;

  @BeforeEach
  void setUp() {
    validator = new CheckoutStepValidator();
  }

  @Nested
  @DisplayName("Invalid Session Tests")
  class InvalidSessionTests {

    @Test
    @DisplayName("null session redirects to cart")
    void nullSessionRedirectsToCart() {
      Optional<String> redirect = validator.validateStepAccess(null, CheckoutStep.BUYER_INFO);

      assertTrue(redirect.isPresent());
      assertEquals("/cart", redirect.get());
    }

    @Test
    @DisplayName("null session redirects to cart for any step")
    void nullSessionRedirectsToCartForAnyStep() {
      for (CheckoutStep step : CheckoutStep.values()) {
        Optional<String> redirect = validator.validateStepAccess(null, step);
        assertTrue(redirect.isPresent(), "Should redirect for step: " + step);
        assertEquals("/cart", redirect.get());
      }
    }
  }

  @Nested
  @DisplayName("Skip Ahead Prevention Tests")
  class SkipAheadTests {

    @Test
    @DisplayName("cannot skip to DELIVERY from BUYER_INFO")
    void cannotSkipToDeliveryFromBuyerInfo() {
      CheckoutSession session = createActiveSession();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.DELIVERY);

      assertTrue(redirect.isPresent());
      assertEquals("/checkout/buyer-info", redirect.get());
    }

    @Test
    @DisplayName("cannot skip to PAYMENT from BUYER_INFO")
    void cannotSkipToPaymentFromBuyerInfo() {
      CheckoutSession session = createActiveSession();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.PAYMENT);

      assertTrue(redirect.isPresent());
      assertEquals("/checkout/buyer-info", redirect.get());
    }

    @Test
    @DisplayName("cannot skip to REVIEW from BUYER_INFO")
    void cannotSkipToReviewFromBuyerInfo() {
      CheckoutSession session = createActiveSession();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.REVIEW);

      assertTrue(redirect.isPresent());
      assertEquals("/checkout/buyer-info", redirect.get());
    }

    @Test
    @DisplayName("cannot skip to CONFIRMATION from BUYER_INFO")
    void cannotSkipToConfirmationFromBuyerInfo() {
      CheckoutSession session = createActiveSession();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.CONFIRMATION);

      assertTrue(redirect.isPresent());
      assertEquals("/checkout/buyer-info", redirect.get());
    }

    @Test
    @DisplayName("cannot skip to PAYMENT from DELIVERY")
    void cannotSkipToPaymentFromDelivery() {
      CheckoutSession session = createSessionAtDelivery();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.PAYMENT);

      assertTrue(redirect.isPresent());
      assertEquals("/checkout/delivery", redirect.get());
    }

    @Test
    @DisplayName("cannot skip to REVIEW from PAYMENT")
    void cannotSkipToReviewFromPayment() {
      CheckoutSession session = createSessionAtPayment();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.REVIEW);

      assertTrue(redirect.isPresent());
      assertEquals("/checkout/payment", redirect.get());
    }
  }

  @Nested
  @DisplayName("Go Back Tests")
  class GoBackTests {

    @Test
    @DisplayName("can go back to BUYER_INFO from DELIVERY")
    void canGoBackToBuyerInfoFromDelivery() {
      CheckoutSession session = createSessionAtDelivery();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.BUYER_INFO);

      assertTrue(redirect.isEmpty(), "Should allow going back to BUYER_INFO");
    }

    @Test
    @DisplayName("can go back to BUYER_INFO from PAYMENT")
    void canGoBackToBuyerInfoFromPayment() {
      CheckoutSession session = createSessionAtPayment();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.BUYER_INFO);

      assertTrue(redirect.isEmpty(), "Should allow going back to BUYER_INFO");
    }

    @Test
    @DisplayName("can go back to DELIVERY from PAYMENT")
    void canGoBackToDeliveryFromPayment() {
      CheckoutSession session = createSessionAtPayment();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.DELIVERY);

      assertTrue(redirect.isEmpty(), "Should allow going back to DELIVERY");
    }

    @Test
    @DisplayName("can go back to BUYER_INFO from REVIEW")
    void canGoBackToBuyerInfoFromReview() {
      CheckoutSession session = createSessionAtReview();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.BUYER_INFO);

      assertTrue(redirect.isEmpty(), "Should allow going back to BUYER_INFO");
    }

    @Test
    @DisplayName("can go back to DELIVERY from REVIEW")
    void canGoBackToDeliveryFromReview() {
      CheckoutSession session = createSessionAtReview();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.DELIVERY);

      assertTrue(redirect.isEmpty(), "Should allow going back to DELIVERY");
    }

    @Test
    @DisplayName("can go back to PAYMENT from REVIEW")
    void canGoBackToPaymentFromReview() {
      CheckoutSession session = createSessionAtReview();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.PAYMENT);

      assertTrue(redirect.isEmpty(), "Should allow going back to PAYMENT");
    }
  }

  @Nested
  @DisplayName("Terminal State Tests")
  class TerminalStateTests {

    @Test
    @DisplayName("completed session allows CONFIRMATION access")
    void completedSessionAllowsConfirmationAccess() {
      CheckoutSession session = createCompletedSession();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.CONFIRMATION);

      assertTrue(redirect.isEmpty(), "Should allow CONFIRMATION access for completed session");
    }

    @Test
    @DisplayName("completed session redirects other steps to CONFIRMATION")
    void completedSessionRedirectsOtherStepsToConfirmation() {
      CheckoutSession session = createCompletedSession();

      for (CheckoutStep step : List.of(
          CheckoutStep.BUYER_INFO, CheckoutStep.DELIVERY,
          CheckoutStep.PAYMENT, CheckoutStep.REVIEW)) {
        Optional<String> redirect = validator.validateStepAccess(session, step);
        assertTrue(redirect.isPresent(), "Should redirect for step: " + step);
        assertEquals("/checkout/confirmation", redirect.get());
      }
    }

    @Test
    @DisplayName("abandoned session redirects to cart")
    void abandonedSessionRedirectsToCart() {
      CheckoutSession session = createAbandonedSession();

      for (CheckoutStep step : CheckoutStep.values()) {
        Optional<String> redirect = validator.validateStepAccess(session, step);
        assertTrue(redirect.isPresent(), "Should redirect for step: " + step);
        assertEquals("/cart", redirect.get());
      }
    }

    @Test
    @DisplayName("expired session redirects to cart")
    void expiredSessionRedirectsToCart() {
      CheckoutSession session = createExpiredSession();

      for (CheckoutStep step : CheckoutStep.values()) {
        Optional<String> redirect = validator.validateStepAccess(session, step);
        assertTrue(redirect.isPresent(), "Should redirect for step: " + step);
        assertEquals("/cart", redirect.get());
      }
    }

    @Test
    @DisplayName("confirmed session allows CONFIRMATION access")
    void confirmedSessionAllowsConfirmationAccess() {
      CheckoutSession session = createConfirmedSession();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.CONFIRMATION);

      assertTrue(redirect.isEmpty(), "Should allow CONFIRMATION access for confirmed session");
    }

    @Test
    @DisplayName("confirmed session redirects other steps to CONFIRMATION")
    void confirmedSessionRedirectsOtherStepsToConfirmation() {
      CheckoutSession session = createConfirmedSession();

      for (CheckoutStep step : List.of(
          CheckoutStep.BUYER_INFO, CheckoutStep.DELIVERY,
          CheckoutStep.PAYMENT, CheckoutStep.REVIEW)) {
        Optional<String> redirect = validator.validateStepAccess(session, step);
        assertTrue(redirect.isPresent(), "Should redirect for step: " + step);
        assertEquals("/checkout/confirmation", redirect.get());
      }
    }
  }

  @Nested
  @DisplayName("Valid Access Tests")
  class ValidAccessTests {

    @Test
    @DisplayName("allows access to current step")
    void allowsAccessToCurrentStep() {
      CheckoutSession session = createActiveSession();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.BUYER_INFO);

      assertTrue(redirect.isEmpty(), "Should allow access to current step");
    }

    @Test
    @DisplayName("allows access to DELIVERY when at DELIVERY step")
    void allowsAccessToDeliveryWhenAtDeliveryStep() {
      CheckoutSession session = createSessionAtDelivery();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.DELIVERY);

      assertTrue(redirect.isEmpty(), "Should allow access to DELIVERY step");
    }

    @Test
    @DisplayName("allows access to PAYMENT when at PAYMENT step")
    void allowsAccessToPaymentWhenAtPaymentStep() {
      CheckoutSession session = createSessionAtPayment();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.PAYMENT);

      assertTrue(redirect.isEmpty(), "Should allow access to PAYMENT step");
    }

    @Test
    @DisplayName("allows access to REVIEW when at REVIEW step")
    void allowsAccessToReviewWhenAtReviewStep() {
      CheckoutSession session = createSessionAtReview();

      Optional<String> redirect = validator.validateStepAccess(session, CheckoutStep.REVIEW);

      assertTrue(redirect.isEmpty(), "Should allow access to REVIEW step");
    }
  }

  @Nested
  @DisplayName("getCurrentStepPath Tests")
  class GetCurrentStepPathTests {

    @Test
    @DisplayName("returns correct path for BUYER_INFO")
    void returnsCorrectPathForBuyerInfo() {
      CheckoutSession session = createActiveSession();

      String path = validator.getCurrentStepPath(session);

      assertEquals("/checkout/buyer-info", path);
    }

    @Test
    @DisplayName("returns correct path for DELIVERY")
    void returnsCorrectPathForDelivery() {
      CheckoutSession session = createSessionAtDelivery();

      String path = validator.getCurrentStepPath(session);

      assertEquals("/checkout/delivery", path);
    }

    @Test
    @DisplayName("returns correct path for PAYMENT")
    void returnsCorrectPathForPayment() {
      CheckoutSession session = createSessionAtPayment();

      String path = validator.getCurrentStepPath(session);

      assertEquals("/checkout/payment", path);
    }

    @Test
    @DisplayName("returns correct path for REVIEW")
    void returnsCorrectPathForReview() {
      CheckoutSession session = createSessionAtReview();

      String path = validator.getCurrentStepPath(session);

      assertEquals("/checkout/review", path);
    }

    @Test
    @DisplayName("returns correct path for CONFIRMATION")
    void returnsCorrectPathForConfirmation() {
      CheckoutSession session = createConfirmedSession();

      String path = validator.getCurrentStepPath(session);

      assertEquals("/checkout/confirmation", path);
    }
  }

  // Helper methods to create test sessions in various states

  private CheckoutSession createActiveSession() {
    return CheckoutSession.start(
        CartId.generate(),
        CustomerId.of(UUID.randomUUID().toString()),
        List.of(createLineItem()),
        Money.of(BigDecimal.valueOf(100), EUR));
  }

  private CheckoutSession createSessionAtDelivery() {
    CheckoutSession session = createActiveSession();
    session.submitBuyerInfo(createBuyerInfo());
    return session;
  }

  private CheckoutSession createSessionAtPayment() {
    CheckoutSession session = createSessionAtDelivery();
    session.submitDelivery(createDeliveryAddress(), createShippingOption());
    return session;
  }

  private CheckoutSession createSessionAtReview() {
    CheckoutSession session = createSessionAtPayment();
    session.submitPayment(createPaymentSelection());
    return session;
  }

  private CheckoutSession createConfirmedSession() {
    CheckoutSession session = createSessionAtReview();
    session.confirm();
    return session;
  }

  private CheckoutSession createCompletedSession() {
    CheckoutSession session = createConfirmedSession();
    session.complete("ORD-123");
    return session;
  }

  private CheckoutSession createAbandonedSession() {
    CheckoutSession session = createActiveSession();
    session.abandon();
    return session;
  }

  private CheckoutSession createExpiredSession() {
    CheckoutSession session = createActiveSession();
    session.expire();
    return session;
  }

  private CheckoutLineItem createLineItem() {
    return CheckoutLineItem.of(
        CheckoutLineItemId.generate(),
        ProductId.generate(),
        "Test Product",
        Money.of(BigDecimal.valueOf(100), EUR),
        1,
        null);
  }

  private BuyerInfo createBuyerInfo() {
    return BuyerInfo.of("test@example.com", "John", "Doe", "+1234567890");
  }

  private DeliveryAddress createDeliveryAddress() {
    return DeliveryAddress.of("123 Main St", "Anytown", "12345", "Germany");
  }

  private ShippingOption createShippingOption() {
    return ShippingOption.of("standard", "Standard Shipping", "3-5 business days", Money.of(BigDecimal.valueOf(5), EUR));
  }

  private PaymentSelection createPaymentSelection() {
    return PaymentSelection.of(PaymentProviderId.of("stripe"));
  }
}
