# PRD: Article Data - Pricing & Inventory Contexts

## Overview

Separate pricing and inventory concerns from Product context into dedicated bounded contexts. Implement resolver pattern for fresh article data in Cart and Checkout.

**Architecture:** [Article Data Resolver Pattern](../docs/architecture/article-data-resolver-pattern.md)

---

## Epic 1: Pricing Bounded Context

### US-52: Pricing Domain Value Objects
**As a** developer
**I want** pricing domain value objects created
**So that** the pricing context has proper domain building blocks

**Acceptance Criteria:**
- PriceId value object with generate() and of() methods
- Implements Value interface with proper validation
- All in `pricing/domain/model/` package

**Architectural Guidance:**
- **Location:** `pricing/domain/model/PriceId.java`
- **Pattern:** Value Object as Java record

---

### US-53: ProductPrice Aggregate
**As a** developer
**I want** a ProductPrice aggregate root
**So that** product prices can be managed with proper invariants

**Acceptance Criteria:**
- Extends BaseAggregateRoot<ProductPrice, PriceId>
- Fields: id, productId, currentPrice (Money), effectiveFrom (Instant)
- Factory method: create(productId, price)
- Method: updatePrice(newPrice) raises PriceChanged event
- Validates price > 0

**Architectural Guidance:**
- **Location:** `pricing/domain/model/ProductPrice.java`
- **Pattern:** Aggregate Root

---

### US-54: Pricing Domain Events
**As a** developer
**I want** pricing domain events
**So that** other contexts can react to price changes

**Acceptance Criteria:**
- PriceCreated event (priceId, productId, price, effectiveFrom)
- PriceChanged event (priceId, productId, oldPrice, newPrice, effectiveFrom)
- All implement DomainEvent interface with eventId, occurredOn, version

**Architectural Guidance:**
- **Location:** `pricing/domain/event/`
- **Pattern:** Domain Event as immutable record

---

### US-55: ProductPriceRepository Interface
**As a** developer
**I want** a ProductPriceRepository interface
**So that** product prices can be persisted and retrieved

**Acceptance Criteria:**
- Extends Repository<ProductPrice, PriceId>
- Methods: findByProductId(ProductId), findByProductIds(Collection<ProductId>)
- Interface in application/shared

**Architectural Guidance:**
- **Location:** `pricing/application/shared/ProductPriceRepository.java`
- **Pattern:** Repository Interface

---

### US-56: InMemoryProductPriceRepository
**As a** developer
**I want** an in-memory repository implementation
**So that** the pricing context works without database

**Acceptance Criteria:**
- Implements ProductPriceRepository
- ConcurrentHashMap-based storage
- Secondary index on ProductId for efficient lookups
- Thread-safe operations

**Architectural Guidance:**
- **Location:** `pricing/adapter/outgoing/persistence/InMemoryProductPriceRepository.java`
- **Pattern:** In-Memory Repository

---

### US-57: GetPricesForProducts Use Case
**As a** developer
**I want** a bulk price lookup use case
**So that** Cart/Checkout can fetch prices efficiently

**Acceptance Criteria:**
- GetPricesForProductsInputPort extends UseCase
- GetPricesForProductsQuery(Collection<ProductId> productIds)
- GetPricesForProductsResult with Map<ProductId, PriceData>
- PriceData record: productId, currentPrice, effectiveFrom

**Architectural Guidance:**
- **Location:** `pricing/application/getpricesforproducts/`
- **Pattern:** Use Case with Query/Result

---

### US-58: PricingService Open Host Service
**As a** developer
**I want** a PricingService OHS
**So that** other contexts can access pricing through a stable API

**Acceptance Criteria:**
- @OpenHostService annotation
- record PriceInfo(ProductId, Money currentPrice, Instant effectiveFrom)
- getPrices(Collection<ProductId>) returns Map<ProductId, PriceInfo>
- getPrice(ProductId) returns Optional<PriceInfo>
- Delegates to GetPricesForProductsUseCase

**Architectural Guidance:**
- **Location:** `pricing/adapter/incoming/openhost/PricingService.java`
- **Pattern:** Open Host Service

---

### US-59: SetProductPrice Use Case
**As a** developer
**I want** a use case to set/update product prices
**So that** prices can be managed

