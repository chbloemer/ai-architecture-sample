Implementation Summary

1. Created Use Case Infrastructure

- Base UseCase<I, O> interface in /src/main/java/de/sample/aiarchitecture/application/UseCase.java
- Defines the contract for all use cases with execute() method

2. Product Use Cases

   Use Case Interfaces:
- CreateProductUseCase - Create new products
- UpdateProductPriceUseCase - Update product prices
- GetProductByIdUseCase - Query product by ID
- GetAllProductsUseCase - Query all products

  Input/Output Models:
- CreateProductInput/Output - Immutable records for product creation
- UpdateProductPriceInput/Output - Price update models
- GetProductByIdInput/Output - Query models
- GetAllProductsInput/Output - List query models

  Implementations:
- CreateProductUseCaseImpl, UpdateProductPriceUseCaseImpl, etc. (package-private)

3. Shopping Cart Use Cases

   Use Case Interfaces:
- CreateCartUseCase - Create new carts
- AddItemToCartUseCase - Add items to cart
- CheckoutCartUseCase - Checkout process
- GetCartByIdUseCase - Query cart

  Input/Output Models:
- All with corresponding Input/Output records

  Implementations:
- Package-private implementations for each use case

4. Updated Architecture Documentation

   Added comprehensive Clean Architecture section in /docs/architecture/architecture-principles.md:
- Use Case pattern explanation
- Command vs Query use cases (CQRS-lite)
- Input/Output model design rules
- Benefits and comparison with traditional Application Services
- Relationship to Hexagonal Architecture (Use Cases = Input Ports)

5. Updated ArchUnit Tests

   Modified naming conventions tests to support both:
- Legacy pattern: *ApplicationService
- Use case pattern: *UseCaseImpl implementing UseCase<I, O>

Key Principles Followed

✅ Interface Segregation Principle - Each use case is a separate interface✅ Single Responsibility - One use case per class✅ Dependency Inversion - Use cases depend on abstractions (Repository interfaces)✅
Framework Independence - Use case interfaces have no framework dependencies✅ Input/Output Decoupling - Primitive types only, no domain entities leaked✅ Immutability - All Input/Output models are immutable
records

All 65 architecture tests pass successfully!