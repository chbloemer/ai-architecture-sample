# Transaction Management

Application services are the transactional boundary - each public method represents a use case running within a single transaction.

## Pattern

```java
@Service
@Transactional  // Class-level: all public methods transactional
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

## Read-Only Transactions

Use `@Transactional(readOnly = true)` for query methods:

```java
@Transactional(readOnly = true)
public List<Product> getAllProducts() {
    return productRepository.findAll();
}
```

**Benefits:** Performance optimization, prevents accidental writes, clearer intent

## Transactional Event Listeners

Events are handled only after successful commit:

```java
@Component
public class ProductEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductCreated(ProductCreated event) {
        // Executes only after successful commit
        // Safe to trigger downstream operations
        log.info("Product created: {}", event.productId());
    }
}
```

## Transaction Flow

```
REST Controller (no transaction)
    ↓
@Transactional Application Service ← TRANSACTION BEGINS
    1. Execute domain logic (events raised)
    2. Save to repository
    3. Publish events
    ↓ COMMIT
@TransactionalEventListener ← AFTER_COMMIT
    Handle events
```

## Key Rules

1. **Application Services = Transactional Boundary** - Controllers have NO `@Transactional`
2. **Read-Only for Queries** - Use `readOnly = true` for query methods
3. **Events After Commit** - Use `@TransactionalEventListener(phase = AFTER_COMMIT)`
4. **One Transaction Per Use Case** - Each public method is one transaction
5. **Domain Layer Never Transactional** - Domain objects remain framework-independent

## Anti-Patterns to Avoid

❌ **Controller with @Transactional:**
```java
@RestController
@Transactional  // WRONG - transaction boundary too broad
public class ProductResource { }
```

❌ **Event Listener Without Transactional Phase:**
```java
@EventListener  // WRONG - may execute before commit
public void onProductCreated(ProductCreated event) { }
```

❌ **Domain Layer with @Transactional:**
```java
@Transactional  // WRONG - breaks framework independence
public class Product { }
```

## Related Documentation

- [Architecture Principles](architecture-principles.md) - Application Service patterns
- [ADR-002: Framework-Independent Domain](adr/adr-002-framework-independent-domain.md)
