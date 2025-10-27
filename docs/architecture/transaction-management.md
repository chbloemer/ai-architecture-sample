# Transaction Management

This document describes the transaction management pattern used in this application.

## Overview

In this application, **application services are the transactional boundary**. Each public method in an application service represents a use case and runs within a single transaction.

## Key Principles

### 1. Application Services as Transactional Boundaries

```java
@Service
@Transactional  // Class-level: all public methods are transactional
public class ProductApplicationService {

    @Transactional  // Write operation (default)
    public Product createProduct(...) {
        // 1. Domain logic
        Product product = productFactory.createProduct(...);

        // 2. Persistence
        productRepository.save(product);

        // 3. Event publishing
        eventPublisher.publishAndClearEvents(product);

        return product;
    }

    @Transactional(readOnly = true)  // Read-only optimization
    public Optional<Product> findProductById(ProductId id) {
        return productRepository.findById(id);
    }
}
```

### 2. Read-Only Transactions

Use `@Transactional(readOnly = true)` for query methods:

```java
@Transactional(readOnly = true)
public List<Product> getAllProducts() {
    return productRepository.findAll();
}
```

**Benefits:**
- Performance optimization
- Prevents accidental writes
- Better resource utilization
- Clearer intent

### 3. Transactional Event Listeners

Event listeners use `@TransactionalEventListener` to ensure events are only handled after successful transaction commit:

```java
@Component
public class ProductEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductCreated(ProductCreated event) {
        // This only executes after successful commit
        // Guarantees the product was actually persisted
        log.info("Product created: {}", event.productId());

        // Safe to trigger downstream operations:
        // - Update search index
        // - Send notifications
        // - Publish to message queue
    }
}
```

## Transaction Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  REST Controller                                                │
│  (No transaction - delegates to application service)            │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  @Transactional                                                 │
│  Application Service    ← TRANSACTION BEGINS                    │
├─────────────────────────────────────────────────────────────────┤
│  1. Execute domain logic                                        │
│     - Aggregate enforces invariants                             │
│     - Domain events raised (not published yet)                  │
│                                                                 │
│  2. Save to repository                                          │
│     - Aggregate persisted                                       │
│     - Events still pending                                      │
│                                                                 │
│  3. Publish events                                              │
│     - Events sent to Spring event system                        │
│     - Listeners registered but NOT executed yet                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼ TRANSACTION COMMITS
┌─────────────────────────────────────────────────────────────────┐
│  @TransactionalEventListener(AFTER_COMMIT)                      │
│  Event Listeners        ← EXECUTES AFTER COMMIT                 │
├─────────────────────────────────────────────────────────────────┤
│  - Update search indexes                                        │
│  - Send notifications                                           │
│  - Coordinate with other bounded contexts                       │
│  - Trigger async processes                                      │
└─────────────────────────────────────────────────────────────────┘
```

## Benefits

### 1. Atomic Operations

All changes within a use case either succeed together or fail together:

```java
@Transactional
public void updateProductPrice(ProductId productId, Price newPrice) {
    Product product = productRepository.findById(productId)
        .orElseThrow();

    // If this fails, nothing is committed
    product.changePrice(newPrice);

    // If this fails, price change is rolled back
    productRepository.save(product);

    // If this fails, everything is rolled back
    eventPublisher.publishAndClearEvents(product);
}
```

### 2. Consistent State

Domain invariants are maintained across the transaction:

```java
@Transactional
public void addItemToCart(CartId cartId, ProductId productId, Quantity quantity) {
    // Both operations succeed or both fail
    ShoppingCart cart = cartRepository.findById(cartId).orElseThrow();
    Product product = productRepository.findById(productId).orElseThrow();

    // Invariant: Product must have sufficient stock
    if (!product.hasStockFor(quantity.value())) {
        throw new IllegalArgumentException("Insufficient stock");
    }

    cart.addItem(productId, quantity, product.price());
    cartRepository.save(cart);
    eventPublisher.publishAndClearEvents(cart);

    // If transaction fails, cart is not modified
}
```

### 3. Event Publishing Only on Success

Events are only published if the transaction commits successfully:

```java
@Transactional
public Product createProduct(...) {
    Product product = productFactory.createProduct(...);  // Raises ProductCreated event
    productRepository.save(product);
    eventPublisher.publishAndClearEvents(product);

    // If exception occurs before commit, event is never published
    // Event listeners only execute AFTER_COMMIT
}
```

### 4. Isolation

Concurrent transactions don't interfere with each other:

```java
// Transaction A
@Transactional
public void updateProductPrice(ProductId id, Price newPrice) {
    Product product = productRepository.findById(id).orElseThrow();
    product.changePrice(newPrice);
    productRepository.save(product);
}