**Acceptance Criteria:**
- SetProductPriceInputPort extends UseCase
- SetProductPriceCommand(productId, priceAmount, priceCurrency)
- Creates new ProductPrice if not exists, updates if exists
- Publishes domain events

**Architectural Guidance:**
- **Location:** `pricing/application/setproductprice/`
- **Pattern:** Use Case with Command/Result

---

### US-60: Pricing Context ArchUnit Rules
**As a** developer
**I want** architecture tests for pricing context
**So that** architectural patterns are enforced

**Acceptance Criteria:**
- Domain has no Spring annotations
- Repository interface in application/shared
- OHS in adapter/incoming/openhost
- All ArchUnit tests pass

**Architectural Guidance:**
- **Location:** Existing ArchUnit tests should cover new context
- **Pattern:** Architecture Testing

---

## Epic 2: Inventory Bounded Context

### US-61: Inventory Domain Value Objects
**As a** developer
**I want** inventory domain value objects
**So that** the inventory context has proper domain building blocks

**Acceptance Criteria:**
- StockLevelId value object with generate() and of()
- StockQuantity value object (int value, cannot be negative)
- Implements Value interface

**Architectural Guidance:**
- **Location:** `inventory/domain/model/`
- **Pattern:** Value Object as Java record

---

### US-62: StockLevel Aggregate
**As a** developer
**I want** a StockLevel aggregate root
**So that** product inventory can be managed with proper invariants

**Acceptance Criteria:**
- Extends BaseAggregateRoot<StockLevel, StockLevelId>
- Fields: id, productId, availableQuantity (StockQuantity), reservedQuantity
- Factory method: create(productId, initialQuantity)
- Methods: increaseStock(int), decreaseStock(int), reserve(int), release(int)
- isAvailable() returns availableQuantity > reservedQuantity
- Raises StockChanged events

**Architectural Guidance:**
- **Location:** `inventory/domain/model/StockLevel.java`
- **Pattern:** Aggregate Root

---

### US-63: Inventory Domain Events
**As a** developer
**I want** inventory domain events
**So that** other contexts can react to stock changes

**Acceptance Criteria:**
- StockLevelCreated event (stockLevelId, productId, quantity)
- StockIncreased event (stockLevelId, productId, addedQuantity, newQuantity)
- StockDecreased event (stockLevelId, productId, removedQuantity, newQuantity)
- StockReserved event (stockLevelId, productId, reservedQuantity)

**Architectural Guidance:**
- **Location:** `inventory/domain/event/`
- **Pattern:** Domain Event as immutable record

---

### US-64: StockLevelRepository Interface
**As a** developer
**I want** a StockLevelRepository interface
**So that** stock levels can be persisted and retrieved

**Acceptance Criteria:**
- Extends Repository<StockLevel, StockLevelId>
- Methods: findByProductId(ProductId), findByProductIds(Collection<ProductId>)
- Interface in application/shared

**Architectural Guidance:**
- **Location:** `inventory/application/shared/StockLevelRepository.java`
- **Pattern:** Repository Interface

---

### US-65: InMemoryStockLevelRepository
**As a** developer
**I want** an in-memory repository implementation
**So that** inventory context works without database

**Acceptance Criteria:**
- Implements StockLevelRepository
- ConcurrentHashMap-based storage
- Secondary index on ProductId
- Thread-safe operations

**Architectural Guidance:**
- **Location:** `inventory/adapter/outgoing/persistence/InMemoryStockLevelRepository.java`
- **Pattern:** In-Memory Repository

---

### US-66: GetStockForProducts Use Case
**As a** developer
**I want** a bulk stock lookup use case
**So that** Cart/Checkout can check availability efficiently

**Acceptance Criteria:**
- GetStockForProductsInputPort extends UseCase
- GetStockForProductsQuery(Collection<ProductId> productIds)
- GetStockForProductsResult with Map<ProductId, StockData>
- StockData record: productId, availableStock, isAvailable

**Architectural Guidance:**
- **Location:** `inventory/application/getstockforproducts/`
- **Pattern:** Use Case with Query/Result

---

### US-67: InventoryService Open Host Service
**As a** developer
**I want** an InventoryService OHS
**So that** other contexts can access stock through a stable API

