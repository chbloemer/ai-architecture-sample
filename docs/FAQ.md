# Architecture FAQ

Frequently asked questions about Clean Architecture and Hexagonal Architecture terminology used in this project.

---

## Q: Is the output model the output port in Clean Architecture?

**No.** The output model is NOT the output port.

### Terminology Breakdown

**Input Side (Primary/Driving):**
- **Input Port** = Use Case Class (e.g., `CreateProductUseCase`)
  - Concrete classes that implement `UseCase<I,O>`
  - What primary adapters depend on
  - Defines WHAT the application can do

- **Input Model** = Request data (e.g., `CreateProductInput`)
  - Data passed TO the use case

- **Output Model** = Response data (e.g., `CreateProductOutput`)
  - Data returned FROM the use case

**Output Side (Secondary/Driven):**
- **Output Port** = Repository/Gateway Interface (e.g., `ProductRepository`)
  - Interface defined in domain/application layer
  - Implemented by secondary adapters (persistence, external APIs)
  - What the application uses to interact with infrastructure

### Example from This Codebase

```java
// BASE USE CASE CONTRACT
public interface UseCase<I, O> {
    @NonNull O execute(@NonNull I input);
}

// INPUT PORT (Primary Port) - Concrete use case class
@Service
@Transactional
public class CreateProductUseCase implements UseCase<CreateProductInput, CreateProductOutput> {
    private final ProductRepository productRepository;

    @Override
    public CreateProductOutput execute(CreateProductInput input) {
        // Business logic here
    }
}

// INPUT MODEL
public record CreateProductInput(
    String sku,
    String name,
    BigDecimal priceAmount,
    String priceCurrency,
    String category,
    int stockQuantity
) {}

// OUTPUT MODEL
public record CreateProductOutput(
    String productId,
    String sku,
    String name,
    BigDecimal priceAmount,
    String priceCurrency,
    String category,
    int stockQuantity
) {}

// OUTPUT PORT (Secondary Port)
public interface ProductRepository extends Repository<Product, ProductId> {
    void save(Product product);
    Optional<Product> findById(ProductId id);
}
```

### Key Distinction

- **Output Model** = What the use case RETURNS to its caller (presentation layer)
- **Output Port** = What the use case USES to access external systems (infrastructure layer)

The naming can be confusing:
- "Output" in "Output Model" means output FROM the use case TO the caller
- "Output" in "Output Port" means output FROM the application TO infrastructure

---

## Q: Is the output port in Hexagonal Architecture the same as in Clean Architecture?

**Yes.** Output ports are essentially the same concept in both architectures, with different terminology.

### Terminology Comparison

**Hexagonal Architecture (Alistair Cockburn):**
- **Primary Ports (Driving Ports)** = Interfaces exposed BY the application
- **Primary Adapters (Driving Adapters)** = REST controllers, CLI, GUI
- **Secondary Ports (Driven Ports)** = Interfaces REQUIRED by the application
- **Secondary Adapters (Driven Adapters)** = Database, external APIs, file system

**Clean Architecture (Robert C. Martin):**
- **Input Ports** = Use Case interfaces (same as Primary Ports)
- **Output Ports** = Repository/Gateway interfaces (same as Secondary Ports)
- Uses "Input/Output" from the application's perspective

### The Same Concept

**Output Port = Secondary Port = Driven Port**

All three terms refer to:
- Interfaces defined in the domain/application layer
- Required by the application to work with external systems
- Implemented by infrastructure/adapter layer
- Follow the Dependency Inversion Principle

### Example

```java
// This is ALL of the following:
// - Output Port (Clean Architecture)
// - Secondary Port (Hexagonal Architecture)
// - Driven Port (Hexagonal Architecture alternative)

package de.sample.aiarchitecture.domain.model.product;

public interface ProductRepository extends Repository<Product, ProductId> {
    void save(Product product);
    Optional<Product> findById(ProductId id);
    List<Product> findAll();
    boolean existsBySku(SKU sku);
}

// The implementation is the Secondary/Driven Adapter:
package de.sample.aiarchitecture.portadapter.outgoing.persistence;

@Repository
class ProductJpaRepository implements ProductRepository {
    // Actual database implementation
}
```

### Why Different Names?

**Hexagonal Architecture:**
- Uses "Primary/Secondary" based on who initiates the call
  - **Primary** = Someone calls YOU (inbound)
  - **Secondary** = YOU call someone else (outbound)

