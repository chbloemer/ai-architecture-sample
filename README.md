# AI Architecture Sample Project

A comprehensive demonstration of **Domain-Driven Design (DDD)**, **Clean Architecture**, **Hexagonal Architecture**, and **Onion Architecture** patterns using an e-commerce domain, with **MCP (Model Context Protocol)** server integration for AI assistant interaction.

## Overview

This project showcases best practices for structuring a Spring Boot application with clean architecture principles. It implements seven bounded contexts:
- **Product Catalog** - Product management with enriched views (pricing + stock from other contexts)
- **Shopping Cart** - Customer shopping cart management with article price resolution
- **Checkout** - Multi-step checkout flow with session management
- **Account** - User registration and authentication
- **Portal** - Application home page and navigation
- **Inventory** - Stock level management (Open Host Service)
- **Pricing** - Product pricing management (Open Host Service)

### Key Features

- **AI-Accessible Product Catalog** via MCP server (Spring AI 1.1.0-M3)
- **Complete Architecture Testing** with ArchUnit (10 test suites)
- **17 Architecture Decision Records** documenting design choices
- **Shared Kernel** pattern for cross-context value objects
- **Framework-Independent Domain** layer (no Spring/JPA in core)
- **Multi-step Checkout Flow** with 5 steps and session management
- **Specification Pattern with Visitor** for database-agnostic cart filtering
- **Multiple Persistence Strategies** (InMemory, JPA, JDBC) for shopping cart

## Architecture Patterns

### Domain-Driven Design (DDD)

**Strategic Patterns:**
- **Bounded Contexts**: Product Catalog, Shopping Cart, Checkout, Account, Portal
- **Shared Kernel**: Cross-context value objects (Money, Price, ProductId, UserId)
- **Context Mapping**: Contexts communicate via shared kernel and domain events
- **Open Host Service**: ProductCatalogService provides cross-context API

**Tactical Patterns:**
- **Aggregates**: Product, ShoppingCart, CheckoutSession, Account, StockLevel, ProductPrice
- **Entities**: CartItem, CheckoutLineItem
- **Value Objects**: ProductId, SKU, Price, Money, Quantity, Category, BuyerInfo, DeliveryAddress, Email, HashedPassword, etc.
- **Repositories**: Interfaces in application layer, implementations in adapters
- **Domain Services**: PricingService, CartTotalCalculator, CheckoutStepValidator, PasswordHasher
- **Domain Events**: ProductCreated, CartCheckedOut, CartItemAddedToCart, CartItemQuantityChanged, ProductRemovedFromCart, CartCleared, CheckoutSessionStarted, CheckoutConfirmed, AccountRegistered, PriceChanged, StockChanged, etc.
- **Factories**: ProductFactory, EnrichedCartFactory, CheckoutCartFactory
- **Specifications**: ProductAvailabilitySpecification, CartSpecification (with Visitor pattern: ActiveCart, HasMinTotal, HasAnyAvailableItem, LastUpdatedBefore, CustomerAllowsMarketing)

### Clean Architecture

- **Use Cases** (Input Ports): Explicit use case interfaces with single responsibility
- **Input/Output Models**: Commands, Queries, and Result objects decouple layers
- **Framework Independence**: Domain layer is framework-agnostic; application layer uses minimal framework annotations pragmatically
- **Dependency Rule**: Dependencies point inward (Infrastructure → Adapters → Application → Domain)
- **Use Case Organization**: One use case per operation (CreateProductUseCase, AddItemToCartUseCase, StartCheckoutUseCase, etc.)

### Hexagonal Architecture (Ports and Adapters)

- **Input Ports**: Use case interfaces (defined in application layer)
- **Output Ports**: Repository and service interfaces (defined in sharedkernel.application.port)
- **Incoming Adapters** (Primary): REST Controllers, MCP Server, Web MVC, Event Consumers, Open Host Services
- **Outgoing Adapters** (Secondary): In-memory, JPA, and JDBC repository implementations

### Onion Architecture

Layers (from innermost to outermost):
1. **Domain Model** (per bounded context) - Pure business logic, framework-independent
2. **Application Services** (per bounded context) - Use case orchestration
3. **Adapters** (per bounded context) - External interfaces
4. **Shared Kernel** - Cross-context shared concepts
5. **Infrastructure** - Cross-cutting concerns

## Project Structure

