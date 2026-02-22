package de.sample.aiarchitecture.checkout.application.confirmcheckout;

import de.sample.aiarchitecture.checkout.application.shared.CartDataPort;
import de.sample.aiarchitecture.checkout.application.shared.CheckoutArticleDataPort;
import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.application.shared.StockReductionPort;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutArticle;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutArticlePriceResolver;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.DomainEventPublisher;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for confirming a checkout session.
 *
 * <p>This use case handles the confirmation step by:
 *
 * <ul>
 *   <li>Loading and validating the checkout session
 *   <li>Fetching fresh article data (pricing, availability) via CheckoutArticleDataPort
 *   <li>Building a resolver for current pricing validation
 *   <li>Calling the domain method to confirm with validation
 *   <li>Persisting the updated session
 *   <li>The domain raises the CheckoutConfirmed integration event
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
  private final CartDataPort cartDataPort;
  private final StockReductionPort stockReductionPort;
  private final DomainEventPublisher domainEventPublisher;

  public ConfirmCheckoutUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final CheckoutArticleDataPort checkoutArticleDataPort,
      final CartDataPort cartDataPort,
      final StockReductionPort stockReductionPort,
      final DomainEventPublisher domainEventPublisher) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.checkoutArticleDataPort = checkoutArticleDataPort;
    this.cartDataPort = cartDataPort;
    this.stockReductionPort = stockReductionPort;
    this.domainEventPublisher = domainEventPublisher;
  }

  @Override
  public ConfirmCheckoutResult execute(final ConfirmCheckoutCommand command) {
    // Load session
    final CheckoutSessionId sessionId = CheckoutSessionId.of(command.sessionId());
    final CheckoutSession session =
        checkoutSessionRepository
            .findById(sessionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Session not found: " + command.sessionId()));

    // Collect product IDs from line items
    final List<ProductId> productIds =
        session.lineItems().stream().map(item -> item.productId()).toList();

    // Fetch fresh article data (pricing, availability) for validation
    final Map<ProductId, CheckoutArticle> articleDataMap =
        checkoutArticleDataPort.getArticleData(productIds);

    // Build resolver from fetched data
    final CheckoutArticlePriceResolver resolver =
        productId -> {
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

    // Publish domain events (triggers cross-context listeners: inventory, product)
    domainEventPublisher.publishAndClearEvents(session);

    // Complete the cart synchronously via Cart API (replaces event-driven cart↔checkout cycle)
    cartDataPort.markAsCompleted(session.cartId());

    // Reduce stock for all ordered items via Inventory API
    session
        .lineItems()
        .forEach(item -> stockReductionPort.reduceStock(item.productId(), item.quantity()));

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
