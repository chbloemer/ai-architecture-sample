package de.sample.aiarchitecture.checkout.domain.service;

import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutStep;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainService;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Domain Service for validating checkout step navigation.
 *
 * <p>Enforces business rules for step access during checkout:
 * <ul>
 *   <li>Cannot access steps if session is invalid (null)</li>
 *   <li>Cannot skip ahead to steps without completing prerequisites</li>
 *   <li>Can go back to previously completed steps</li>
 *   <li>Cannot access steps in terminal session states</li>
 *   <li>Can only access CONFIRMATION step when session is completed</li>
 * </ul>
 *
 * <p>Returns redirect URLs when access is denied, allowing controllers
 * to redirect users to the appropriate step.
 */
public final class CheckoutStepValidator implements DomainService {

  private static final String CHECKOUT_BASE_PATH = "/checkout";
  private static final String CART_PATH = "/cart";

  /**
   * Validates if access to a specific checkout step is allowed.
   *
   * @param session the checkout session (may be null for invalid sessions)
   * @param targetStep the step the user wants to access
   * @return empty if access is allowed, or redirect URL if access is denied
   */
  public Optional<String> validateStepAccess(
      @Nullable final CheckoutSession session, @NonNull final CheckoutStep targetStep) {

    // Rule 1: Invalid session - redirect to cart
    if (session == null) {
      return Optional.of(CART_PATH);
    }

    // Rule 2: Terminal states handling (COMPLETED, ABANDONED, EXPIRED)
    if (session.status().isTerminal()) {
      return handleTerminalState(session, targetStep);
    }

    // Rule 3: CONFIRMED state - can only access CONFIRMATION
    if (session.status().canComplete()) {
      return handleConfirmedState(targetStep);
    }

    // Rule 4: CONFIRMATION step is only accessible after checkout is confirmed or completed
    if (targetStep == CheckoutStep.CONFIRMATION) {
      return handleConfirmationAccess(session);
    }

    // Rule 5: Cannot skip ahead - must complete prior steps
    if (isSkippingAhead(session, targetStep)) {
      return Optional.of(getStepPath(session.currentStep()));
    }

    // Access allowed
    return Optional.empty();
  }

  /**
   * Determines the appropriate redirect URL for a session that needs to be redirected
   * to its current valid step.
   *
   * @param session the checkout session
   * @return the URL path for the session's current step
   */
  public String getCurrentStepPath(@NonNull final CheckoutSession session) {
    return getStepPath(session.currentStep());
  }

  private Optional<String> handleTerminalState(
      final CheckoutSession session, final CheckoutStep targetStep) {

    return switch (session.status()) {
      case COMPLETED -> {
        // Completed sessions can only access CONFIRMATION
        if (targetStep == CheckoutStep.CONFIRMATION) {
          yield Optional.empty();
        }
        yield Optional.of(getStepPath(CheckoutStep.CONFIRMATION));
      }
      case ABANDONED, EXPIRED -> {
        // Abandoned/expired sessions redirect to cart to start fresh
        yield Optional.of(CART_PATH);
      }
      // CONFIRMED is not terminal - handled separately by handleConfirmedState
      // ACTIVE is not terminal - handled by normal flow
      default -> Optional.empty();
    };
  }

  private Optional<String> handleConfirmedState(final CheckoutStep targetStep) {
    // CONFIRMED sessions can only access CONFIRMATION step
    if (targetStep == CheckoutStep.CONFIRMATION) {
      return Optional.empty();
    }
    return Optional.of(getStepPath(CheckoutStep.CONFIRMATION));
  }

  private Optional<String> handleConfirmationAccess(final CheckoutSession session) {
    // CONFIRMATION is only accessible when status is CONFIRMED or COMPLETED
    if (session.status().canComplete() || session.isCompleted()) {
      return Optional.empty();
    }

    // Redirect to current step if trying to access CONFIRMATION prematurely
    return Optional.of(getStepPath(session.currentStep()));
  }

  private boolean isSkippingAhead(final CheckoutSession session, final CheckoutStep targetStep) {
    // Check if user is trying to access a step beyond their current progress
    final CheckoutStep currentStep = session.currentStep();

    // Cannot go to a step that comes after the current step
    if (targetStep.isAfter(currentStep)) {
      return true;
    }

    // For steps before or equal to current, also verify prerequisites are met
    // (going back is allowed, but going to a step whose prerequisites aren't met is not)
    return !arePrerequisitesMet(session, targetStep);
  }

  private boolean arePrerequisitesMet(final CheckoutSession session, final CheckoutStep targetStep) {
    return switch (targetStep) {
      case BUYER_INFO -> true; // First step, no prerequisites
      case DELIVERY -> session.isStepCompleted(CheckoutStep.BUYER_INFO);
      case PAYMENT -> session.isStepCompleted(CheckoutStep.BUYER_INFO)
          && session.isStepCompleted(CheckoutStep.DELIVERY);
      case REVIEW -> session.isStepCompleted(CheckoutStep.BUYER_INFO)
          && session.isStepCompleted(CheckoutStep.DELIVERY)
          && session.isStepCompleted(CheckoutStep.PAYMENT);
      case CONFIRMATION -> session.isCompleted();
    };
  }

  private String getStepPath(final CheckoutStep step) {
    final String stepName = step.name().toLowerCase().replace("_", "-");
    return CHECKOUT_BASE_PATH + "/" + stepName;
  }
}