**Clean Architecture:**
- Uses "Input/Output" from application's perspective
  - **Input** = Data/requests coming IN to the application
  - **Output** = Going OUT from application to infrastructure

Both describe the **exact same architectural pattern** with different terminology preferences.

---

## Q: How do ports and adapters relate to use cases?

**Use Case Classes ARE Input Ports (Primary Ports).**

### The Relationship

```
Primary Adapter → Input Port (Use Case Class) → Output Port → Secondary Adapter
(REST API)       (Business Logic)              (Repository)  (Database)
```

**Concrete Example:**

```java
// Primary Adapter (Incoming)
@RestController
public class ProductResource {
    private final CreateProductUseCase createProductUseCase; // Depends on Input Port

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(CreateProductRequest request) {
        CreateProductInput input = // Convert request to input
        CreateProductOutput output = createProductUseCase.execute(input);
        return // Convert output to DTO
    }
}

// Input Port (Primary Port) - Use Case Class
@Service
@Transactional
public class CreateProductUseCase implements UseCase<CreateProductInput, CreateProductOutput> {
    private final ProductRepository productRepository; // Depends on Output Port
    private final DomainEventPublisher eventPublisher;

    @Override
    public CreateProductOutput execute(CreateProductInput input) {
        // Business logic
        Product product = // Create product
        productRepository.save(product); // Uses Output Port
        return // Map to output
    }
}

// Output Port (Secondary Port)
public interface ProductRepository {
    void save(Product product);
}

// Secondary Adapter (Outgoing)
@Repository
class ProductJpaRepositoryAdapter implements ProductRepository {
    // JPA implementation
}
```

---

## Q: Where do Input/Output models belong in the architecture?

**Input and Output models belong in the application layer.**

### Package Location

```
de.sample.aiarchitecture.application/
├── UseCase.java                    # Base interface (generic contract)
├── CreateProductUseCase.java       # Input Port (use case class)
├── CreateProductInput.java         # Input Model
└── CreateProductOutput.java        # Output Model
```

### Why Application Layer?

1. **Decoupling**: Prevents domain layer from knowing about presentation concerns
2. **Stability**: Input/Output can change without affecting domain
3. **Translation**: Acts as anti-corruption layer between presentation and domain
4. **Testing**: Easy to test use cases with simple data structures

### What Goes Where?

**Application Layer:**
- Base UseCase interface (generic contract)
- Use Case classes (Input Ports - e.g., CreateProductUseCase, GetCartByIdUseCase)
- Input models (request data)
- Output models (response data)

**Domain Layer:**
- Entities, Value Objects, Aggregates
- Domain Services
- Repository interfaces (Output Ports)
- Domain Events
- Specifications

**Adapter Layer:**
- REST controllers (Primary Adapters)
- Repository implementations (Secondary Adapters)
- DTOs (presentation concern)
- Converters between DTOs and Input/Output models

---

## Q: What's the difference between a DTO and an Output Model?

**DTOs are adapter concerns; Output Models are application concerns.**

### Key Differences

| Aspect | Output Model | DTO |
|--------|-------------|-----|
| **Layer** | Application | Adapter (Presentation) |
| **Purpose** | Use case response | External API contract |
| **Framework** | Framework-independent | May use framework annotations |
| **Audience** | Adapters | External clients |
| **Stability** | Can change with use case | Should be stable (API versioning) |

### Example

```java
// Output Model (Application Layer)
package de.sample.aiarchitecture.application;

public record CreateProductOutput(
    String productId,
    String sku,
    String name,
    BigDecimal priceAmount,
    String priceCurrency
) {}

// DTO (Adapter Layer)
package de.sample.aiarchitecture.portadapter.incoming.api.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductDto(
    @JsonProperty("id") String productId,
    @JsonProperty("sku") String sku,
    @JsonProperty("name") String name,
    @JsonProperty("price") BigDecimal priceAmount,
    @JsonProperty("currency") String priceCurrency
) {}
```

### Conversion Flow

```
External Request (JSON)
    ↓
DTO (REST layer)
    ↓ Adapter converts
Input Model (Application layer)
    ↓ Use case processes
Output Model (Application layer)
    ↓ Adapter converts
DTO (REST layer)
    ↓
External Response (JSON)
```

---

## References

- **Clean Architecture** by Robert C. Martin (Uncle Bob)
- **Hexagonal Architecture** by Alistair Cockburn
- **Get Your Hands Dirty on Clean Architecture** by Tom Hombergs
- [Architecture Principles](./architecture/architecture-principles.md) - Project documentation