**Acceptance Criteria:**
- @OpenHostService annotation
- record StockInfo(ProductId, int availableStock, boolean isAvailable)
- getStock(Collection<ProductId>) returns Map<ProductId, StockInfo>
- hasStock(ProductId, int quantity) returns boolean
- Delegates to GetStockForProductsUseCase

**Architectural Guidance:**
- **Location:** `inventory/adapter/incoming/openhost/InventoryService.java`
- **Pattern:** Open Host Service

---

### US-68: SetStockLevel Use Case
**As a** developer
**I want** a use case to set/update stock levels
**So that** inventory can be managed

**Acceptance Criteria:**
- SetStockLevelInputPort extends UseCase
- SetStockLevelCommand(productId, quantity)
- Creates new StockLevel if not exists, updates if exists
- Publishes domain events

**Architectural Guidance:**
- **Location:** `inventory/application/setstocklevel/`
- **Pattern:** Use Case with Command/Result

---

### US-69: Inventory Context ArchUnit Rules
**As a** developer
**I want** architecture tests for inventory context
**So that** architectural patterns are enforced

**Acceptance Criteria:**
- Domain has no Spring annotations
- Repository interface in application/shared
- OHS in adapter/incoming/openhost
- All ArchUnit tests pass

**Architectural Guidance:**
- Existing ArchUnit tests should cover new context

---

## Epic 3: Cart Resolver Pattern

### US-70: ArticlePriceResolver Interface
**As a** developer
**I want** an ArticlePriceResolver functional interface
**So that** cart domain can receive fresh pricing without external dependencies

**Acceptance Criteria:**
- @FunctionalInterface annotation
- ArticlePrice resolve(ProductId productId) method
- record ArticlePrice(Money price, boolean isAvailable, int availableStock) implements Value
- In cart domain package

**Architectural Guidance:**
- **Location:** `cart/domain/model/ArticlePriceResolver.java`
- **Pattern:** Functional Interface, Value Object

---

### US-71: CartValidationResult Value Object
**As a** developer
**I want** a CartValidationResult value object
**So that** validation errors can be collected and returned

**Acceptance Criteria:**
- record CartValidationResult(List<ValidationError> errors)
- isValid() returns errors.isEmpty()
- record ValidationError(ProductId productId, String message, ErrorType type)
- enum ErrorType { PRODUCT_UNAVAILABLE, INSUFFICIENT_STOCK }
- Static factory methods for each error type

**Architectural Guidance:**
- **Location:** `cart/domain/model/CartValidationResult.java`
- **Pattern:** Value Object

---

### US-72: ShoppingCart Resolver Methods
**As a** developer
**I want** resolver-based methods on ShoppingCart aggregate
**So that** calculations use fresh pricing data

**Acceptance Criteria:**
- calculateTotal(ArticlePriceResolver) returns Money
- validateForCheckout(ArticlePriceResolver) returns CartValidationResult
- Keep existing calculateTotal() as @Deprecated for migration
- Both methods iterate items and use resolver for pricing

**Architectural Guidance:**
- **Location:** `cart/domain/model/ShoppingCart.java`
- **Pattern:** Aggregate method with injected dependency

---

### US-73: ArticleDataPort Output Port
**As a** developer
**I want** an ArticleDataPort output port
**So that** cart use cases can fetch article data

**Acceptance Criteria:**
- Extends OutputPort interface
- record ArticleData(ProductId, String name, Money currentPrice, int availableStock, boolean isAvailable)
- getArticleData(Collection<ProductId>) returns Map<ProductId, ArticleData>
- getArticleData(ProductId) returns Optional<ArticleData>

**Architectural Guidance:**
- **Location:** `cart/application/shared/ArticleDataPort.java`
- **Pattern:** Output Port

---

### US-74: CompositeArticleDataAdapter
**As a** developer
**I want** a composite adapter that aggregates data from multiple OHS
**So that** article data comes from the right sources

**Acceptance Criteria:**
- Implements ArticleDataPort
- Injects: ProductCatalogService, PricingService, InventoryService
- Fetches from all three, combines into ArticleData
- Handles missing data gracefully

**Architectural Guidance:**
- **Location:** `cart/adapter/outgoing/article/CompositeArticleDataAdapter.java`
- **Pattern:** Composite Adapter

---

### US-75: Update CheckoutCartUseCase with Resolver
**As a** developer
**I want** CheckoutCartUseCase to use resolver pattern
**So that** checkout validates with fresh data

