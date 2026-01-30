package de.sample.aiarchitecture.checkout.application.shared;

import de.sample.aiarchitecture.checkout.domain.model.CartId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.sharedkernel.application.port.Repository;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

/**
 * Repository interface for CheckoutSession aggregate.
 *
 * <p>Provides collection-like access to CheckoutSession aggregates using domain language.
 * Implementation resides in the secondary adapter layer.
 *
 * <p>Extends the base {@link Repository} interface which provides common methods:
 * <ul>
 *   <li>{@code findById(CheckoutSessionId)} - inherited from base interface
 *   <li>{@code save(CheckoutSession)} - inherited from base interface
 *   <li>{@code deleteById(CheckoutSessionId)} - inherited from base interface
 * </ul>
 */
public interface CheckoutSessionRepository extends Repository<CheckoutSession, CheckoutSessionId> {

  /**
   * Finds a checkout session by the cart it was created from.
   *
   * <p>A cart can have at most one active checkout session at a time.
   *
   * @param cartId the cart ID
   * @return the checkout session if found, empty otherwise
   */
  Optional<CheckoutSession> findByCartId(@NonNull CartId cartId);

  /**
   * Finds all checkout sessions that have expired.
   *
   * <p>A session is considered expired when its status is EXPIRED. This method
   * is typically used by cleanup processes or for reporting purposes.
   *
   * @return list of expired checkout sessions
   */
  List<CheckoutSession> findExpiredSessions();

  /**
   * Retrieves all checkout sessions.
   *
   * @return list of all checkout sessions
   */
  List<CheckoutSession> findAll();
}