```
src/main/java/de/sample/aiarchitecture/
├── sharedkernel/                         # Shared Kernel (cross-context)
│   ├── marker/                           # Architectural markers
│   │   ├── tactical/                     # DDD tactical patterns
│   │   │   ├── Id.java                   # Identity marker
│   │   │   ├── Entity.java
│   │   │   ├── Value.java
│   │   │   ├── AggregateRoot.java
│   │   │   ├── BaseAggregateRoot.java
│   │   │   ├── DomainEvent.java
│   │   │   ├── IntegrationEvent.java
│   │   │   ├── DomainService.java
│   │   │   ├── Factory.java
│   │   │   └── Specification.java
│   │   ├── strategic/                    # DDD strategic patterns
│   │   │   ├── BoundedContext.java
│   │   │   ├── SharedKernel.java
│   │   │   └── OpenHostService.java
│   │   ├── port/                         # Hexagonal Architecture ports
│   │   │   ├── in/                       # Input ports (driving)
│   │   │   │   ├── InputPort.java
│   │   │   │   └── UseCase.java
│   │   │   └── out/                      # Output ports (driven)
│   │   │       ├── OutputPort.java
│   │   │       ├── Repository.java
│   │   │       ├── DomainEventPublisher.java
│   │   │       └── IdentityProvider.java
│   │   └── infrastructure/               # Framework integration markers
│   │       └── AsyncInitialize.java
│   ├── domain/
│   │   ├── model/                        # Shared value objects
│   │   │   ├── ProductId.java            # Cross-context ID
│   │   │   ├── UserId.java               # Cross-context ID
│   │   │   ├── Money.java                # Cross-context value
│   │   │   └── Price.java                # Cross-context value
│   │   └── specification/                # Composable specification pattern
│   │       ├── CompositeSpecification.java
│   │       ├── AndSpecification.java
│   │       ├── OrSpecification.java
│   │       ├── NotSpecification.java
│   │       └── SpecificationVisitor.java
│   └── adapter/
│       └── outgoing/
│           └── event/
│               └── SpringDomainEventPublisher.java  # Domain event publishing
│
├── product/                              # Product Catalog bounded context
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── Product.java              # Aggregate Root
│   │   │   ├── SKU.java                  # Value Objects
│   │   │   ├── ProductName.java
│   │   │   ├── ProductDescription.java
│   │   │   ├── ProductArticle.java
│   │   │   ├── ProductStock.java
│   │   │   ├── EnrichedProduct.java       # Enriched read model
│   │   │   ├── Category.java
│   │   │   └── ProductFactory.java       # Factory
│   │   ├── specification/                # Specifications
│   │   │   └── ProductAvailabilitySpecification.java
│   │   ├── service/                      # Domain services
│   │   │   └── PricingService.java
│   │   └── event/                        # Domain events
│   │       └── ProductCreated.java
│   ├── application/                      # Application layer
│   │   ├── createproduct/                # Use case: Create Product
│   │   │   ├── CreateProductInputPort.java
│   │   │   ├── CreateProductUseCase.java
│   │   │   ├── CreateProductCommand.java
│   │   │   └── CreateProductResult.java
│   │   ├── getallproducts/               # Use case: Get All Products
│   │   │   ├── GetAllProductsInputPort.java
│   │   │   ├── GetAllProductsUseCase.java
│   │   │   ├── GetAllProductsQuery.java
│   │   │   └── GetAllProductsResult.java
│   │   ├── getproductbyid/               # Use case: Get Product By ID
│   │   │   ├── GetProductByIdInputPort.java
│   │   │   ├── GetProductByIdUseCase.java
│   │   │   ├── GetProductByIdQuery.java
│   │   │   └── GetProductByIdResult.java
│   │   ├── reduceproductstock/           # Use case: Reduce Product Stock
│   │   │   ├── ReduceProductStockInputPort.java
│   │   │   ├── ReduceProductStockUseCase.java
│   │   │   ├── ReduceProductStockCommand.java
│   │   │   └── ReduceProductStockResult.java
│   │   └── shared/                       # Shared output ports
│   │       ├── ProductRepository.java
│   │       ├── PricingDataPort.java       # Port for pricing data from Pricing context
│   │       └── ProductStockDataPort.java  # Port for stock data from Inventory context
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters (primary)
│       │   ├── api/
│       │   │   ├── ProductResource.java
│       │   │   ├── CreateProductRequest.java
│       │   │   ├── ProductDto.java
│       │   │   └── ProductDtoConverter.java
│       │   ├── mcp/
│       │   │   └── ProductCatalogMcpToolProvider.java
│       │   ├── web/
│       │   │   ├── ProductPageController.java
│       │   │   ├── ProductCatalogPageViewModel.java
│       │   │   └── ProductDetailPageViewModel.java
│       │   ├── openhost/
│       │   │   └── ProductCatalogService.java    # Open Host Service
│       │   └── event/
│       │       ├── ProductEventConsumer.java
│       │       ├── CheckoutConfirmedEventListener.java
│       │       └── acl/
│       │           └── CheckoutEventTranslator.java  # Anti-corruption layer
│       └── outgoing/                     # Outgoing adapters (secondary)
│           ├── persistence/
│           │   └── InMemoryProductRepository.java
│           ├── pricing/
│           │   └── PricingDataAdapter.java    # Adapter to Pricing context
│           └── inventory/
│               └── InventoryStockDataAdapter.java  # Adapter to Inventory context
│
├── cart/                                 # Shopping Cart bounded context
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── ShoppingCart.java         # Aggregate Root
│   │   │   ├── CartItem.java             # Entity
│   │   │   ├── CartId.java               # Value Objects
│   │   │   ├── CartItemId.java
│   │   │   ├── CustomerId.java
│   │   │   ├── Quantity.java
│   │   │   ├── CartStatus.java
│   │   │   ├── ArticlePrice.java         # Price from Pricing context
│   │   │   ├── ArticlePriceResolver.java # Resolver for fresh prices
│   │   │   ├── CartArticle.java          # Article data for cart items
│   │   │   ├── EnrichedCart.java          # Enriched read model
│   │   │   ├── EnrichedCartFactory.java   # Factory for enriched carts
│   │   │   ├── EnrichedCartItem.java      # Enriched cart item with prices
│   │   │   └── CartValidationResult.java  # Validation result
│   │   ├── specification/                # Cart specifications (Visitor pattern)
│   │   │   ├── CartSpecification.java    # Base specification interface
│   │   │   ├── CartSpecificationVisitor.java  # Visitor for database-agnostic filtering
│   │   │   ├── ComposedCartSpecification.java
│   │   │   ├── ActiveCart.java
│   │   │   ├── HasMinTotal.java
│   │   │   ├── HasAnyAvailableItem.java
│   │   │   ├── LastUpdatedBefore.java
│   │   │   └── CustomerAllowsMarketing.java
│   │   ├── service/                      # Domain services
│   │   │   └── CartTotalCalculator.java
│   │   └── event/                        # Domain events
│   │       ├── CartCheckedOut.java
│   │       ├── CartItemAddedToCart.java
│   │       ├── CartItemQuantityChanged.java
│   │       ├── ProductRemovedFromCart.java
│   │       └── CartCleared.java
│   ├── application/                      # Application layer
│   │   ├── createcart/                   # Use case: Create Cart
│   │   │   ├── CreateCartInputPort.java
│   │   │   ├── CreateCartUseCase.java
│   │   │   ├── CreateCartCommand.java
│   │   │   └── CreateCartResult.java
│   │   ├── additemtocart/                # Use case: Add Item to Cart
│   │   │   ├── AddItemToCartInputPort.java
│   │   │   ├── AddItemToCartUseCase.java
│   │   │   ├── AddItemToCartCommand.java
│   │   │   └── AddItemToCartResult.java
│   │   ├── checkoutcart/                 # Use case: Checkout Cart
│   │   │   ├── CheckoutCartInputPort.java
│   │   │   ├── CheckoutCartUseCase.java
│   │   │   ├── CheckoutCartCommand.java
│   │   │   └── CheckoutCartResult.java
│   │   ├── getallcarts/                  # Use case: Get All Carts
│   │   │   ├── GetAllCartsInputPort.java
│   │   │   ├── GetAllCartsUseCase.java
│   │   │   ├── GetAllCartsQuery.java
│   │   │   └── GetAllCartsResult.java
│   │   ├── getcartbyid/                  # Use case: Get Cart By ID
│   │   │   ├── GetCartByIdInputPort.java
│   │   │   ├── GetCartByIdUseCase.java
│   │   │   ├── GetCartByIdQuery.java
│   │   │   └── GetCartByIdResult.java
│   │   ├── getorcreateactivecart/        # Use case: Get or Create Active Cart
│   │   │   ├── GetOrCreateActiveCartInputPort.java
│   │   │   ├── GetOrCreateActiveCartUseCase.java
│   │   │   ├── GetOrCreateActiveCartCommand.java
│   │   │   └── GetOrCreateActiveCartResult.java
│   │   ├── removeitemfromcart/           # Use case: Remove Item from Cart
│   │   │   ├── RemoveItemFromCartInputPort.java
│   │   │   ├── RemoveItemFromCartUseCase.java
│   │   │   ├── RemoveItemFromCartCommand.java
│   │   │   └── RemoveItemFromCartResult.java
│   │   ├── mergecarts/                   # Use case: Merge Carts
│   │   │   ├── MergeCartsInputPort.java
│   │   │   ├── MergeCartsUseCase.java
│   │   │   ├── MergeCartsCommand.java
│   │   │   └── MergeCartsResult.java
│   │   ├── getcartmergeoptions/          # Use case: Get Cart Merge Options
│   │   │   ├── GetCartMergeOptionsInputPort.java
│   │   │   ├── GetCartMergeOptionsUseCase.java
│   │   │   ├── GetCartMergeOptionsQuery.java
│   │   │   └── GetCartMergeOptionsResult.java
│   │   ├── completecart/                 # Use case: Complete Cart (after checkout)
│   │   │   ├── CompleteCartInputPort.java
│   │   │   ├── CompleteCartUseCase.java
│   │   │   ├── CompleteCartCommand.java
│   │   │   └── CompleteCartResult.java
│   │   ├── recovercart/                  # Use case: Recover Abandoned Cart
│   │   │   ├── RecoverCartInputPort.java
│   │   │   ├── RecoverCartUseCase.java
│   │   │   ├── RecoverCartCommand.java
│   │   │   └── RecoverCartOnLoginResult.java
│   │   └── shared/                       # Shared output ports
│   │       ├── ShoppingCartRepository.java
│   │       ├── ArticleDataPort.java       # Port for article data (prices + stock)
│   │       └── ProductDataPort.java       # Port for product data from other context
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   ├── api/
│       │   │   ├── ShoppingCartResource.java
│       │   │   ├── AddToCartRequest.java
│       │   │   ├── ShoppingCartDto.java
│       │   │   ├── ShoppingCartListDto.java
│       │   │   ├── CartItemDto.java
│       │   │   └── ShoppingCartDtoConverter.java
│       │   ├── web/
│       │   │   ├── CartPageController.java
│       │   │   ├── CartPageViewModel.java
│       │   │   ├── CartMergePageController.java
│       │   │   └── CartMergePageViewModel.java
│       │   └── event/
│       │       ├── CartEventConsumer.java
│       │       └── CheckoutEventConsumer.java
│       └── outgoing/                     # Outgoing adapters
│           ├── product/
│           │   └── CompositeArticleDataAdapter.java  # Composite adapter for article data
│           └── persistence/
│               ├── InMemoryShoppingCartRepository.java
│               ├── JdbcShoppingCartRepository.java
│               └── jpa/                  # JPA persistence alternative
│                   ├── JpaShoppingCartRepository.java
│                   ├── CartJpaRepository.java
│                   ├── CartEntity.java
│                   ├── CartItemEntity.java
│                   └── CartSpecToJpa.java    # Specification visitor for JPA
│               └── jdbc/
│                   └── CartSpecToJdbc.java   # Specification visitor for JDBC
│
├── checkout/                             # Checkout bounded context
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── CheckoutSession.java      # Aggregate Root
│   │   │   ├── CheckoutLineItem.java     # Entity
│   │   │   ├── CheckoutSessionId.java    # Value Objects
│   │   │   ├── CheckoutLineItemId.java
│   │   │   ├── CheckoutStep.java         # Enum: BUYER_INFO, DELIVERY, PAYMENT, REVIEW, CONFIRMATION
│   │   │   ├── CheckoutSessionStatus.java
│   │   │   ├── CheckoutTotals.java
│   │   │   ├── CheckoutValidationResult.java
│   │   │   ├── BuyerInfo.java
│   │   │   ├── DeliveryAddress.java
│   │   │   ├── ShippingOption.java
│   │   │   ├── PaymentSelection.java
│   │   │   ├── PaymentProviderId.java
│   │   │   ├── CustomerId.java
│   │   │   ├── CartId.java
│   │   │   ├── CheckoutArticle.java       # Article data for checkout
│   │   │   ├── CheckoutArticlePriceResolver.java  # Price resolver
│   │   │   ├── CheckoutCart.java           # Cart snapshot for checkout
│   │   │   ├── CheckoutCartFactory.java    # Factory for checkout cart
│   │   │   └── EnrichedCheckoutLineItem.java  # Enriched line item with prices
│   │   ├── service/                      # Domain services
│   │   │   └── CheckoutStepValidator.java
│   │   └── event/                        # Domain events
│   │       ├── CheckoutSessionStarted.java
│   │       ├── BuyerInfoSubmitted.java
│   │       ├── DeliverySubmitted.java
│   │       ├── PaymentSubmitted.java
│   │       ├── CheckoutConfirmed.java
│   │       ├── CheckoutCompleted.java
│   │       ├── CheckoutAbandoned.java
│   │       └── CheckoutExpired.java
│   ├── application/                      # Application layer
│   │   ├── startcheckout/                # Use case: Start Checkout
│   │   │   ├── StartCheckoutInputPort.java
│   │   │   ├── StartCheckoutUseCase.java
│   │   │   ├── StartCheckoutCommand.java
│   │   │   └── StartCheckoutResult.java
│   │   ├── submitbuyerinfo/              # Use case: Submit Buyer Info
│   │   │   ├── SubmitBuyerInfoInputPort.java
│   │   │   ├── SubmitBuyerInfoUseCase.java
│   │   │   ├── SubmitBuyerInfoCommand.java
│   │   │   └── SubmitBuyerInfoResult.java
│   │   ├── submitdelivery/               # Use case: Submit Delivery
│   │   │   ├── SubmitDeliveryInputPort.java
│   │   │   ├── SubmitDeliveryUseCase.java
│   │   │   ├── SubmitDeliveryCommand.java
│   │   │   └── SubmitDeliveryResult.java
│   │   ├── submitpayment/                # Use case: Submit Payment
│   │   │   ├── SubmitPaymentInputPort.java
│   │   │   ├── SubmitPaymentUseCase.java
│   │   │   ├── SubmitPaymentCommand.java
│   │   │   └── SubmitPaymentResult.java
│   │   ├── confirmcheckout/              # Use case: Confirm Checkout
│   │   │   ├── ConfirmCheckoutInputPort.java
│   │   │   ├── ConfirmCheckoutUseCase.java
│   │   │   ├── ConfirmCheckoutCommand.java
│   │   │   └── ConfirmCheckoutResult.java
│   │   ├── getcheckoutsession/           # Use case: Get Checkout Session
│   │   │   ├── GetCheckoutSessionInputPort.java
│   │   │   ├── GetCheckoutSessionUseCase.java
│   │   │   ├── GetCheckoutSessionQuery.java
│   │   │   └── GetCheckoutSessionResult.java
│   │   ├── getactivecheckoutsession/     # Use case: Get Active Checkout Session
│   │   │   ├── GetActiveCheckoutSessionInputPort.java
│   │   │   ├── GetActiveCheckoutSessionUseCase.java
│   │   │   ├── GetActiveCheckoutSessionQuery.java
│   │   │   └── GetActiveCheckoutSessionResult.java
│   │   ├── getconfirmedcheckoutsession/  # Use case: Get Confirmed Checkout Session
│   │   │   ├── GetConfirmedCheckoutSessionInputPort.java
│   │   │   ├── GetConfirmedCheckoutSessionUseCase.java
│   │   │   ├── GetConfirmedCheckoutSessionQuery.java
│   │   │   └── GetConfirmedCheckoutSessionResult.java
│   │   ├── getshippingoptions/           # Use case: Get Shipping Options
│   │   │   ├── GetShippingOptionsInputPort.java
│   │   │   ├── GetShippingOptionsUseCase.java
│   │   │   ├── GetShippingOptionsQuery.java
│   │   │   └── GetShippingOptionsResult.java
│   │   ├── getpaymentproviders/          # Use case: Get Payment Providers
│   │   │   ├── GetPaymentProvidersInputPort.java
│   │   │   ├── GetPaymentProvidersUseCase.java
│   │   │   ├── GetPaymentProvidersQuery.java
│   │   │   └── GetPaymentProvidersResult.java
│   │   ├── synccheckoutwithcart/         # Use case: Sync Checkout with Cart
│   │   │   ├── SyncCheckoutWithCartInputPort.java
│   │   │   ├── SyncCheckoutWithCartUseCase.java
│   │   │   ├── SyncCheckoutWithCartCommand.java
│   │   │   └── SyncCheckoutWithCartResult.java
│   │   └── shared/                       # Shared output ports
│   │       ├── CheckoutSessionRepository.java
│   │       ├── CartDataPort.java
│   │       ├── CartData.java
│   │       ├── CheckoutArticleDataPort.java  # Port for article data (prices + stock)
│   │       ├── ProductInfoPort.java
│   │       ├── PaymentProvider.java
│   │       └── PaymentProviderRegistry.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   ├── web/
│       │   │   ├── StartCheckoutPageController.java
│       │   │   ├── BuyerInfoPageController.java
│       │   │   ├── BuyerInfoPageViewModel.java
│       │   │   ├── DeliveryPageController.java
│       │   │   ├── DeliveryPageViewModel.java
│       │   │   ├── PaymentPageController.java
│       │   │   ├── PaymentPageViewModel.java
│       │   │   ├── ReviewPageController.java
│       │   │   ├── ReviewPageViewModel.java
│       │   │   ├── ConfirmationPageController.java
│       │   │   └── ConfirmationPageViewModel.java
│       │   └── event/
│       │       └── CartChangeEventConsumer.java
│       └── outgoing/                     # Outgoing adapters
│           ├── persistence/
│           │   └── InMemoryCheckoutSessionRepository.java
│           ├── cart/
│           │   └── CartDataAdapter.java
│           ├── product/
│           │   ├── CompositeCheckoutArticleDataAdapter.java  # Composite adapter
│           │   └── ProductInfoAdapter.java
│           └── payment/
│               ├── MockPaymentProvider.java
│               └── InMemoryPaymentProviderRegistry.java
│
├── account/                              # Account bounded context
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── Account.java              # Aggregate Root
│   │   │   ├── AccountId.java            # Value Objects
│   │   │   ├── Email.java
│   │   │   ├── HashedPassword.java
│   │   │   └── AccountStatus.java
│   │   ├── service/                      # Domain services
│   │   │   └── PasswordHasher.java       # Interface for password hashing
│   │   └── event/                        # Domain events
│   │       ├── AccountRegistered.java
│   │       └── AccountLinkedToIdentity.java
│   ├── application/                      # Application layer
│   │   ├── registeraccount/              # Use case: Register Account
│   │   │   ├── RegisterAccountInputPort.java
│   │   │   ├── RegisterAccountUseCase.java
│   │   │   ├── RegisterAccountCommand.java
│   │   │   └── RegisterAccountResult.java
│   │   ├── authenticateaccount/          # Use case: Authenticate Account
│   │   │   ├── AuthenticateAccountInputPort.java
│   │   │   ├── AuthenticateAccountUseCase.java
│   │   │   ├── AuthenticateAccountCommand.java
│   │   │   └── AuthenticateAccountResult.java
│   │   └── shared/                       # Shared output ports
│   │       ├── AccountRepository.java
│   │       ├── RegisteredUserValidator.java
│   │       ├── TokenService.java
│   │       └── IdentitySession.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   ├── api/
│       │   │   ├── AuthResource.java
│       │   │   ├── LoginRequest.java
│       │   │   ├── LoginApiResult.java
│       │   │   ├── RegisterRequest.java
│       │   │   └── RegisterApiResult.java
│       │   └── web/
│       │       ├── LoginPageController.java
│       │       └── RegisterPageController.java
│       └── outgoing/                     # Outgoing adapters
│           ├── persistence/
│           │   └── InMemoryAccountRepository.java
│           └── security/
│               ├── SpringSecurityPasswordHasher.java
│               └── AccountBasedRegisteredUserValidator.java
│
├── inventory/                            # Inventory bounded context
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── StockLevel.java           # Aggregate Root
│   │   │   ├── StockLevelId.java         # Value Objects
│   │   │   └── StockQuantity.java
│   │   └── event/                        # Domain events
│   │       ├── StockLevelCreated.java
│   │       ├── StockChanged.java
│   │       ├── StockIncreased.java
│   │       ├── StockDecreased.java
│   │       └── StockReserved.java
│   ├── application/                      # Application layer
│   │   ├── setstocklevel/                # Use case: Set Stock Level
│   │   │   ├── SetStockLevelInputPort.java
│   │   │   ├── SetStockLevelUseCase.java
│   │   │   ├── SetStockLevelCommand.java
│   │   │   └── SetStockLevelResult.java
│   │   ├── reducestock/                  # Use case: Reduce Stock
│   │   │   ├── ReduceStockInputPort.java
│   │   │   ├── ReduceStockUseCase.java
│   │   │   ├── ReduceStockCommand.java
│   │   │   └── ReduceStockResult.java
│   │   ├── getstockforproducts/          # Use case: Get Stock for Products
│   │   │   ├── GetStockForProductsInputPort.java
│   │   │   ├── GetStockForProductsUseCase.java
│   │   │   ├── GetStockForProductsQuery.java
│   │   │   └── GetStockForProductsResult.java
│   │   └── shared/                       # Shared output ports
│   │       └── StockLevelRepository.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   ├── openhost/
│       │   │   └── InventoryService.java     # Open Host Service
│       │   └── event/
│       │       └── CheckoutConfirmedEventConsumer.java
│       └── outgoing/                     # Outgoing adapters
│           └── persistence/
│               └── InMemoryStockLevelRepository.java
│
├── pricing/                              # Pricing bounded context
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── ProductPrice.java         # Aggregate Root
│   │   │   └── PriceId.java             # Value Objects
│   │   └── event/                        # Domain events
│   │       ├── PriceCreated.java
│   │       └── PriceChanged.java
│   ├── application/                      # Application layer
│   │   ├── setproductprice/              # Use case: Set Product Price
│   │   │   ├── SetProductPriceInputPort.java
│   │   │   ├── SetProductPriceUseCase.java
│   │   │   ├── SetProductPriceCommand.java
│   │   │   └── SetProductPriceResult.java
│   │   ├── getpricesforproducts/         # Use case: Get Prices for Products
│   │   │   ├── GetPricesForProductsInputPort.java
│   │   │   ├── GetPricesForProductsUseCase.java
│   │   │   ├── GetPricesForProductsQuery.java
│   │   │   └── GetPricesForProductsResult.java
│   │   └── shared/                       # Shared output ports
│   │       └── ProductPriceRepository.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   ├── openhost/
│       │   │   └── PricingService.java       # Open Host Service
│       │   └── event/
│       │       └── ProductCreatedEventConsumer.java
│       └── outgoing/                     # Outgoing adapters
│           └── persistence/
│               └── InMemoryProductPriceRepository.java
│
├── portal/                               # Portal bounded context
│   └── adapter/                          # Adapters
│       └── incoming/                     # Incoming adapters
│           └── web/
│               └── HomePageController.java
│
└── infrastructure/                       # Infrastructure (cross-cutting)
    ├── AiArchitectureApplication.java    # Spring Boot main class
    ├── config/                           # Spring @Configuration classes
    │   ├── SecurityConfiguration.java
    │   ├── TransactionConfiguration.java
    │   ├── AsyncConfiguration.java
    │   ├── DomainConfiguration.java
    │   └── Pug4jConfiguration.java
    ├── init/                             # Initialization
    │   └── SampleDataInitializer.java    # Sample data seeding
    ├── support/                          # Framework support components
    │   └── AsyncInitializationProcessor.java
    └── security/                         # Security infrastructure
        ├── SpringSecurityIdentityProvider.java
        ├── JwtIdentity.java
        ├── JwtIdentityType.java
        └── jwt/                          # JWT authentication
            ├── JwtTokenService.java
            ├── JwtIdentitySession.java
            ├── JwtProperties.java
            └── JwtAuthenticationFilter.java
```

