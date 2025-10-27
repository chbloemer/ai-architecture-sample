package de.sample.aiarchitecture.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Transaction management configuration.
 *
 * <p>This configuration enables transaction management for the application. Since this is a sample
 * project using in-memory repositories (no actual database), we use a simple
 * {@link InMemoryTransactionManager} which provides transaction semantics without requiring
 * a physical resource.
 *
 * <p><b>Transactional Boundaries:</b>
 *
 * <p>In this application, <b>application services are the transactional boundary</b>. Each use
 * case method in an application service runs within a single transaction. This ensures:
 *
 * <ul>
 *   <li>Atomic operations: Either all changes succeed or all are rolled back
 *   <li>Consistent state: Domain invariants are maintained
 *   <li>Isolated execution: Concurrent operations don't interfere
 *   <li>Event publishing only on success: Domain events are published only after successful commit
 * </ul>
 *
 * <p><b>Usage Pattern:</b>
 *
 * <pre>
 * {@literal @}Service
 * public class ProductApplicationService {
 *
 *   {@literal @}Transactional
 *   public Product createProduct(...) {
 *     // 1. Domain logic executes
 *     Product product = productFactory.createProduct(...);
 *
 *     // 2. Repository save
 *     productRepository.save(product);
 *
 *     // 3. Events published (after commit via @TransactionalEventListener)
 *     eventPublisher.publishAndClearEvents(product);
 *
 *     return product;
 *   }
 * }
 * </pre>
 *
 * <p><b>Why InMemoryTransactionManager?</b>
 *
 * <p>This sample project uses in-memory repositories (ConcurrentHashMap) for simplicity. In a
 * production application with a real database, you would use:
 *
 * <ul>
 *   <li>{@code JpaTransactionManager} for JPA/Hibernate
 *   <li>{@code DataSourceTransactionManager} for JDBC
 *   <li>{@code JmsTransactionManager} for JMS
 * </ul>
 *
 * <p>The {@code InMemoryTransactionManager} provides transaction semantics (begin, commit,
 * rollback) without requiring an actual transactional resource, making it perfect for this
 * educational sample.
 *
 * <p><b>Note:</b> In a real application with a database, simply adding
 * {@code spring-boot-starter-data-jpa} would auto-configure a proper transaction manager.
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfiguration {

  /**
   * Creates a transaction manager for in-memory operations.
   *
   * <p>This transaction manager doesn't manage actual resources (like database connections) but
   * provides transaction semantics for testing and demonstration purposes.
   *
   * <p>In a production application, this would be replaced with a real transaction manager
   * configured automatically by Spring Boot based on your data access technology.
   *
   * @return a platform transaction manager for in-memory operations
   */
  @Bean
  public PlatformTransactionManager transactionManager() {
    return new InMemoryTransactionManager();
  }

  /**
   * Simple transaction manager for in-memory repositories.
   *
   * <p>This transaction manager provides transaction semantics (begin, commit, rollback) without
   * requiring an actual transactional resource like a database connection. It's suitable for:
   *
   * <ul>
   *   <li>Sample applications with in-memory repositories
   *   <li>Testing transaction boundaries
   *   <li>Demonstrating transaction patterns
   * </ul>
   *
   * <p>The main benefit is enabling {@link org.springframework.transaction.event.TransactionalEventListener}
   * to work correctly, allowing event listeners to fire after transaction commit.
   *
   * <p><b>Important:</b> This is NOT suitable for production use. In production, use a real
   * transaction manager appropriate for your data access technology.
   */
  private static class InMemoryTransactionManager extends AbstractPlatformTransactionManager {

    @Override
    protected Object doGetTransaction() throws TransactionException {
      return new Object();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition)
        throws TransactionException {
      // No-op: No actual resource to begin transaction on
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
      // No-op: No actual resource to commit
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
      // No-op: No actual resource to rollback
    }
  }
}