**Acceptance Criteria:**
- Inject ArticleDataPort
- Fetch article data for all cart items
- Build resolver from fetched data
- Call cart.checkout(resolver) instead of cart.checkout()
- Handle validation errors appropriately

**Architectural Guidance:**
- **Location:** `cart/application/checkoutcart/CheckoutCartUseCase.java`
- **Pattern:** Use Case orchestration

---

### US-76: Update GetCartByIdUseCase with Fresh Data
**As a** developer
**I want** GetCartByIdUseCase to enrich results with fresh pricing
**So that** displayed prices are always current

**Acceptance Criteria:**
- Inject ArticleDataPort
- Fetch article data for all cart items
- Include currentPrice and availability in result
- Add priceChanged flag if currentPrice != priceAtAddition

**Architectural Guidance:**
- **Location:** `cart/application/getcartbyid/GetCartByIdUseCase.java`
- **Pattern:** Query enrichment

---

## Epic 4: Checkout Resolver Pattern

### US-77: CheckoutArticlePriceResolver Interface
**As a** developer
**I want** a CheckoutArticlePriceResolver functional interface
**So that** checkout domain can receive fresh pricing

**Acceptance Criteria:**
- @FunctionalInterface annotation
- ArticlePrice resolve(ProductId productId) method
- record ArticlePrice(Money price, boolean isAvailable, int availableStock) implements Value
- In checkout domain package

**Architectural Guidance:**
- **Location:** `checkout/domain/model/CheckoutArticlePriceResolver.java`
- **Pattern:** Functional Interface

---

### US-78: CheckoutValidationResult Value Object
**As a** developer
**I want** a CheckoutValidationResult value object
**So that** checkout validation errors can be collected

**Acceptance Criteria:**
- record CheckoutValidationResult(List<ValidationError> errors)
- isValid() returns errors.isEmpty()
- Similar structure to CartValidationResult

**Architectural Guidance:**
- **Location:** `checkout/domain/model/CheckoutValidationResult.java`
- **Pattern:** Value Object

---

### US-79: CheckoutSession Resolver Methods
**As a** developer
**I want** resolver-based methods on CheckoutSession aggregate
**So that** order totals use fresh pricing

**Acceptance Criteria:**
- calculateOrderTotal(CheckoutArticlePriceResolver) returns Money
- validateItems(CheckoutArticlePriceResolver) returns CheckoutValidationResult
- Update confirm() to accept resolver and validate before confirming

**Architectural Guidance:**
- **Location:** `checkout/domain/model/CheckoutSession.java`
- **Pattern:** Aggregate method with injected dependency

---

### US-80: CheckoutArticleDataPort Output Port
**As a** developer
**I want** a CheckoutArticleDataPort output port
**So that** checkout use cases can fetch article data

**Acceptance Criteria:**
- Extends OutputPort interface
- record ArticleData(ProductId, String name, Money currentPrice, int availableStock, boolean isAvailable)
- getArticleData(Collection<ProductId>) returns Map<ProductId, ArticleData>

**Architectural Guidance:**
- **Location:** `checkout/application/shared/CheckoutArticleDataPort.java`
- **Pattern:** Output Port

---

### US-81: CompositeCheckoutArticleDataAdapter
**As a** developer
**I want** a composite adapter for checkout
**So that** checkout gets article data from the right sources

**Acceptance Criteria:**
- Implements CheckoutArticleDataPort
- Injects: ProductCatalogService, PricingService, InventoryService
- Same pattern as Cart's CompositeArticleDataAdapter

**Architectural Guidance:**
- **Location:** `checkout/adapter/outgoing/article/CompositeCheckoutArticleDataAdapter.java`
- **Pattern:** Composite Adapter

---

### US-82: Update StartCheckoutUseCase with Resolver
**As a** developer
**I want** StartCheckoutUseCase to use resolver pattern
**So that** checkout starts with fresh pricing

**Acceptance Criteria:**
- Inject CheckoutArticleDataPort
- Fetch article data for all line items
- Build resolver from fetched data
- Pass resolver to CheckoutSession methods

**Architectural Guidance:**
- **Location:** `checkout/application/startcheckout/StartCheckoutUseCase.java`
- **Pattern:** Use Case orchestration

