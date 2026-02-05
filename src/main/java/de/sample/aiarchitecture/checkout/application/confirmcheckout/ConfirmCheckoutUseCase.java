package de.sample.aiarchitecture.checkout.application.confirmcheckout;

import de.sample.aiarchitecture.checkout.application.shared.CheckoutArticleDataPort;
import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutArticle;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutArticlePriceResolver;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.DomainEventPublisher;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for confirming a checkout session.
 *
 * <p>This use case handles the confirmation step by:
 * <ul>
 *   <li>Loading and validating the checkout session</li>
 *   <li>Fetching fresh article data (pricing, availability) via CheckoutArticleDataPort</li>
 *   <li>Building a resolver for current pricing validation</li>
 *   <li>Calling the domain method to confirm with validation</li>
 *   <li>Persisting the updated session</li>
 *   <li>The domain raises the CheckoutConfirmed integration event</li>
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link ConfirmCheckoutInputPort}
 * interface, which is a primary/driving port in the application layer.
 *
 * <p><b>Bounded Context Isolation:</b> This use case accesses article data (pricing, availability)
 * through {@link CheckoutArticleDataPort} output port to validate items before final confirmation.
 */
@Service
@Transactional
public class ConfirmCheckoutUseCase implements ConfirmCheckoutInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final CheckoutArticleDataPort checkoutArticleDataPort;
  private final DomainEventPublisher domainEventPublisher;

  public ConfirmCheckoutUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final CheckoutArticleDataPort checkoutArticleDataPort,
      final DomainEventPublisher domainEventPublisher) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.checkoutArticleDataPort = checkoutArticleDataPort;
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

    // Collect product IDs from line items
    final List<ProductId> productIds = session.lineItems().stream()
        .map(item -> item.productId())
        .toList();

    // Fetch fresh article data (pricing, availability) for validation
    final Map<ProductId, CheckoutArticle> articleDataMap =
        checkoutArticleDataPort.getArticleData(productIds);

    // Build resolver from fetched data
    final CheckoutArticlePriceResolver resolver = productId -> {
      final CheckoutArticle article = articleDataMap.get(productId);
      if (article == null) {
        throw new IllegalArgumentException("Article data not found for: " + productId.value());
      }
      return new CheckoutArticlePriceResolver.ArticlePrice(
          article.currentPrice(), article.isAvailable(), article.availableStock());
    };

    // Confirm checkout with validation (domain validates state, step, completeness, and items)
    // This raises the CheckoutConfirmed integration event
    session.confirm(resolver);

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