## Getting Started

### Prerequisites
- Java 21
- Gradle 9.1 or higher

### Running the Application

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

## API Documentation

### Product API

#### Get All Products
```bash
curl http://localhost:8080/api/products
```

#### Get Product by ID
```bash
curl http://localhost:8080/api/products/{productId}
```

#### Get Product by SKU
```bash
curl http://localhost:8080/api/products/sku/LAPTOP-001
```

#### Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "TEST-001",
    "name": "Test Product",
    "description": "A test product",
    "price": 99.99,
    "category": "Electronics",
    "stock": 10
  }'
```

#### Delete Product
```bash
curl -X DELETE http://localhost:8080/api/products/{productId}
```

### Shopping Cart API

#### Create Cart
```bash
curl -X POST "http://localhost:8080/api/carts?customerId=customer-123"
```

#### Get Active Cart for Customer
```bash
curl http://localhost:8080/api/carts/customer/customer-123/active
```

#### Get Cart by ID
```bash
curl http://localhost:8080/api/carts/{cartId}
```

#### Add Item to Cart
```bash
curl -X POST http://localhost:8080/api/carts/{cartId}/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "{productId}",
    "quantity": 2
  }'
```

#### Remove Item from Cart
```bash
curl -X DELETE http://localhost:8080/api/carts/{cartId}/items/{itemId}
```

#### Checkout Cart
```bash
curl -X POST http://localhost:8080/api/carts/{cartId}/checkout
```

#### Delete Cart
```bash
curl -X DELETE http://localhost:8080/api/carts/{cartId}
```

### MCP (Model Context Protocol) API

The product catalog is accessible via MCP server for AI assistants like Claude. The server runs on `http://localhost:8080/mcp` using **streamable HTTP** (HTTP + Server-Sent Events).