---

### US-83: Update ConfirmCheckoutUseCase with Resolver
**As a** developer
**I want** ConfirmCheckoutUseCase to validate with fresh data
**So that** final confirmation uses current pricing/availability

**Acceptance Criteria:**
- Inject CheckoutArticleDataPort
- Fetch fresh article data before confirmation
- Build resolver and call session.confirm(resolver)
- Handle validation errors

**Architectural Guidance:**
- **Location:** `checkout/application/confirmcheckout/ConfirmCheckoutUseCase.java`
- **Pattern:** Use Case orchestration

---

## Epic 5: Data Migration & Cleanup

### US-84: Initialize Pricing Data from Products
**As a** developer
**I want** pricing data initialized from existing products
**So that** pricing context has data on startup

**Acceptance Criteria:**
- AsyncInitialize method in InMemoryProductPriceRepository
- Load all products and create ProductPrice for each
- Copy price from Product to Pricing context

**Architectural Guidance:**
- **Location:** `pricing/adapter/outgoing/persistence/InMemoryProductPriceRepository.java`
- **Pattern:** Data initialization

---

### US-85: Initialize Inventory Data from Products
**As a** developer
**I want** inventory data initialized from existing products
**So that** inventory context has data on startup

**Acceptance Criteria:**
- AsyncInitialize method in InMemoryStockLevelRepository
- Load all products and create StockLevel for each
- Copy stock quantity from Product to Inventory context

**Architectural Guidance:**
- **Location:** `inventory/adapter/outgoing/persistence/InMemoryStockLevelRepository.java`
- **Pattern:** Data initialization

---

### US-86: Update Product Context - Remove Pricing Responsibility
**As a** developer
**I want** Product context to no longer manage pricing
**So that** separation of concerns is complete

**Acceptance Criteria:**
- Deprecate price-related methods in Product aggregate
- ProductCatalogService OHS returns name/SKU only (not price)
- Update ProductDataPort in Cart to not expect pricing
- Keep Price in Product for now (remove in future story)

**Architectural Guidance:**
- **Location:** `product/domain/model/Product.java`, `product/adapter/incoming/openhost/ProductCatalogService.java`
- **Pattern:** Gradual migration

---

### US-87: Integration Tests for Article Data Flow
**As a** developer
**I want** integration tests for the full article data flow
**So that** the resolver pattern works correctly end-to-end

**Acceptance Criteria:**
- Test CompositeArticleDataAdapter with all three OHS
- Test CheckoutCartUseCase with resolver
- Test GetCartByIdUseCase enrichment
- Test price change detection

**Architectural Guidance:**
- **Location:** `src/test/java/.../cart/application/`
- **Pattern:** Integration Testing

---

### US-88: Remove Deprecated Price from Product Context
**As a** developer
**I want** to remove all deprecated price-related code from Product context
**So that** the bounded context separation is complete and clean

**Acceptance Criteria:**
- Remove price field from Product aggregate
- Remove deprecated price() method from Product
- Remove deprecated changePrice() method from Product
- Remove ProductPriceChanged domain event from Product context
- Remove deprecated ProductInfoWithPrice record from ProductCatalogService
- Remove deprecated getAllProductsWithInitialPrice() method from ProductCatalogService
- Remove transition fallbacks from CompositeArticleDataAdapter
- Remove transition fallbacks from CompositeCheckoutArticleDataAdapter
- Update CreateProductUseCase to not set price
- Update UpdateProductPriceUseCase to use Pricing context or remove
- All tests pass after removal

**Architectural Guidance:**
- **Location:** `product/domain/model/Product.java`, `product/adapter/incoming/openhost/ProductCatalogService.java`, `cart/adapter/outgoing/product/CompositeArticleDataAdapter.java`, `checkout/adapter/outgoing/product/CompositeCheckoutArticleDataAdapter.java`
- **Pattern:** Migration cleanup, Bounded context separation

---

## Goals
- Fresh pricing/availability for all cart/checkout calculations
- Proper bounded context separation (Pricing, Inventory)
- Domain purity maintained via resolver pattern
- Backward compatibility during migration

## Non-Goals
- External pricing service integration (mock/internal only)
- External inventory service integration (mock/internal only)
- Price history tracking (future enhancement)
- Stock reservations during checkout (future enhancement)