// Transaction B (concurrent)
@Transactional(readOnly = true)
public Optional<Product> findProductById(ProductId id) {
    // Sees consistent state based on isolation level
    return productRepository.findById(id);
}
```

## Transaction Configuration

### InMemoryTransactionManager

This sample application uses an in-memory repository (ConcurrentHashMap) without a database. We use a custom `InMemoryTransactionManager` which provides transaction semantics without requiring a physical resource:

```java
@Configuration
@EnableTransactionManagement
public class TransactionConfiguration {

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new InMemoryTransactionManager();
    }

    /**
     * Simple transaction manager for in-memory repositories.
     * Extends AbstractPlatformTransactionManager to provide transaction
     * semantics without actual database resources.
     */
    private static class InMemoryTransactionManager extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            // No-op: No actual resource to begin transaction on
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            // No-op: No actual resource to commit
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            // No-op: No actual resource to rollback
        }
    }
}
```

**Why a custom transaction manager?**

The main benefit is enabling `@TransactionalEventListener` to work correctly, ensuring event listeners fire only after successful transaction commit. This is critical for maintaining consistency between aggregates and bounded contexts.

### Production Configuration

In a production application with a real database, Spring Boot auto-configures the appropriate transaction manager:

**JPA/Hibernate:**
```java
// Automatically configured when spring-boot-starter-data-jpa is present
// No manual configuration needed
```

**JDBC:**
```java
@Bean
public PlatformTransactionManager transactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
}
```

**JMS:**
```java
@Bean
public PlatformTransactionManager transactionManager(ConnectionFactory connectionFactory) {
    return new JmsTransactionManager(connectionFactory);
}
```

## Best Practices

### 1. Keep Transactions Short

```java
// ❌ BAD: Long-running transaction
@Transactional
public void processOrder(OrderId orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.process();
    orderRepository.save(order);

    // DON'T: Slow external call inside transaction
    paymentGateway.processPayment(order);  // Holds transaction open!
    emailService.sendConfirmation(order);  // Holds transaction open!
}

// ✅ GOOD: Short transaction, external calls outside
@Transactional
public void processOrder(OrderId orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.process();
    orderRepository.save(order);
    eventPublisher.publishAndClearEvents(order);  // Raises OrderProcessed event
}

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onOrderProcessed(OrderProcessed event) {
    // External calls happen outside the transaction
    paymentGateway.processPayment(event.orderId());
    emailService.sendConfirmation(event.orderId());
}
```

### 2. Use Appropriate Isolation Levels

```java
// Default isolation (usually READ_COMMITTED)
@Transactional
public void updateProduct(Product product) {
    productRepository.save(product);
}

// Higher isolation when needed
@Transactional(isolation = Isolation.SERIALIZABLE)
public void criticalOperation() {
    // Prevents phantom reads, non-repeatable reads
}
```

### 3. Handle Exceptions Appropriately

```java
@Transactional
public void updateProduct(ProductId id, Price newPrice) {
    try {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        product.changePrice(newPrice);
        productRepository.save(product);

    } catch (ProductNotFoundException e) {
        // RuntimeException: transaction will roll back
        log.error("Product not found: {}", id);
        throw e;
    } catch (IllegalArgumentException e) {
        // RuntimeException: transaction will roll back
        log.error("Invalid price: {}", newPrice);
        throw e;
    }
}

// Mark specific exceptions as non-rolling back if needed
@Transactional(noRollbackFor = ValidationException.class)
public void validateAndUpdate(Product product) {
    // ValidationException won't trigger rollback
}
```

### 4. Don't Call Transactional Methods from Same Class

```java
// ❌ BAD: Internal call bypasses transaction proxy
@Service
@Transactional
public class ProductService {

    public void publicMethod() {
        // This call does NOT go through transaction proxy!
        this.internalTransactionalMethod();  // Transaction not applied!
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void internalTransactionalMethod() {
        // Transaction settings ignored!
    }
}

// ✅ GOOD: Extract to separate service
@Service
public class ProductService {
    private final ProductInternalService internalService;

    @Transactional
    public void publicMethod() {
        // This call goes through proxy - transaction applied
        internalService.transactionalMethod();
    }
}

@Service
public class ProductInternalService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transactionalMethod() {
        // Transaction settings applied correctly
    }
}
```

### 5. Use Propagation Appropriately

```java
// REQUIRED (default): Join existing transaction or create new one
@Transactional(propagation = Propagation.REQUIRED)
public void method1() { }