#### Available MCP Tools

**all-products**
- Returns all products in the catalog with complete details
- No parameters required

**product-by-sku**
- Find product by SKU (e.g., "LAPTOP-001")
- Parameters: `sku` (String) - must contain uppercase letters, numbers, hyphens only

**product-by-category**
- Find all products in a category
- Parameters: `categoryName` (String)
- Valid categories: Electronics, Clothing, Books, Home & Garden, Sports & Outdoors

**product-by-id**
- Get product by internal UUID
- Parameters: `id` (String) - UUID format

#### Connecting AI Assistants

Create `.mcp.json` in the project root:

```json
{
  "mcpServers": {
    "product-catalog": {
      "type": "http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

AI assistants will automatically discover and connect to the MCP server.

**See:** [docs/integrations/mcp-server-integration.md](docs/integrations/mcp-server-integration.md) for detailed MCP server documentation.

## Sample Data

The application initializes with 11 sample products across different categories:
- **Electronics**: Laptop, Smartphone, Tablet
- **Clothing**: T-Shirt, Jeans
- **Books**: DDD Book, Clean Architecture Book
- **Home & Garden**: Office Chair, Standing Desk
- **Sports**: Yoga Mat, Dumbbells

## Architecture Documentation

For comprehensive architecture documentation, see:
- **[docs/architecture/](docs/architecture/)** - Complete architecture documentation
- **[docs/architecture/architecture-principles.md](docs/architecture/architecture-principles.md)** - Detailed patterns and principles
- **[CLAUDE.md](CLAUDE.md)** - Development guidelines and best practices

### Quick Reference

**Domain Layer** (per bounded context) - Framework-independent business logic
- No Spring/JPA annotations in domain models
- All business rules in domain objects
- Organized into: domain.model, domain.service, domain.event, domain.specification
- Dependencies point inward toward domain

**Application Layer** (per bounded context) - Use case orchestration
- Thin coordination layer (no business logic)
- Manages transactions and domain event publishing
- Defines ports: Input ports (use cases) and output ports (repositories)
- One use case class per operation

**Adapter Layer** (per bounded context) - External interfaces
- Incoming Adapters (Primary):
  - `adapter.incoming.api` - REST APIs (@RestController) returning JSON via DTOs
  - `adapter.incoming.web` - Web MVC (@Controller) returning HTML via page-specific ViewModels
  - `adapter.incoming.mcp` - MCP Server for AI assistants
  - `adapter.incoming.openhost` - Open Host Services for cross-context APIs
  - `adapter.incoming.event` - Domain event consumers
  - `adapter.incoming.event.acl` - Anti-corruption layer event translators
- Outgoing Adapters (Secondary):
  - `adapter.outgoing.persistence` - Repository implementations (InMemory, JPA, JDBC)
  - `adapter.outgoing.cart`, `adapter.outgoing.product` - Cross-context data adapters
  - `adapter.outgoing.payment` - Payment provider adapters
  - `adapter.outgoing.security` - Security-related adapters
- DTO/ViewModel conversion happens in adapters (not application layer)
- ViewModels use primitives only, created from domain read models
- Adapters don't communicate directly

**Shared Kernel** - Cross-context shared concepts
- `sharedkernel.marker.tactical` - DDD tactical patterns (Entity, Value, AggregateRoot, DomainEvent, etc.)
- `sharedkernel.marker.strategic` - DDD strategic patterns (BoundedContext, SharedKernel, OpenHostService)
- `sharedkernel.marker.port.in` - Input ports (UseCase, InputPort)
- `sharedkernel.marker.port.out` - Output ports (Repository, DomainEventPublisher, IdentityProvider)
- `sharedkernel.marker.infrastructure` - Framework integration markers (AsyncInitialize)
- `sharedkernel.domain.model` - Shared value objects (Money, Price, ProductId, UserId)
- `sharedkernel.domain.specification` - Composable specification pattern
- `sharedkernel.adapter.outgoing.event` - Shared outgoing adapters (SpringDomainEventPublisher)

**Infrastructure Layer** - Cross-cutting concerns
- `infrastructure.config` - Spring @Configuration classes
- `infrastructure.support` - Framework support components (processors, listeners)
- `infrastructure.security` - Security infrastructure (JWT, authentication, identity)

## Testing

### Architecture Tests

Architecture rules are **actively enforced** using ArchUnit with 10 comprehensive test suites:

```bash
./gradlew test-architecture
```

**Test Suites:**
- **DddTacticalPatternsArchUnitTest** - Aggregate, Entity, Value Object, Repository patterns
- **DddAdvancedPatternsArchUnitTest** - Domain Events, Services, Factories, Specifications
- **DddStrategicPatternsArchUnitTest** - Bounded Context isolation
- **HexagonalArchitectureArchUnitTest** - Ports and Adapters rules
- **OnionArchitectureArchUnitTest** - Dependency flow rules
- **LayeredArchitectureArchUnitTest** - Layer access rules
- **NamingConventionsArchUnitTest** - Naming standards
- **PackageCyclesArchUnitTest** - Circular dependency detection
- **UseCasePatternsArchUnitTest** - Application service patterns
- **BaseArchUnitTest** - Common test infrastructure

These tests verify:
- Domain layer has no framework dependencies
- Aggregates only reference other aggregates by ID
- Value objects are immutable records
- Repository interfaces live in domain
- No circular package dependencies
- Naming conventions are followed
- Bounded contexts are properly isolated

### Unit Tests
```bash
./gradlew test
```

## Key Design Decisions

1. **In-Memory Storage**: Uses ConcurrentHashMap for simplicity; production would use JPA/database
2. **Security**: Permits all requests for demo purposes
3. **Price Snapshot**: Cart items store price at time of addition (common e-commerce pattern)
4. **Package-Private Constructors**: CartItem can only be created through ShoppingCart
5. **Immutable Value Objects**: All value objects are Java records
6. **MCP as Primary Adapter**: MCP server implemented as incoming adapter, maintaining clean architecture
7. **Read-Only MCP Tools**: AI access limited to queries for safety
8. **Shared Kernel**: Cross-context value objects shared between bounded contexts
9. **Multi-step Checkout Flow**: 5 steps (Buyer Info → Delivery → Payment → Review → Confirmation) with session management
10. **Specification Pattern with Visitor**: Database-agnostic filtering via CartSpecification and CartSpecificationVisitor
11. **Multiple Persistence Strategies**: InMemory (default), JPA, and JDBC implementations for shopping cart
12. **OpenHost Service Pattern**: ProductCatalogService provides cross-context API for checkout
13. **UserId Continuity on Registration**: When a user registers, their anonymous UserId is preserved

**Architecture Decision Records:** See [docs/architecture/adr/README.md](docs/architecture/adr/README.md) for 17 documented architectural decisions.

## Business Rules Demonstrated

### Product Aggregate
- Price must be positive
- Stock cannot be negative
- SKU must be unique

### Shopping Cart Aggregate
- Cannot modify checked-out or completed cart
- Cannot checkout empty cart
- Each product appears once (quantities combined)
- Cart stores price snapshot at time of addition

### Checkout Session Aggregate
- Cannot skip steps - must complete in order (Buyer Info → Delivery → Payment → Review → Confirmation)
- Can go back to modify previous steps (before confirmation)
- Cannot modify confirmed, completed, or expired sessions
- Must have at least one line item to start checkout

### Account Aggregate
- Email must be unique across all accounts
- Password is hashed before storage (never stored in plain text)
- Account status transitions: PENDING → ACTIVE → SUSPENDED/DELETED

## Further Reading

### Project Documentation
- **[Architecture Documentation](docs/architecture/)** - Complete architecture guide
- **[Architecture Principles](docs/architecture/architecture-principles.md)** - Detailed DDD, Hexagonal, and Onion patterns
- **[CLAUDE.md](CLAUDE.md)** - Development guidelines and documentation standards

### External Resources
- **[Domain-Driven Design](https://www.domainlanguage.com/ddd/)** by Eric Evans
- **[Implementing Domain-Driven Design](https://vaughnvernon.com/)** by Vaughn Vernon
- **[Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)** by Alistair Cockburn
- **[Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)** by Robert C. Martin
- **[ArchUnit](https://www.archunit.org/)** - Architecture testing framework

## License

This is a sample project for educational purposes.