// REQUIRES_NEW: Always create new transaction, suspend current
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void independentOperation() {
    // Commits/rolls back independently of caller
}

// MANDATORY: Must be called within existing transaction
@Transactional(propagation = Propagation.MANDATORY)
public void mustBeInTransaction() {
    // Throws exception if no transaction exists
}

// NEVER: Must NOT be called within transaction
@Transactional(propagation = Propagation.NEVER)
public void mustBeOutsideTransaction() {
    // Throws exception if transaction exists
}
```

## Testing Transactions

### Unit Tests (No Transactions)

```java
class ProductApplicationServiceTest {

    @Test
    void shouldCreateProduct() {
        // Unit test - no actual transactions
        ProductRepository mockRepository = mock(ProductRepository.class);
        ProductApplicationService service = new ProductApplicationService(
            mockRepository,
            new ProductFactory(),
            mock(DomainEventPublisher.class)
        );

        Product product = service.createProduct(...);

        verify(mockRepository).save(any(Product.class));
    }
}
```

### Integration Tests (With Transactions)

```java
@SpringBootTest
@Transactional  // Rollback after each test
class ProductApplicationServiceIntegrationTest {

    @Autowired
    private ProductApplicationService service;

    @Autowired
    private ProductRepository repository;

    @Test
    void shouldCreateProductWithTransaction() {
        // Real transaction
        Product product = service.createProduct(...);

        // Verify in database
        assertThat(repository.findById(product.id())).isPresent();

        // Rolled back after test
    }
}
```

## Common Patterns

### Pattern 1: Command-Query Separation

```java
@Service
@Transactional
public class ProductApplicationService {

    // Commands: Write operations (default @Transactional)
    public Product createProduct(...) { }
    public void updateProduct(...) { }
    public void deleteProduct(...) { }

    // Queries: Read operations (@Transactional(readOnly = true))
    @Transactional(readOnly = true)
    public Optional<Product> findById(ProductId id) { }

    @Transactional(readOnly = true)
    public List<Product> findAll() { }
}
```

### Pattern 2: Event-Driven Coordination

```java
// Aggregate raises event
@Transactional
public void checkout(CartId cartId) {
    ShoppingCart cart = cartRepository.findById(cartId).orElseThrow();
    cart.checkout();  // Raises CartCheckedOut event
    cartRepository.save(cart);
    eventPublisher.publishAndClearEvents(cart);
}

// Event handler coordinates with other contexts
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onCartCheckedOut(CartCheckedOut event) {
    // Each in its own transaction
    inventoryService.reserveInventory(event.cartId());
    orderService.createOrder(event.cartId());
    paymentService.initiatePayment(event.totalAmount());
}
```

### Pattern 3: Saga for Distributed Transactions

```java
// Orchestration-based saga
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onOrderCreated(OrderCreated event) {
    try {
        // Step 1: Reserve inventory
        inventoryService.reserve(event.orderId());

        // Step 2: Process payment
        paymentService.process(event.orderId());

        // Step 3: Arrange shipping
        shippingService.arrange(event.orderId());

    } catch (InventoryException e) {
        // Compensate: Cancel order
        orderService.cancel(event.orderId());
    } catch (PaymentException e) {
        // Compensate: Release inventory, cancel order
        inventoryService.release(event.orderId());
        orderService.cancel(event.orderId());
    }
}
```

## Summary

### Key Takeaways

1. **Application services are transactional boundaries** - Use `@Transactional` on service classes
2. **Read-only optimization** - Use `@Transactional(readOnly = true)` for queries
3. **Events after commit** - Use `@TransactionalEventListener(AFTER_COMMIT)` for event handlers
4. **Keep transactions short** - Move slow operations to event listeners
5. **One use case = One transaction** - Each public service method is one transaction
6. **Atomic operations** - All changes succeed or all fail
7. **Event publishing only on success** - Events only published after commit

### References

- **Spring Transaction Management**: https://docs.spring.io/spring-framework/reference/data-access/transaction.html
- **@Transactional**: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/annotation/Transactional.html
- **@TransactionalEventListener**: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/event/TransactionalEventListener.html
- **Implementing Domain-Driven Design** by Vaughn Vernon - Chapter 10: "Aggregates"

---

**Last Updated:** 2025-10-24

*This document is part of the architecture documentation. Keep it synchronized with code changes.*
