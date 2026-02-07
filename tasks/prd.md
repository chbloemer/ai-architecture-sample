# PRD: AI Architecture Sample - E-Commerce Application

## Overview

Reference implementation demonstrating Domain-Centric Architecture with DDD, Hexagonal Architecture, and Clean Architecture patterns.

---

### US-1: Initial Project Setup & Spring Boot Application

**Description:** Set up Spring Boot project with Gradle, Pug template engine, and basic web infrastructure for the e-commerce sample application.

**Acceptance Criteria:**
- Spring Boot 3.x project with Java 21
- Gradle build configuration with required dependencies
- Pug4j template engine integration
- Basic layout template with navigation
- Home page controller and template

---

### US-2: ArchUnit Architecture Tests

**Description:** Set up ArchUnit test infrastructure to enforce architectural rules automatically.

**Acceptance Criteria:**
- Separate test-architecture source set with Spock/Groovy
- Tests for hexagonal architecture (ports and adapters)
- Tests for onion architecture (layer dependencies)
- Tests for DDD tactical patterns (aggregates, entities, value objects)
- Tests for naming conventions

---

### US-3: Product Catalog Bounded Context

**Description:** Implement Product Catalog bounded context with domain model, repository, and use cases for browsing products.

**Acceptance Criteria:**
- Product aggregate with ProductId, Name, Description, Price, Availability
- ProductRepository interface in application layer
- InMemoryProductRepository implementation
- GetAllProductsUseCase for catalog listing
- GetProductByIdUseCase for product details
- CatalogPageController and ProductDetailPageController
- Pug templates for catalog and product detail pages

---

### US-4: Use Case Pattern Implementation

**Description:** Implement use case pattern from Clean Architecture with InputPort/UseCase interfaces and Command/Query objects.

**Acceptance Criteria:**
- UseCase<INPUT, OUTPUT> base interface
- InputPort marker interface
- Use cases organized by bounded context
- Command objects for write operations
- Query objects for read operations

---

### US-5: Shared Kernel & DDD Marker Interfaces

**Description:** Create shared kernel with DDD marker interfaces and common domain primitives used across bounded contexts.

**Acceptance Criteria:**
- AggregateRoot, Entity, Value marker interfaces
- DomainEvent and DomainService markers
- Repository and OutputPort base interfaces
- BaseAggregateRoot with event collection
- Shared value objects (CustomerId, Money)
- All in sharedkernel package

---

### US-6: Domain-Centric Architecture Refactoring

**Description:** Refactor package structure to follow domain-centric architecture with clear layer separation.

**Acceptance Criteria:**
- Package structure: domain, application, adapter, infrastructure
- Adapters split into incoming (controllers) and outgoing (repositories)
- Spring application class in infrastructure layer
- Naming conventions documented and applied
- Event listeners moved to adapter layer

---

### US-7: Shopping Cart Bounded Context

**Description:** Implement Shopping Cart bounded context with cart aggregate and add-to-cart functionality.

**Acceptance Criteria:**
- ShoppingCart aggregate with CartId, CustomerId, line items
- CartLineItem entity with product reference and quantity
- AddToCartUseCase for adding products
- CartRepository interface and implementation
- CartPageController for viewing cart
- Cart view template

---

### US-8: Cross-Context Integration Events

**Description:** Implement event-driven communication between bounded contexts using domain and integration events.

**Acceptance Criteria:**
- DomainEvent interface for internal events
- IntegrationEvent for cross-context communication
- Event publishing infrastructure with Spring events
- Event consumers in adapter.incoming.event package
- ProductAddedToCart example integration

---

### US-9: In-Memory Repository Implementation

**Description:** Implement thread-safe in-memory repositories using ConcurrentHashMap for development and testing.

**Acceptance Criteria:**
- ConcurrentHashMap-based storage
- Thread-safe operations
- Secondary indexes for efficient lookups
- InMemoryProductRepository implementation
- InMemoryCartRepository implementation

---

### US-10: Specification Pattern

**Description:** Implement Specification pattern for encapsulating business rules and query criteria.

**Acceptance Criteria:**
- Specification<T> interface with isSatisfiedBy method
- Composite specifications (and, or, not)
- Example specification implementations
- Repository support for specification queries

---

### US-11: Domain Value Objects

**Description:** Create checkout domain value objects (CheckoutSessionId, CheckoutStep, BuyerInfo, DeliveryAddress, ShippingOption, PaymentProviderId, PaymentSelection, CheckoutTotals, CheckoutLineItem)

**Acceptance Criteria:**
- CheckoutSessionId value object with generate() and of() methods
- CheckoutStep enum with all states
- BuyerInfo, DeliveryAddress, ShippingOption value objects
- PaymentProviderId, PaymentSelection value objects
- CheckoutTotals, CheckoutLineItemId, CheckoutLineItem value objects
- All implement Value interface with proper validation

---

### US-12: CheckoutSession Aggregate

**Description:** Create CheckoutSession aggregate root with state management and invariants

**Acceptance Criteria:**
- Extends AggregateRoot<CheckoutSession, CheckoutSessionId>
- Factory method: start(cartId, customerId, lineItems, subtotal)
- Methods: submitBuyerInfo(), submitDelivery(), submitPayment(), confirm(), complete(), abandon(), expire()
- Step validation: cannot skip steps, can go back
- Raises domain events for each step

---

### US-13: Domain Events

**Description:** Create checkout domain events for state changes

**Acceptance Criteria:**
- CheckoutStarted event
- BuyerInfoSubmitted event
- DeliverySubmitted event
- PaymentSubmitted event
- CheckoutConfirmed integration event

---

### US-14: Repository and Persistence

**Description:** Create checkout session repository interface and in-memory implementation

**Acceptance Criteria:**
- CheckoutSessionRepository interface in application/shared
- Methods: save(), findById(), findByCartId(), findExpiredSessions()
- InMemoryCheckoutSessionRepository implementation

---

### US-15: Start Checkout Use Case

**Description:** Use case to begin checkout process from cart

**Acceptance Criteria:**
- StartCheckoutInputPort extends UseCase
- StartCheckoutCommand and StartCheckoutResponse
- StartCheckoutUseCase implementation
- Loads cart, creates session, saves session

---

### US-16: Submit Buyer Info Use Case

**Description:** Use case for customer contact information

**Acceptance Criteria:**
- SubmitBuyerInfoInputPort extends UseCase
- SubmitBuyerInfoCommand and SubmitBuyerInfoResponse
- Validates session and step
- Updates session with buyer info

---

### US-17: Submit Delivery Use Case

**Description:** Use case for delivery address and shipping option

**Acceptance Criteria:**
- SubmitDeliveryInputPort extends UseCase
- SubmitDeliveryCommand and SubmitDeliveryResponse
- Validates session and step
- Updates session with delivery info

---

### US-18: Get Shipping Options Use Case

**Description:** Use case to retrieve available shipping methods

**Acceptance Criteria:**
- GetShippingOptionsInputPort extends UseCase
- GetShippingOptionsQuery and GetShippingOptionsResponse
- Returns hardcoded shipping options

---

### US-19: Payment Provider System

**Description:** Plugin architecture for payment providers

**Acceptance Criteria:**
- PaymentProvider output port interface
- PaymentProviderRegistry output port interface
- MockPaymentProvider adapter
- InMemoryPaymentProviderRegistry adapter

---

### US-20: Submit Payment Use Case

**Description:** Use case for payment selection and authorization

**Acceptance Criteria:**
- SubmitPaymentInputPort extends UseCase
- SubmitPaymentCommand and SubmitPaymentResponse
- Validates provider exists
- Updates session with payment

---

### US-21: Get Payment Providers Use Case

**Description:** Use case to retrieve available payment methods

**Acceptance Criteria:**
- GetPaymentProvidersInputPort extends UseCase
- GetPaymentProvidersQuery and GetPaymentProvidersResponse
- Uses PaymentProviderRegistry

---

### US-22: Get Checkout Session Use Case

**Description:** Use case to load session data for display

**Acceptance Criteria:**
- GetCheckoutSessionInputPort extends UseCase
- GetCheckoutSessionQuery and GetCheckoutSessionResponse
- Returns all session data or found=false

---

### US-23: Confirm Checkout Use Case

**Description:** Use case to complete the order

**Acceptance Criteria:**
- ConfirmCheckoutInputPort extends UseCase
- ConfirmCheckoutCommand and ConfirmCheckoutResponse
- Validates all required info
- Publishes CheckoutConfirmed event

---

### US-24: Checkout Step Validator

**Description:** Component to enforce step navigation rules

**Acceptance Criteria:**
- CheckoutStepValidator component
- validateStepAccess returns redirect URL or empty
- Handles invalid session, skip ahead, go back, terminal states

---

### US-25: Start Checkout Controller

**Description:** Controller to initiate checkout from cart

**Acceptance Criteria:**
- StartCheckoutPageController at /checkout/start
- GET with cartId parameter
- Redirects to /checkout/{id}/buyer

---

### US-26: Buyer Info Controller

**Description:** Controller for contact information page

**Acceptance Criteria:**
- BuyerInfoPageController at /checkout/{id}/buyer
- GET returns buyer.pug template
- POST redirects to delivery

---

### US-27: Delivery Controller

**Description:** Controller for shipping address page

**Acceptance Criteria:**
- DeliveryPageController at /checkout/{id}/delivery
- GET returns delivery.pug template
- POST redirects to payment

---

### US-28: Payment Controller

**Description:** Controller for payment method page

**Acceptance Criteria:**
- PaymentPageController at /checkout/{id}/payment
- GET returns payment.pug template
- POST redirects to review

---

### US-29: Review Controller

**Description:** Controller for order review page

**Acceptance Criteria:**
- ReviewPageController at /checkout/{id}/review
- GET returns review.pug with all order details
- Edit links to previous steps

---

### US-30: Confirmation Controller

**Description:** Controller for order confirmation

**Acceptance Criteria:**
- POST /checkout/{id}/confirm calls ConfirmCheckoutUseCase
- GET /checkout/{id}/confirmation shows thank you page
- Only accessible after confirmation

---

### US-31: Checkout Templates

**Description:** Pug templates for checkout pages

**Acceptance Criteria:**
- buyer.pug, delivery.pug, payment.pug templates
- review.pug with order summary
- confirmation.pug thank you page
- All extend layout with step progress

---

### US-32: Cart Integration

**Description:** Integrate checkout with cart context

**Acceptance Criteria:**
- Checkout button on cart page
- CheckoutEventConsumer listens for CheckoutConfirmed
- Calls CheckoutCartUseCase to update cart status

---

### US-33: Architecture Tests Pass

**Description:** Verify checkout follows project patterns

**Acceptance Criteria:**
- ./gradlew test-architecture passes
- No direct imports from Cart domain
- Domain layer has no Spring annotations
- All use cases follow InputPort pattern

---

### US-34: Build and Manual Test

**Description:** Verify complete checkout flow works end-to-end

**Acceptance Criteria:**
- ./gradlew build passes
- Application starts successfully
- Complete flow: Buyer → Delivery → Payment → Review → Confirm
- Cart status is CHECKED_OUT after completion

---

### US-35: Connect Cart Checkout Button to Checkout Flow

**Description:** Replace the fake checkout button on the cart page with a link to the new checkout bounded context. The cart page should redirect to the checkout flow instead of the old fake checkout endpoint.

**Acceptance Criteria:**
- Cart page checkout button redirects to /checkout/start?cartId={cartId} instead of /cart/{cartId}/checkout
- Old /cart/{cartId}/checkout endpoint is removed or deprecated
- Checkout flow can be started from the cart page
- Architecture tests pass

---

### US-36: Reduce Product Availability on Checkout

**Description:** When a checkout is confirmed, the product availability must be reduced by the quantities purchased. The product context listens for the CheckoutConfirmed integration event and reduces availability for each line item.

**Acceptance Criteria:**
- Product context has a CheckoutConfirmedEventListener that listens for CheckoutConfirmed events
- For each line item in the checkout, product availability is reduced by the purchased quantity
- Uses existing Product aggregate's reduceAvailability() method
- Architecture tests pass (no direct coupling between Checkout and Product domains)

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `product.adapter.incoming.event.CheckoutConfirmedEventListener`
- **Patterns:** Event Consumer, Cross-Context Integration
- **Constraints:**
  - Event listener in product.adapter.incoming.event package
  - No direct imports from checkout.domain - only checkout integration events
  - Call ProductRepository via application layer or directly (simple case)
  - Use @EventListener or @TransactionalEventListener(phase = AFTER_COMMIT)
  - Run ./gradlew test-architecture to verify

---

### US-37: Remove Deprecated CartCheckedOut Availability Logic

**Description:** The CartCheckedOut event was previously used to reduce product availability. Since US-36 now handles this via CheckoutConfirmed event, the old CartCheckedOut event consumer and related logic in the product context should be removed.

**Acceptance Criteria:**
- CartCheckedOutEventListener in product context is removed (if exists)
- Any CartCheckedOut event handling for availability reduction is removed
- CartCheckedOut event itself remains if still used for cart status updates
- Build and architecture tests pass
- Product availability is only reduced via CheckoutConfirmed (US-36)

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `product.adapter.incoming.event.CartCheckedOutEventListener (DELETE)`
- **Patterns:** Code Removal, Dead Code Cleanup
- **Constraints:**
  - Search for CartCheckedOut references in product context
  - Verify event is not used elsewhere before removing listener
  - Keep CartCheckedOut event if cart context still publishes it for other purposes
  - Run ./gradlew test-architecture after removal
  - Run ./gradlew build to ensure no compilation errors

---

### US-38: Handle Missing Account After Application Restart

**Description:** After application restart with in-memory storage, JWT tokens remain valid but accounts are lost. Registered users should be treated as anonymous when their account no longer exists.

**Acceptance Criteria:**
- RegisteredUserValidator port interface in sharedkernel/application/port/security
- AccountBasedRegisteredUserValidator implementation in account/adapter/outgoing/security
- JwtAuthenticationFilter checks if registered user's account exists
- If account doesn't exist, creates new anonymous identity and token
- User sees logged-out state and can register/login again
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** sharedkernel, adapter, infrastructure
- **Locations:**
  - `sharedkernel.application.port.security.RegisteredUserValidator`
  - `account.adapter.outgoing.security.AccountBasedRegisteredUserValidator`
  - `infrastructure.security.jwt.JwtAuthenticationFilter`
- **Patterns:** Port/Adapter, Cross-Context Integration via Shared Kernel
- **Constraints:**
  - Port interface in shared kernel maintains bounded context isolation
  - Infrastructure depends on port, not account context directly
  - Account context provides implementation

---

### US-39: E2E Test Infrastructure with Playwright

**Description:** Set up end-to-end testing infrastructure using Playwright for Java to test critical user flows through the browser.

**Acceptance Criteria:**
- Playwright Java dependency added to build.gradle
- test-e2e source set configured separately from unit tests
- BaseE2ETest class with Playwright setup/teardown
- IntelliJ run configurations for E2E tests (headless and headed)
- E2E tests can be run via ./gradlew test-e2e

**Architectural Guidance:**
- **Affected Layers:** test-e2e
- **Locations:**
  - `src/test-e2e/java/de/sample/aiarchitecture/e2e/BaseE2ETest.java`
- **Patterns:** Test Infrastructure, Browser Automation

---

### US-40: Data-Test Attributes for E2E Testing

**Description:** Add data-test attributes to UI elements to provide stable selectors for E2E tests, decoupling tests from CSS classes and DOM structure.

**Acceptance Criteria:**
- data-test attributes added to interactive elements (buttons, links, forms)
- data-test attributes added to key display elements (prices, totals, messages)
- Naming convention: kebab-case descriptive names (e.g., data-test="checkout-button")
- ADR documenting the data-test attribute strategy
- Templates updated: login, register, cart, catalog, product detail, checkout pages

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `src/main/resources/templates/**/*.pug`
  - `docs/architecture/adr/adr-017-e2e-data-test-attributes.md`
- **Patterns:** Test Attribute Strategy, Stable Selectors
- **Constraints:**
  - Never use data-test attributes for styling
  - data-test attributes are for testing infrastructure only
  - Keep attribute names semantic and descriptive

---

### US-41: Page Object Pattern for E2E Tests

**Description:** Implement Page Object pattern to encapsulate page interactions and selectors, improving test maintainability and readability.

**Acceptance Criteria:**
- Page object classes for each major page (LoginPage, RegisterPage, CartPage, etc.)
- Page objects use data-test attributes for element selection
- Page objects provide high-level methods for user actions
- ADR documenting the Page Object pattern
- E2E tests refactored to use page objects

**Architectural Guidance:**
- **Affected Layers:** test-e2e
- **Locations:**
  - `src/test-e2e/java/de/sample/aiarchitecture/e2e/pages/`
  - `docs/architecture/adr/adr-018-page-object-pattern-e2e.md`
- **Patterns:** Page Object Pattern, Test Abstraction
- **Constraints:**
  - Page objects should not contain assertions
  - Page objects return page objects for fluent navigation
  - Selectors centralized in page objects, not in tests

---

### US-42: JWT Authentication Infrastructure

**Description:** Implement JWT-based authentication infrastructure with anonymous and registered user support. Anonymous users get a soft identity via JWT cookie, which converts to a hard identity on registration/login.

**Acceptance Criteria:**
- JwtTokenService for generating and validating JWT tokens
- JwtAuthenticationFilter extracts identity from cookie or creates anonymous identity
- Identity record with userId, type (ANONYMOUS/REGISTERED), email, and roles
- IdentityProvider port for accessing current identity in use cases
- TokenService port interface in shared kernel
- JWT cookie with HttpOnly, SameSite=Lax settings
- Anonymous tokens valid 30 days, registered tokens valid 7 days

**Architectural Guidance:**
- **Affected Layers:** sharedkernel, infrastructure
- **Locations:**
  - `sharedkernel.application.port.security.TokenService`
  - `sharedkernel.application.port.security.IdentityProvider`
  - `sharedkernel.application.common.security.Identity`
  - `infrastructure.security.jwt.JwtTokenService`
  - `infrastructure.security.jwt.JwtAuthenticationFilter`
  - `infrastructure.security.SpringSecurityIdentityProvider`
- **Patterns:** Port/Adapter, Security Filter Chain

---

### US-43: Account Bounded Context with Registration and Login

**Description:** Create Account bounded context with user registration and authentication capabilities. Accounts are linked to UserId for cart continuity.

**Acceptance Criteria:**
- Account aggregate with AccountId, Email, HashedPassword, linkedUserId
- RegisterAccountUseCase for new user registration
- AuthenticateAccountUseCase for login validation
- AccountRepository interface and InMemoryAccountRepository
- PasswordHasher port with BCrypt implementation
- AuthResource REST controller for API authentication
- LoginPageController and RegisterPageController for web UI
- Login and register Pug templates

**Architectural Guidance:**
- **Affected Layers:** domain, application, adapter
- **Locations:**
  - `account.domain.model.Account`
  - `account.application.registeraccount.RegisterAccountUseCase`
  - `account.application.authenticateaccount.AuthenticateAccountUseCase`
  - `account.adapter.incoming.api.AuthResource`
  - `account.adapter.incoming.web.LoginPageController`
  - `account.adapter.incoming.web.RegisterPageController`
- **Patterns:** Aggregate, Use Case Pattern, Port/Adapter

---

### US-44: Migrate Controllers to JWT Identity

**Description:** Migrate Cart and Checkout controllers from session-based customer ID to JWT-based identity. Controllers use IdentityProvider to get current user instead of URL parameters.

**Acceptance Criteria:**
- CartPageController uses IdentityProvider.getCurrentIdentity()
- All Checkout controllers use IdentityProvider for user identification
- Session IDs removed from checkout URLs (use /checkout/{sessionId}/step pattern)
- CustomerId derived from Identity.userId()
- Cart is automatically associated with current identity

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `cart.adapter.incoming.web.CartPageController`
  - `checkout.adapter.incoming.web.*PageController`
- **Patterns:** Identity Provider, Security Context

---

### US-45: Recover Cart on Login

**Description:** When an anonymous user logs in, their cart should be recovered and associated with their registered account. The cart created during anonymous browsing persists after login.

**Acceptance Criteria:**
- RecoverCartOnLoginUseCase transfers cart ownership on login
- Anonymous user's cart items preserved after registration/login
- Cart associated with registered user's linkedUserId
- Login flow triggers cart recovery automatically

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `cart.application.recovercart.RecoverCartOnLoginUseCase`
- **Patterns:** Use Case Pattern, Identity Continuity

---

### US-46: Checkout Login/Register Options on Buyer Page

**Description:** Add login and register options to the checkout buyer info page, allowing anonymous users to create an account or login during checkout.

**Acceptance Criteria:**
- Buyer page shows login/register options for anonymous users
- Login redirects back to checkout after authentication
- Register redirects back to checkout after account creation
- Registered users see their email pre-filled
- Guest checkout still available without account

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `checkout.adapter.incoming.web.BuyerInfoPageController`
  - `templates/checkout/buyer.pug`
- **Patterns:** Progressive Authentication

---

### US-47: Preserve Cart and Checkout Session on Registration

**Description:** When registering during checkout, the cart and checkout session should be preserved. Fix by removing the 'anon-' prefix from anonymous tokens so UserId doesn't change on registration.

**Acceptance Criteria:**
- Anonymous tokens use raw UUID without 'anon-' prefix
- UserId remains unchanged after registration
- Cart items preserved after registration during checkout
- Checkout session preserved after registration during checkout
- User returns to checkout flow after registration without losing progress
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** infrastructure, domain, sharedkernel
- **Locations:**
  - `infrastructure.security.jwt.JwtTokenService`
  - `sharedkernel.domain.model.UserId`
  - `account.domain.model.Account`
- **Patterns:** Identity Continuity
- **Constraints:**
  - Anonymous tokens must use raw UUID format (no prefix)
  - UserId must be identical before and after registration
  - Update UserId.isAnonymous() to use IdentityType from JWT instead of prefix
  - Remove UserId transformation in Account.register()
  - Run ./gradlew test-architecture to verify

---

### US-48: Cart Merge Options on Login During Checkout

**Description:** When an anonymous user with items in their anonymous cart logs in and their account already has a cart with items, present them with a choice: (1) Merge both carts, (2) Use account cart only, or (3) Use anonymous cart only. Execute the selected choice.

**Acceptance Criteria:**
- Detect if both anonymous cart and account cart have items during login
- Display merge options UI only when both carts have items
- Option 1: Merge - combine items from both carts (add quantities for same products)
- Option 2: Use account cart - discard anonymous cart, keep account cart
- Option 3: Use anonymous cart - replace account cart with anonymous cart items
- After merge/use account: delete anonymous cart
- After use anonymous: keep anonymous cart (becomes the account cart)
- User returns to checkout flow after selection
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** domain, application, adapter
- **Locations:**
  - `cart.domain.model.ShoppingCart (merge method)`
  - `cart.application.mergecarts/MergeCartsUseCase`
  - `cart.application.mergecarts/MergeCartsInputPort`
  - `cart.application.mergecarts/MergeCartsCommand`
  - `cart.application.mergecarts/MergeCartsResponse`
  - `cart.application.getcartmergeoptions/GetCartMergeOptionsUseCase`
  - `cart.adapter.incoming.web.CartMergePageController`
  - `account.adapter.incoming.web.LoginPageController (redirect logic)`
  - `templates/cart/merge-options.pug`
- **Patterns:** Use Case Pattern, Domain Logic in Aggregate, Progressive Authentication
- **Constraints:**
  - Merge logic belongs in ShoppingCart aggregate
  - Use case orchestrates loading both carts and calling merge
  - No direct cart manipulation in controllers
  - Controller redirects to merge page when conflict detected
  - Run ./gradlew test-architecture to verify

---

### US-49: Cart Merge Test Coverage

**Description:** Implement comprehensive test coverage for cart merge functionality using a layered testing approach: unit tests for domain logic, integration tests for use case orchestration, and one E2E test for the full flow.

**Acceptance Criteria:**
- Unit tests for ShoppingCart.merge() covering: same product quantity combination, different products addition, empty cart handling
- Integration tests for MergeCartsUseCase covering all 3 strategies: merge, use-account, use-anonymous
- Integration tests verify correct cart deletion based on strategy
- One E2E test verifying the full flow: login with both carts having items, see merge options, select merge, verify combined cart
- All tests pass: ./gradlew test, ./gradlew test-e2e

**Architectural Guidance:**
- **Affected Layers:** domain, application, test-e2e
- **Locations:**
  - `src/test/java/de/sample/aiarchitecture/cart/domain/model/ShoppingCartTest.java`
  - `src/test/java/de/sample/aiarchitecture/cart/application/mergecarts/MergeCartsUseCaseTest.java`
  - `src/test-e2e/java/de/sample/aiarchitecture/e2e/CartMergeE2ETest.java`
  - `src/test-e2e/java/de/sample/aiarchitecture/e2e/pages/CartMergePage.java`
- **Patterns:** Layered Testing, Page Object Pattern, Unit Test Isolation
- **Constraints:**
  - Unit tests must not depend on Spring context
  - Integration tests use @SpringBootTest with test slices where appropriate
  - E2E test uses Page Object pattern for CartMergePage
  - E2E test covers only one happy path scenario (merge strategy)
  - Edge cases and error scenarios covered by unit/integration tests

---

### US-50: Fix Incoming Adapter Cross-Context Violations

**Description:** Fix architectural violations where incoming adapters access other bounded contexts directly. StartCheckoutPageController should not call cart use cases, and LoginPageController should not call cart use cases. Enforce bounded context isolation at the adapter layer.

**Acceptance Criteria:**
- StartCheckoutPageController accepts cartId as URL parameter instead of calling GetOrCreateActiveCartUseCase
- LoginPageController redirects to /cart/merge with URL parameters instead of calling GetCartMergeOptionsUseCase
- CartMergePageController accepts anonymousUserId and returnUrl from URL parameters (stateless, no session)
- No imports from cart context in checkout adapters
- No imports from cart context in account adapters
- Architecture tests pass: ./gradlew test-architecture
- All tests pass: ./gradlew test

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `checkout.adapter.incoming.web.StartCheckoutPageController`
  - `account.adapter.incoming.web.LoginPageController`
  - `cart.adapter.incoming.web.CartMergePageController`
  - `templates/cart/merge-options.pug`
- **Patterns:** Bounded Context Isolation, URL Parameter State Transfer, Stateless Controllers
- **Constraints:**
  - Incoming adapters must only access their own bounded context's use cases
  - Cross-context coordination happens via URL redirects with parameters
  - No session storage - use URL parameters for state transfer
  - Cart context decides if merge is needed (not account context)
  - UI passes known data (cartId) instead of controller querying other contexts
  - Run ./gradlew test-architecture to verify

---

### US-51: Bounded Context Isolation via Open Host Service Pattern

**Description:** Eliminate cross-context imports in application layer by introducing ProductCatalogService as an Open Host Service in Product context. Cart and Checkout define their own output ports (ProductDataPort, ProductInfoPort) with outgoing adapters that call the OHS. This ensures use cases never import from other bounded contexts.

**Acceptance Criteria:**
- OpenHostService annotation created in sharedkernel/stereotype
- ProductCatalogService created in product/adapter/incoming/openhost with @OpenHostService annotation
- ProductDataPort created in cart/application/shared as Cart's output port for product data
- ProductDataAdapter created in cart/adapter/outgoing/product implementing ProductDataPort
- AddItemToCartUseCase uses ProductDataPort instead of ProductRepository from Product context
- ProductInfoPort created in checkout/application/shared as Checkout's output port for product names
- ProductInfoAdapter created in checkout/adapter/outgoing/product implementing ProductInfoPort
- StartCheckoutUseCase and SyncCheckoutWithCartUseCase use ProductInfoPort instead of ProductRepository
- ArchUnit test updated: application layer has no cross-context imports except documented exceptions
- ArchUnit test added: outgoing adapters may only access @OpenHostService from other contexts
- All tests pass: ./gradlew test-architecture, ./gradlew build

**Architectural Guidance:**
- **Affected Layers:** sharedkernel, application, adapter
- **Locations:**
  - `sharedkernel.stereotype.OpenHostService`
  - `product.adapter.incoming.openhost.ProductCatalogService`
  - `cart.application.shared.ProductDataPort`
  - `cart.adapter.outgoing.product.ProductDataAdapter`
  - `cart.application.additemtocart.AddItemToCartUseCase`
  - `checkout.application.shared.ProductInfoPort`
  - `checkout.adapter.outgoing.product.ProductInfoAdapter`
  - `checkout.application.startcheckout.StartCheckoutUseCase`
  - `checkout.application.synccheckoutwithcart.SyncCheckoutWithCartUseCase`
  - `DddStrategicPatternsArchUnitTest.groovy`
  - `HexagonalArchitectureArchUnitTest.groovy`
- **Patterns:** Open Host Service, Output Port, Adapter Pattern, Bounded Context Isolation
- **Constraints:**
  - Use cases must NEVER import from other bounded contexts directly
  - Cross-context access is isolated to outgoing adapters
  - Outgoing adapters may only import @OpenHostService classes from other contexts
  - Open Host Services return DTOs, never domain objects
  - Each context owns its output port definitions
  - Run ./gradlew test-architecture to verify

---

### US-52: Pricing Domain Value Objects
**Epic:** pricing-context

**Description:** Create pricing domain value objects (PriceId) for the new Pricing bounded context.

**Acceptance Criteria:**
- PriceId value object with generate() and of() methods
- Implements Value interface with proper validation
- All in pricing/domain/model/ package

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `pricing.domain.model.PriceId`
- **Patterns:** Value Object as Java record

---

### US-53: ProductPrice Aggregate
**Epic:** pricing-context
**Depends on:** US-52

**Description:** Create ProductPrice aggregate root for managing product prices with proper invariants.

**Acceptance Criteria:**
- Extends BaseAggregateRoot<ProductPrice, PriceId>
- Fields: id, productId, currentPrice (Money), effectiveFrom (Instant)
- Factory method: create(productId, price)
- Method: updatePrice(newPrice) raises PriceChanged event
- Validates price > 0

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `pricing.domain.model.ProductPrice`
- **Patterns:** Aggregate Root

---

### US-54: Pricing Domain Events
**Epic:** pricing-context
**Depends on:** US-53

**Description:** Create pricing domain events for price changes.

**Acceptance Criteria:**
- PriceCreated event (priceId, productId, price, effectiveFrom)
- PriceChanged event (priceId, productId, oldPrice, newPrice, effectiveFrom)
- All implement DomainEvent interface with eventId, occurredOn, version

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `pricing.domain.event.PriceCreated`
  - `pricing.domain.event.PriceChanged`
- **Patterns:** Domain Event as immutable record

---

### US-55: ProductPriceRepository Interface
**Epic:** pricing-context
**Depends on:** US-53

**Description:** Create repository interface for persisting and retrieving product prices.

**Acceptance Criteria:**
- Extends Repository<ProductPrice, PriceId>
- Methods: findByProductId(ProductId), findByProductIds(Collection<ProductId>)
- Interface in application/shared

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `pricing.application.shared.ProductPriceRepository`
- **Patterns:** Repository Interface

---

### US-56: InMemoryProductPriceRepository
**Epic:** pricing-context
**Depends on:** US-55

**Description:** Implement in-memory repository for pricing context.

**Acceptance Criteria:**
- Implements ProductPriceRepository
- ConcurrentHashMap-based storage
- Secondary index on ProductId for efficient lookups
- Thread-safe operations

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `pricing.adapter.outgoing.persistence.InMemoryProductPriceRepository`
- **Patterns:** In-Memory Repository

---

### US-57: GetPricesForProducts Use Case
**Epic:** pricing-context
**Depends on:** US-55

**Description:** Create bulk price lookup use case for Cart/Checkout to fetch prices efficiently.

**Acceptance Criteria:**
- GetPricesForProductsInputPort extends UseCase
- GetPricesForProductsQuery(Collection<ProductId> productIds)
- GetPricesForProductsResult with Map<ProductId, PriceData>
- PriceData record: productId, currentPrice, effectiveFrom

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `pricing.application.getpricesforproducts`
- **Patterns:** Use Case with Query/Result

---

### US-58: PricingService Open Host Service
**Epic:** pricing-context
**Depends on:** US-57

**Description:** Create PricingService OHS for other contexts to access pricing through a stable API.

**Acceptance Criteria:**
- @OpenHostService annotation
- record PriceInfo(ProductId, Money currentPrice, Instant effectiveFrom)
- getPrices(Collection<ProductId>) returns Map<ProductId, PriceInfo>
- getPrice(ProductId) returns Optional<PriceInfo>
- Delegates to GetPricesForProductsUseCase

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `pricing.adapter.incoming.openhost.PricingService`
- **Patterns:** Open Host Service

---

### US-59: SetProductPrice Use Case
**Epic:** pricing-context
**Depends on:** US-55, US-54

**Description:** Create use case to set/update product prices.

**Acceptance Criteria:**
- SetProductPriceInputPort extends UseCase
- SetProductPriceCommand(productId, priceAmount, priceCurrency)
- Creates new ProductPrice if not exists, updates if exists
- Publishes domain events

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `pricing.application.setproductprice`
- **Patterns:** Use Case with Command/Result

---

### US-60: Pricing Context ArchUnit Rules
**Epic:** pricing-context
**Depends on:** US-58, US-59

**Description:** Verify architecture tests cover new pricing context.

**Acceptance Criteria:**
- Domain has no Spring annotations
- Repository interface in application/shared
- OHS in adapter/incoming/openhost
- All ArchUnit tests pass

**Architectural Guidance:**
- **Affected Layers:** test-architecture
- **Locations:**
  - `Existing ArchUnit tests`
- **Patterns:** Architecture Testing

---

### US-61: Inventory Domain Value Objects
**Epic:** inventory-context

**Description:** Create inventory domain value objects for the new Inventory bounded context.

**Acceptance Criteria:**
- StockLevelId value object with generate() and of()
- StockQuantity value object (int value, cannot be negative)
- Implements Value interface

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `inventory.domain.model.StockLevelId`
  - `inventory.domain.model.StockQuantity`
- **Patterns:** Value Object as Java record

---

### US-62: StockLevel Aggregate
**Epic:** inventory-context
**Depends on:** US-61

**Description:** Create StockLevel aggregate root for managing product inventory with proper invariants.

**Acceptance Criteria:**
- Extends BaseAggregateRoot<StockLevel, StockLevelId>
- Fields: id, productId, availableQuantity (StockQuantity), reservedQuantity
- Factory method: create(productId, initialQuantity)
- Methods: increaseStock(int), decreaseStock(int), reserve(int), release(int)
- isAvailable() returns availableQuantity > reservedQuantity
- Raises StockChanged events

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `inventory.domain.model.StockLevel`
- **Patterns:** Aggregate Root

---

### US-63: Inventory Domain Events
**Epic:** inventory-context
**Depends on:** US-62

**Description:** Create inventory domain events for stock changes.

**Acceptance Criteria:**
- StockLevelCreated event (stockLevelId, productId, quantity)
- StockIncreased event (stockLevelId, productId, addedQuantity, newQuantity)
- StockDecreased event (stockLevelId, productId, removedQuantity, newQuantity)
- StockReserved event (stockLevelId, productId, reservedQuantity)

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `inventory.domain.event`
- **Patterns:** Domain Event as immutable record

---

### US-64: StockLevelRepository Interface
**Epic:** inventory-context
**Depends on:** US-62

**Description:** Create repository interface for persisting and retrieving stock levels.

**Acceptance Criteria:**
- Extends Repository<StockLevel, StockLevelId>
- Methods: findByProductId(ProductId), findByProductIds(Collection<ProductId>)
- Interface in application/shared

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `inventory.application.shared.StockLevelRepository`
- **Patterns:** Repository Interface

---

### US-65: InMemoryStockLevelRepository
**Epic:** inventory-context
**Depends on:** US-64

**Description:** Implement in-memory repository for inventory context.

**Acceptance Criteria:**
- Implements StockLevelRepository
- ConcurrentHashMap-based storage
- Secondary index on ProductId
- Thread-safe operations

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `inventory.adapter.outgoing.persistence.InMemoryStockLevelRepository`
- **Patterns:** In-Memory Repository

---

### US-66: GetStockForProducts Use Case
**Epic:** inventory-context
**Depends on:** US-64

**Description:** Create bulk stock lookup use case for Cart/Checkout to check availability efficiently.

**Acceptance Criteria:**
- GetStockForProductsInputPort extends UseCase
- GetStockForProductsQuery(Collection<ProductId> productIds)
- GetStockForProductsResult with Map<ProductId, StockData>
- StockData record: productId, availableStock, isAvailable

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `inventory.application.getstockforproducts`
- **Patterns:** Use Case with Query/Result

---

### US-67: InventoryService Open Host Service
**Epic:** inventory-context
**Depends on:** US-66

**Description:** Create InventoryService OHS for other contexts to access stock through a stable API.

**Acceptance Criteria:**
- @OpenHostService annotation
- record StockInfo(ProductId, int availableStock, boolean isAvailable)
- getStock(Collection<ProductId>) returns Map<ProductId, StockInfo>
- hasStock(ProductId, int quantity) returns boolean
- Delegates to GetStockForProductsUseCase

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `inventory.adapter.incoming.openhost.InventoryService`
- **Patterns:** Open Host Service

---

### US-68: SetStockLevel Use Case
**Epic:** inventory-context
**Depends on:** US-64, US-63

**Description:** Create use case to set/update stock levels.

**Acceptance Criteria:**
- SetStockLevelInputPort extends UseCase
- SetStockLevelCommand(productId, quantity)
- Creates new StockLevel if not exists, updates if exists
- Publishes domain events

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `inventory.application.setstocklevel`
- **Patterns:** Use Case with Command/Result

---

### US-69: Inventory Context ArchUnit Rules
**Epic:** inventory-context
**Depends on:** US-67, US-68

**Description:** Verify architecture tests cover new inventory context.

**Acceptance Criteria:**
- Domain has no Spring annotations
- Repository interface in application/shared
- OHS in adapter/incoming/openhost
- All ArchUnit tests pass

**Architectural Guidance:**
- **Affected Layers:** test-architecture
- **Locations:**
  - `Existing ArchUnit tests`
- **Patterns:** Architecture Testing

---

### US-70: ArticlePriceResolver Interface
**Epic:** cart-resolver

**Description:** Create functional interface for cart domain to receive fresh pricing without external dependencies.

**Acceptance Criteria:**
- @FunctionalInterface annotation
- ArticlePrice resolve(ProductId productId) method
- record ArticlePrice(Money price, boolean isAvailable, int availableStock) implements Value
- In cart domain package

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `cart.domain.model.ArticlePriceResolver`
- **Patterns:** Functional Interface, Value Object

---

### US-71: CartValidationResult Value Object
**Epic:** cart-resolver
**Depends on:** US-70

**Description:** Create value object for collecting and returning validation errors.

**Acceptance Criteria:**
- record CartValidationResult(List<ValidationError> errors)
- isValid() returns errors.isEmpty()
- record ValidationError(ProductId productId, String message, ErrorType type)
- enum ErrorType { PRODUCT_UNAVAILABLE, INSUFFICIENT_STOCK }
- Static factory methods for each error type

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `cart.domain.model.CartValidationResult`
- **Patterns:** Value Object

---

### US-72: ShoppingCart Resolver Methods
**Epic:** cart-resolver
**Depends on:** US-70, US-71

**Description:** Add resolver-based methods on ShoppingCart aggregate for calculations using fresh pricing data.

**Acceptance Criteria:**
- calculateTotal(ArticlePriceResolver) returns Money
- validateForCheckout(ArticlePriceResolver) returns CartValidationResult
- Keep existing calculateTotal() as @Deprecated for migration
- Both methods iterate items and use resolver for pricing

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `cart.domain.model.ShoppingCart`
- **Patterns:** Aggregate method with injected dependency

---

### US-73: ArticleDataPort Output Port
**Epic:** cart-resolver
**Depends on:** US-58, US-67

**Description:** Create output port for cart use cases to fetch article data.

**Acceptance Criteria:**
- Extends OutputPort interface
- record ArticleData(ProductId, String name, Money currentPrice, int availableStock, boolean isAvailable)
- getArticleData(Collection<ProductId>) returns Map<ProductId, ArticleData>
- getArticleData(ProductId) returns Optional<ArticleData>

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `cart.application.shared.ArticleDataPort`
- **Patterns:** Output Port

---

### US-74: CompositeArticleDataAdapter
**Epic:** cart-resolver
**Depends on:** US-73, US-58, US-67

**Description:** Create composite adapter that aggregates data from multiple Open Host Services.

**Acceptance Criteria:**
- Implements ArticleDataPort
- Injects: ProductCatalogService, PricingService, InventoryService
- Fetches from all three, combines into ArticleData
- Handles missing data gracefully

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `cart.adapter.outgoing.article.CompositeArticleDataAdapter`
- **Patterns:** Composite Adapter
- **Constraints:**
  - Only import @OpenHostService classes from other contexts
  - Never import domain objects from other contexts

---

### US-75: Update CheckoutCartUseCase with Resolver
**Epic:** cart-resolver
**Depends on:** US-72, US-74

**Description:** Update CheckoutCartUseCase to use resolver pattern for checkout validation with fresh data.

**Acceptance Criteria:**
- Inject ArticleDataPort
- Fetch article data for all cart items
- Build resolver from fetched data
- Call cart.checkout(resolver) instead of cart.checkout()
- Handle validation errors appropriately

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `cart.application.checkoutcart.CheckoutCartUseCase`
- **Patterns:** Use Case orchestration

---

### US-76: Update GetCartByIdUseCase with Fresh Data
**Epic:** cart-resolver
**Depends on:** US-73, US-74

**Description:** Update GetCartByIdUseCase to enrich results with fresh pricing.

**Acceptance Criteria:**
- Inject ArticleDataPort
- Fetch article data for all cart items
- Include currentPrice and availability in result
- Add priceChanged flag if currentPrice != priceAtAddition

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `cart.application.getcartbyid.GetCartByIdUseCase`
- **Patterns:** Query enrichment

---

### US-77: CheckoutArticlePriceResolver Interface
**Epic:** checkout-resolver

**Description:** Create functional interface for checkout domain to receive fresh pricing.

**Acceptance Criteria:**
- @FunctionalInterface annotation
- ArticlePrice resolve(ProductId productId) method
- record ArticlePrice(Money price, boolean isAvailable, int availableStock) implements Value
- In checkout domain package

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `checkout.domain.model.CheckoutArticlePriceResolver`
- **Patterns:** Functional Interface

---

### US-78: CheckoutValidationResult Value Object
**Epic:** checkout-resolver
**Depends on:** US-77

**Description:** Create value object for collecting checkout validation errors.

**Acceptance Criteria:**
- record CheckoutValidationResult(List<ValidationError> errors)
- isValid() returns errors.isEmpty()
- Similar structure to CartValidationResult

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `checkout.domain.model.CheckoutValidationResult`
- **Patterns:** Value Object

---

### US-79: CheckoutSession Resolver Methods
**Epic:** checkout-resolver
**Depends on:** US-77, US-78

**Description:** Add resolver-based methods on CheckoutSession aggregate for order totals using fresh pricing.

**Acceptance Criteria:**
- calculateOrderTotal(CheckoutArticlePriceResolver) returns Money
- validateItems(CheckoutArticlePriceResolver) returns CheckoutValidationResult
- Update confirm() to accept resolver and validate before confirming

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `checkout.domain.model.CheckoutSession`
- **Patterns:** Aggregate method with injected dependency

---

### US-80: CheckoutArticleDataPort Output Port
**Epic:** checkout-resolver
**Depends on:** US-58, US-67

**Description:** Create output port for checkout use cases to fetch article data.

**Acceptance Criteria:**
- Extends OutputPort interface
- record ArticleData(ProductId, String name, Money currentPrice, int availableStock, boolean isAvailable)
- getArticleData(Collection<ProductId>) returns Map<ProductId, ArticleData>

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `checkout.application.shared.CheckoutArticleDataPort`
- **Patterns:** Output Port

---

### US-81: CompositeCheckoutArticleDataAdapter
**Epic:** checkout-resolver
**Depends on:** US-80, US-58, US-67

**Description:** Create composite adapter for checkout to get article data from the right sources.

**Acceptance Criteria:**
- Implements CheckoutArticleDataPort
- Injects: ProductCatalogService, PricingService, InventoryService
- Same pattern as Cart's CompositeArticleDataAdapter

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `checkout.adapter.outgoing.article.CompositeCheckoutArticleDataAdapter`
- **Patterns:** Composite Adapter

---

### US-82: Update StartCheckoutUseCase with Resolver
**Epic:** checkout-resolver
**Depends on:** US-79, US-81

**Description:** Update StartCheckoutUseCase to use resolver pattern for checkout with fresh pricing.

**Acceptance Criteria:**
- Inject CheckoutArticleDataPort
- Fetch article data for all line items
- Build resolver from fetched data
- Pass resolver to CheckoutSession methods

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `checkout.application.startcheckout.StartCheckoutUseCase`
- **Patterns:** Use Case orchestration

---

### US-83: Update ConfirmCheckoutUseCase with Resolver
**Epic:** checkout-resolver
**Depends on:** US-79, US-81

**Description:** Update ConfirmCheckoutUseCase to validate with fresh data before final confirmation.

**Acceptance Criteria:**
- Inject CheckoutArticleDataPort
- Fetch fresh article data before confirmation
- Build resolver and call session.confirm(resolver)
- Handle validation errors

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `checkout.application.confirmcheckout.ConfirmCheckoutUseCase`
- **Patterns:** Use Case orchestration

---

### US-84: Initialize Pricing Data from Products
**Epic:** data-migration
**Depends on:** US-56

**Description:** Initialize pricing data from existing products on startup.

**Acceptance Criteria:**
- AsyncInitialize method in InMemoryProductPriceRepository
- Load all products and create ProductPrice for each
- Copy price from Product to Pricing context

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `pricing.adapter.outgoing.persistence.InMemoryProductPriceRepository`
- **Patterns:** Data initialization

---

### US-85: Initialize Inventory Data from Products
**Epic:** data-migration
**Depends on:** US-65

**Description:** Initialize inventory data from existing products on startup.

**Acceptance Criteria:**
- AsyncInitialize method in InMemoryStockLevelRepository
- Load all products and create StockLevel for each
- Copy stock quantity from Product to Inventory context

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `inventory.adapter.outgoing.persistence.InMemoryStockLevelRepository`
- **Patterns:** Data initialization

---

### US-86: Update Product Context - Remove Pricing Responsibility
**Epic:** data-migration
**Depends on:** US-75, US-76, US-82, US-83

**Description:** Update Product context to no longer manage pricing (separation of concerns).

**Acceptance Criteria:**
- Deprecate price-related methods in Product aggregate
- ProductCatalogService OHS returns name/SKU only (not price)
- Update ProductDataPort in Cart to not expect pricing
- Keep Price in Product for now (remove in future story)

**Architectural Guidance:**
- **Affected Layers:** domain, adapter
- **Locations:**
  - `product.domain.model.Product`
  - `product.adapter.incoming.openhost.ProductCatalogService`
- **Patterns:** Gradual migration

---

### US-87: Integration Tests for Article Data Flow
**Epic:** data-migration
**Depends on:** US-74, US-81, US-84, US-85

**Description:** Create integration tests for the full article data flow to verify resolver pattern works correctly.

**Acceptance Criteria:**
- Test CompositeArticleDataAdapter with all three OHS
- Test CheckoutCartUseCase with resolver
- Test GetCartByIdUseCase enrichment
- Test price change detection

**Architectural Guidance:**
- **Affected Layers:** test
- **Locations:**
  - `src/test/java/.../cart/application/`
- **Patterns:** Integration Testing

---

### US-88: Remove Deprecated Price from Product Context
**Epic:** data-migration
**Depends on:** US-86, US-87

**Description:** Complete the pricing migration by removing all deprecated price-related code from the Product context.

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
- **Affected Layers:** domain, application, adapter
- **Locations:**
  - `product/domain/model/Product.java`
  - `product/domain/event/ProductPriceChanged.java`
  - `product/adapter/incoming/openhost/ProductCatalogService.java`
  - `product/application/createproduct/CreateProductUseCase.java`
  - `product/application/updateproductprice/`
  - `cart/adapter/outgoing/product/CompositeArticleDataAdapter.java`
  - `checkout/adapter/outgoing/product/CompositeCheckoutArticleDataAdapter.java`
- **Patterns:** Migration cleanup, Bounded context separation

---

### US-89: Remove Stock Responsibility from Product Context
**Epic:** data-migration
**Depends on:** US-85, US-74, US-81

**Description:** Remove stock/availability responsibility from Product context since Inventory is the proper owner. Product context should only own identity (productId, sku) and description (name, description, category). This eliminates the duplicated stock data between Product.stock and Inventory.stockLevel.

**Acceptance Criteria:**
- Remove ProductStock value object from Product domain
- Remove stock field from Product aggregate
- Remove stockQuantity from ProductCatalogService.ProductInfo DTO
- Remove hasStock() method from ProductCatalogService
- Remove ProductDataPort from Cart context (no longer needed)
- Remove ProductDataAdapter from Cart context
- Update AddItemToCartUseCase to use only ArticleDataPort for validation
- CompositeArticleDataAdapter uses InventoryService for stock (not ProductCatalogService)
- Update CreateProductCommand to not require stockQuantity
- Update CreateProductUseCase to not set stock
- All tests pass: ./gradlew build, ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** domain, application, adapter
- **Locations:**
  - `product/domain/model/ProductStock.java (DELETE)`
  - `product/domain/model/Product.java (remove stock field)`
  - `product/adapter/incoming/openhost/ProductCatalogService.java`
  - `cart/application/shared/ProductDataPort.java (DELETE)`
  - `cart/adapter/outgoing/product/ProductDataAdapter.java (DELETE)`
  - `cart/application/additemtocart/AddItemToCartUseCase.java`
  - `cart/adapter/outgoing/product/CompositeArticleDataAdapter.java`
  - `product/application/createproduct/CreateProductCommand.java`
  - `product/application/createproduct/CreateProductUseCase.java`
- **Patterns:** Bounded Context Separation, Single Source of Truth
- **Constraints:**
  - Inventory context is the single source of truth for stock
  - Product context owns only identity and descriptive information
  - No stock-related methods or fields in Product context after completion
  - Run ./gradlew test-architecture to verify

---

### US-90: CheckoutArticle Domain Value Object
**Epic:** enriched-cart-pattern
**Depends on:** US-89

**Description:** Promote ArticleData from application port to proper domain value object. Currently ArticleData is nested inside CheckoutArticleDataPort (application layer), preventing domain logic from using it directly. Create CheckoutArticle as a domain-level value object that represents checkout's view of article data from external contexts.

Before starting, check tasks/logs/ folder for US-89 result to understand the current article data flow, or check git history for recent changes to the checkout context.

**Acceptance Criteria:**
- CheckoutArticle record in checkout/domain/model/
- Fields: ProductId productId, String name, Money currentPrice, int availableStock, boolean isAvailable
- Implements Value interface
- Method: hasStockFor(int quantity) returns boolean
- Validation: productId not null, name not blank, currentPrice not null, availableStock >= 0
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `checkout/domain/model/CheckoutArticle.java`
- **Patterns:** Value Object, Anti-Corruption Layer
- **Constraints:**
  - This is checkout context's representation of external article data
  - Domain value objects must be immutable (Java record)
  - Run ./gradlew test-architecture to verify

---

### US-91: EnrichedCheckoutLineItem Value Object
**Epic:** enriched-cart-pattern
**Depends on:** US-90

**Description:** Create value object that combines a CheckoutLineItem with current CheckoutArticle data. This enables domain logic like 'has price changed since item was added' and 'is stock sufficient for this quantity'.

Before starting, check tasks/logs/ folder for US-90 result to understand the CheckoutArticle structure.

**Acceptance Criteria:**
- EnrichedCheckoutLineItem record in checkout/domain/model/
- Fields: CheckoutLineItem lineItem, CheckoutArticle currentArticle
- Implements Value interface
- Method: currentLineTotal() returns Money (currentPrice * quantity)
- Method: originalLineTotal() returns Money (lineItem.lineTotal())
- Method: hasPriceChanged() returns boolean
- Method: priceDifference() returns Money
- Method: hasSufficientStock() returns boolean
- Method: isValidForCheckout() returns boolean (available AND sufficient stock)
- Validation: productId must match between lineItem and currentArticle
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `checkout/domain/model/EnrichedCheckoutLineItem.java`
- **Patterns:** Value Object, Enriched Read Model
- **Constraints:**
  - Encapsulates comparison logic between original and current data
  - Implements 'smart cart display' metaphor
  - Run ./gradlew test-architecture to verify

---

### US-92: CheckoutCart Domain Value Object
**Epic:** enriched-cart-pattern
**Depends on:** US-91

**Description:** Create the 'smart shopping cart' value object that contains enriched line items and provides business methods for validation and calculations. This is like a shopping cart with a display that shows running total, current prices, and alerts for price changes.

Before starting, check tasks/logs/ folder for US-91 result to understand the EnrichedCheckoutLineItem structure.

**Acceptance Criteria:**
- CheckoutCart record in checkout/domain/model/
- Fields: CartId cartId, CustomerId customerId, List<EnrichedCheckoutLineItem> items
- Implements Value interface
- Method: calculateCurrentSubtotal() returns Money
- Method: calculateOriginalSubtotal() returns Money
- Method: totalPriceDifference() returns Money
- Method: hasAnyPriceChanges() returns boolean
- Method: itemsWithPriceChanges() returns List<EnrichedCheckoutLineItem>
- Method: isValidForCheckout() returns boolean
- Method: invalidItems() returns List<EnrichedCheckoutLineItem>
- Method: unavailableItems() returns List<EnrichedCheckoutLineItem>
- Method: itemsWithInsufficientStock() returns List<EnrichedCheckoutLineItem>
- Method: itemCount() and totalQuantity()
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `checkout/domain/model/CheckoutCart.java`
- **Patterns:** Value Object, Enriched Read Model, Smart Cart Pattern
- **Constraints:**
  - Not an Aggregate - just a read model Value Object
  - Business logic for checkout validation lives here
  - Run ./gradlew test-architecture to verify

---

### US-93: CheckoutCartFactory
**Epic:** enriched-cart-pattern
**Depends on:** US-92

**Description:** Create factory to assemble CheckoutCart from checkout line items and article data. The factory encapsulates the complex assembly of enriched line items.

Before starting, check tasks/logs/ folder for US-92 result to understand the CheckoutCart structure.

**Acceptance Criteria:**
- CheckoutCartFactory class in checkout/domain/model/
- Implements Factory marker interface
- Method: create(CartId, CustomerId, List<CheckoutLineItem>, Map<ProductId, CheckoutArticle>) returns CheckoutCart
- Method: fromSession(CheckoutSession, Map<ProductId, CheckoutArticle>) returns CheckoutCart
- Validates that all line items have corresponding article data
- Throws IllegalArgumentException if article data missing for any line item
- Framework-independent (no Spring annotations)
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `checkout/domain/model/CheckoutCartFactory.java`
- **Patterns:** Factory Pattern (DDD), Cross-Context Assembly
- **Constraints:**
  - Factory is domain-level, not application-level
  - Application layer fetches data via ports, passes to factory
  - Run ./gradlew test-architecture to verify

---

### US-94: Update CheckoutArticleDataPort to Return Domain Objects
**Epic:** enriched-cart-pattern
**Depends on:** US-93

**Description:** Refactor CheckoutArticleDataPort to return CheckoutArticle domain objects instead of nested ArticleData DTOs. Update the CompositeCheckoutArticleDataAdapter to return the new domain type.

Before starting, check tasks/logs/ folder for US-93 result to understand the factory pattern usage.

**Acceptance Criteria:**
- CheckoutArticleDataPort returns Map<ProductId, CheckoutArticle>
- Remove nested ArticleData record from CheckoutArticleDataPort
- Update CompositeCheckoutArticleDataAdapter to build CheckoutArticle objects
- Update StartCheckoutUseCase to use CheckoutArticle and CheckoutCartFactory
- Update ConfirmCheckoutUseCase to use CheckoutArticle
- Keep CheckoutArticlePriceResolver for backward compatibility with confirm() method
- All tests pass: ./gradlew build
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** application, adapter
- **Locations:**
  - `checkout/application/shared/CheckoutArticleDataPort.java`
  - `checkout/adapter/outgoing/product/CompositeCheckoutArticleDataAdapter.java`
  - `checkout/application/startcheckout/StartCheckoutUseCase.java`
  - `checkout/application/confirmcheckout/ConfirmCheckoutUseCase.java`
- **Patterns:** Output Port, Adapter Pattern
- **Constraints:**
  - Ports should return domain objects, not DTOs
  - Adapter translates OHS data to domain value objects
  - Run ./gradlew test-architecture to verify

---

### US-95: CartArticle Domain Value Object
**Epic:** enriched-cart-pattern
**Depends on:** US-90

**Description:** Create CartArticle domain value object in Cart context (mirroring CheckoutArticle). This promotes ArticleData from the ArticleDataPort to a proper domain concept.

Before starting, check tasks/logs/ folder for US-90 result to see the equivalent CheckoutArticle implementation as reference.

**Acceptance Criteria:**
- CartArticle record in cart/domain/model/
- Fields: ProductId productId, String name, Money currentPrice, int availableStock, boolean isAvailable
- Implements Value interface
- Method: hasStockFor(int quantity) returns boolean
- Same structure as CheckoutArticle (context-specific ownership)
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `cart/domain/model/CartArticle.java`
- **Patterns:** Value Object, Anti-Corruption Layer
- **Constraints:**
  - Each bounded context owns its value object definition
  - Same structure as CheckoutArticle is intentional (context isolation)
  - Run ./gradlew test-architecture to verify

---

### US-96: EnrichedCartItem Value Object
**Epic:** enriched-cart-pattern
**Depends on:** US-95

**Description:** Create value object that combines a CartItem with current CartArticle data in the Cart context.

Before starting, check tasks/logs/ folder for US-91 and US-95 results for reference.

**Acceptance Criteria:**
- EnrichedCartItem record in cart/domain/model/
- Fields: CartItem cartItem, CartArticle currentArticle
- Implements Value interface
- Method: currentLineTotal() returns Money
- Method: originalLineTotal() returns Money (priceAtAddition * quantity)
- Method: hasPriceChanged() returns boolean
- Method: priceDifference() returns Money
- Method: hasSufficientStock() returns boolean
- Method: isValidForCheckout() returns boolean
- Validation: productId must match
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `cart/domain/model/EnrichedCartItem.java`
- **Patterns:** Value Object, Enriched Read Model
- **Constraints:**
  - Mirror structure of EnrichedCheckoutLineItem
  - Run ./gradlew test-architecture to verify

---

### US-97: EnrichedCart Domain Value Object
**Epic:** enriched-cart-pattern
**Depends on:** US-96

**Description:** Create the 'smart shopping cart' value object for Cart context with enriched items and business methods.

Before starting, check tasks/logs/ folder for US-92 and US-96 results for reference.

**Acceptance Criteria:**
- EnrichedCart record in cart/domain/model/
- Fields: CartId cartId, CustomerId customerId, List<EnrichedCartItem> items, CartStatus status
- Implements Value interface
- Method: calculateCurrentSubtotal() returns Money
- Method: calculateOriginalSubtotal() returns Money
- Method: totalPriceDifference() returns Money
- Method: hasAnyPriceChanges() returns boolean
- Method: itemsWithPriceChanges() returns List<EnrichedCartItem>
- Method: isValidForCheckout() returns boolean
- Method: invalidItems() returns List<EnrichedCartItem>
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `cart/domain/model/EnrichedCart.java`
- **Patterns:** Value Object, Enriched Read Model, Smart Cart Pattern
- **Constraints:**
  - Mirror structure of CheckoutCart
  - Not an Aggregate - read model only
  - Run ./gradlew test-architecture to verify

---

### US-98: EnrichedCartFactory
**Epic:** enriched-cart-pattern
**Depends on:** US-97

**Description:** Create factory to assemble EnrichedCart from ShoppingCart and article data.

Before starting, check tasks/logs/ folder for US-93 and US-97 results for reference.

**Acceptance Criteria:**
- EnrichedCartFactory class in cart/domain/model/
- Implements Factory marker interface
- Method: create(ShoppingCart, Map<ProductId, CartArticle>) returns EnrichedCart
- Validates that all cart items have corresponding article data
- Framework-independent
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `cart/domain/model/EnrichedCartFactory.java`
- **Patterns:** Factory Pattern (DDD)
- **Constraints:**
  - Mirror structure of CheckoutCartFactory
  - Run ./gradlew test-architecture to verify

---

### US-99: Update ArticleDataPort to Return Domain Objects
**Epic:** enriched-cart-pattern
**Depends on:** US-98

**Description:** Refactor ArticleDataPort to return CartArticle domain objects. Update the CompositeArticleDataAdapter and use cases.

Before starting, check tasks/logs/ folder for US-94 and US-98 results for reference.

**Acceptance Criteria:**
- ArticleDataPort returns Map<ProductId, CartArticle>
- Remove nested ArticleData record from ArticleDataPort
- Update CompositeArticleDataAdapter to build CartArticle objects
- Update GetCartByIdUseCase to use CartArticle and EnrichedCartFactory
- Update CheckoutCartUseCase to use EnrichedCart for validation
- Keep ArticlePriceResolver for backward compatibility
- All tests pass: ./gradlew build
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** application, adapter
- **Locations:**
  - `cart/application/shared/ArticleDataPort.java`
  - `cart/adapter/outgoing/product/CompositeArticleDataAdapter.java`
  - `cart/application/getcartbyid/GetCartByIdUseCase.java`
  - `cart/application/checkoutcart/CheckoutCartUseCase.java`
- **Patterns:** Output Port, Adapter Pattern
- **Constraints:**
  - Run ./gradlew test-architecture to verify

---

### US-100: Document Enriched Cart Patterns
**Epic:** enriched-cart-pattern
**Depends on:** US-94, US-99

**Description:** Document the new patterns in architecture documentation: Composite Adapter, Enriched Read Model, Factory for Cross-Context Assembly.

Before starting, check tasks/logs/ folder for US-94 and US-99 results to see the completed implementations.

**Acceptance Criteria:**
- Add 'Composite Adapter Pattern' section to architecture-principles.md
- Add 'Enriched Read Model Pattern' section to architecture-principles.md
- Add 'Factory for Cross-Context Assembly' section to architecture-principles.md
- Include code examples from actual implementations
- Update implementing-domain-centric-architecture/README.md if applicable
- Documentation follows concise style (no bloat)

**Architectural Guidance:**
- **Affected Layers:** documentation
- **Locations:**
  - `docs/architecture/architecture-principles.md`
  - `implementing-domain-centric-architecture/README.md`
- **Patterns:** Documentation
- **Constraints:**
  - Keep documentation concise and to the point
  - Include real code examples from implementation
  - Link to related docs instead of duplicating

---

### US-101: Product Detail Page Availability from Inventory Context
**Epic:** inventory-integration
**Depends on:** US-85

**Description:** Display product availability information on the product detail page by fetching stock data from the Inventory Context via a proper output port, following hexagonal architecture principles.

**Acceptance Criteria:**
- ProductStockDataPort output port in product/application/shared/
- InventoryStockDataAdapter in product/adapter/outgoing/inventory/
- GetProductByIdUseCase uses ProductStockDataPort for stock information
- GetProductByIdResult includes stockQuantity and isAvailable fields
- ProductPageController passes isAvailable to template model
- Product detail page shows 'In Stock' or 'Out of Stock' from Inventory Context
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** application, adapter
- **Locations:**
  - `product/application/shared/ProductStockDataPort.java`
  - `product/adapter/outgoing/inventory/InventoryStockDataAdapter.java`
  - `product/application/getproductbyid/GetProductByIdUseCase.java`
  - `product/application/getproductbyid/GetProductByIdResult.java`
  - `product/adapter/incoming/web/ProductPageController.java`
- **Patterns:** Output Port, Outgoing Adapter, Open Host Service Consumer
- **Constraints:**
  - Controllers must NOT call external services directly
  - Stock data flows through output port -> adapter -> OHS
  - Product context consumes Inventory context via InventoryService OHS

---

### US-102: Product Catalog Availability from Inventory Context
**Epic:** inventory-integration
**Depends on:** US-101

**Description:** Display product availability information on the product catalog overview page by fetching bulk stock data from the Inventory Context.

**Acceptance Criteria:**
- ProductStockDataPort has method for bulk stock fetching: getStockData(Collection<ProductId>)
- GetAllProductsUseCase uses ProductStockDataPort for stock information
- GetAllProductsResult.ProductSummary includes stockQuantity field
- Product catalog page shows stock status for all products
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** application, adapter
- **Locations:**
  - `product/application/shared/ProductStockDataPort.java`
  - `product/adapter/outgoing/inventory/InventoryStockDataAdapter.java`
  - `product/application/getallproducts/GetAllProductsUseCase.java`
  - `product/application/getallproducts/GetAllProductsResult.java`
- **Patterns:** Output Port, Bulk Data Fetching
- **Constraints:**
  - Bulk fetching for efficiency (single call for all products)
  - Graceful handling when stock data not found

---

### US-103: Reduce Inventory Stock on Order Confirmation
**Epic:** inventory-integration
**Depends on:** US-65

**Description:** When a checkout is confirmed, reduce stock levels in the Inventory Context for all ordered products using domain events and eventual consistency.

**Acceptance Criteria:**
- ReduceStockCommand record in inventory/application/reducestock/
- ReduceStockResult record in inventory/application/reducestock/
- ReduceStockInputPort interface extending UseCase
- ReduceStockUseCase implements ReduceStockInputPort
- CheckoutConfirmedEventConsumer in inventory/adapter/incoming/event/
- Event consumer listens for CheckoutConfirmed integration event
- Stock is reduced for each line item in the confirmed checkout
- Domain events (StockDecreased/StockDepleted) are published
- Logging for successful and failed stock reductions
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** application, adapter
- **Locations:**
  - `inventory/application/reducestock/ReduceStockCommand.java`
  - `inventory/application/reducestock/ReduceStockResult.java`
  - `inventory/application/reducestock/ReduceStockInputPort.java`
  - `inventory/application/reducestock/ReduceStockUseCase.java`
  - `inventory/adapter/incoming/event/CheckoutConfirmedEventConsumer.java`
- **Patterns:** Use Case Pattern, Event Consumer Adapter, Eventual Consistency, Integration Events
- **Constraints:**
  - Stock reduction happens in Inventory Context (not Product Context)
  - Event-driven: Checkout publishes, Inventory consumes
  - Partial failures allowed (eventual consistency)

---

### US-104: Add Stock Information to MCP Product Catalog Tool
**Epic:** inventory-integration
**Depends on:** US-101

**Description:** Extend the MCP Product Catalog Tool to include stock quantity and availability information in product responses.

**Acceptance Criteria:**
- ProductDto includes stockQuantity and isAvailable fields
- ProductDtoConverter includes stock fields in all toDto methods
- MCP getAllProducts returns stock information for each product
- MCP getProductById returns stock information
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `product/adapter/incoming/mcp/ProductDto.java`
  - `product/adapter/incoming/mcp/ProductDtoConverter.java`
  - `product/adapter/incoming/mcp/ProductCatalogMcpToolProvider.java`
- **Patterns:** MCP Tool Provider, DTO Pattern
- **Constraints:**
  - MCP responses include real-time stock from Inventory Context
  - Stock information flows through existing use cases

---

### US-105: Add @NullMarked Package-Level Nullability
**Epic:** code-quality

**Description:** Add JSpecify @NullMarked annotation at package level for all bounded contexts and shared kernel, removing redundant @NonNull annotations for cleaner code.

**Acceptance Criteria:**
- @NullMarked added to all bounded context package-info.java files (account, cart, checkout, portal, product, inventory, pricing)
- @NullMarked added to sharedkernel/package-info.java
- @NullMarked added to new infrastructure/package-info.java
- All @NonNull annotations removed from src/main/java (232 files cleaned)
- Import statements for org.jspecify.annotations.NonNull removed
- Code formatting preserved after cleanup
- Build passes: ./gradlew build
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** all
- **Locations:**
  - `*/package-info.java`
  - `All Java files in src/main/java`
- **Patterns:** JSpecify Nullability, Package-Level Annotations
- **Constraints:**
  - All types are non-null by default with @NullMarked
  - Only @Nullable annotations needed for parameters/returns that can be null
  - No @NonNull annotations should remain in codebase

---


## Goals
- Domain-Centric Architecture reference implementation
- DDD tactical and strategic patterns demonstration
- Hexagonal Architecture with ports and adapters
- Clean Architecture with framework-independent domain
- 5-step checkout flow (Buyer Info -> Delivery -> Payment -> Review -> Confirmation)
- Guest checkout (no account required)
- Pricing and Inventory as separate bounded contexts
- Fresh pricing/availability for all cart/checkout calculations

## Non-Goals
- Actual payment processing (mock provider only)
- Order management after checkout
- External pricing/inventory service integration
- Price history tracking
- Production-ready deployment configuration

### US-116: ProductStateInterest Interface
**Epic:** interest-interface-pattern
**Depends on:** US-106

**Description:** Create the Interest interface for Product aggregate. This interface defines receive*() methods for all state that can be exposed from the product aggregate.

**Acceptance Criteria:**
- ProductStateInterest interface in product/domain/model/
- Interface implements StateInterest marker
- receiveProductId(ProductId) method
- receiveSku(String) method
- receiveName(String) method
- receiveDescription(String) method
- receiveCategory(String) method
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `product/domain/model/ProductStateInterest.java`
- **Patterns:** Interest Interface Pattern, Double Dispatch
- **Constraints:**
  - No Spring annotations in domain layer
  - Use domain types (ProductId) in method signatures where appropriate
  - Methods return void - push model, not pull

---

### US-117: Product provideStateTo Method
**Epic:** interest-interface-pattern
**Depends on:** US-116

**Description:** Add provideStateTo(ProductStateInterest) method to Product aggregate. The aggregate pushes its state to interested parties through this method, keeping control over what is exposed.

**Acceptance Criteria:**
- provideStateTo(ProductStateInterest interest) method added to Product
- Method calls all appropriate receive*() methods on the interest
- ProductId, SKU, name, description, and category are pushed
- Existing getters can remain temporarily
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `product/domain/model/Product.java`
- **Patterns:** Interest Interface Pattern, Tell Don't Ask
- **Constraints:**
  - Aggregate controls what state is exposed
  - No new getters should be added
  - Method must be comprehensive - push all relevant state

---

### US-118: ProductArticle and EnrichedProduct Read Models
**Epic:** interest-interface-pattern
**Depends on:** US-117

**Description:** Create ProductArticle value object for external pricing/stock data and EnrichedProduct domain read model that combines aggregate state with external article data.

**Acceptance Criteria:**
- ProductArticle record in product/domain/model/ with Money currentPrice, int stockQuantity, boolean isAvailable
- EnrichedProduct record in product/domain/model/ combining aggregate fields and article data
- EnrichedProduct implements Value marker
- EnrichedProduct has isInStock() convenience method
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `product/domain/model/ProductArticle.java`
  - `product/domain/model/EnrichedProduct.java`
- **Patterns:** Enriched Read Model, Value Object
- **Constraints:**
  - Both must be immutable (use records)
  - No Spring annotations in domain layer
  - EnrichedProduct combines snapshot (from aggregate) + current (from external sources)

---

### US-119: EnrichedProductBuilder
**Epic:** interest-interface-pattern
**Depends on:** US-118

**Description:** Create EnrichedProductBuilder that implements ProductStateInterest to build enriched product read models. The builder combines state from the aggregate with current pricing/stock data from external services.

**Acceptance Criteria:**
- Create product/domain/readmodel/ package
- EnrichedProductStateInterest extends ProductStateInterest with receiveArticleData(ProductArticle)
- EnrichedProductBuilder class in product/domain/readmodel/
- Builder implements EnrichedProductStateInterest
- Builder implements ReadModelBuilder marker
- All receive*() methods store state internally
- build() method returns immutable EnrichedProduct
- reset() method for builder reuse
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `product/domain/readmodel/EnrichedProductStateInterest.java`
  - `product/domain/readmodel/EnrichedProductBuilder.java`
- **Patterns:** Interest Interface Pattern, Builder Pattern, Enriched Read Model
- **Constraints:**
  - Builder is in domain layer (not application)
  - EnrichedProduct must be immutable
  - No Spring annotations in domain layer

---

### US-120: Refactor Product Use Cases
**Epic:** interest-interface-pattern
**Depends on:** US-119

**Description:** Refactor product use cases (GetProductByIdUseCase, GetAllProductsUseCase) to use the builder pattern with ProductStateInterest instead of directly accessing aggregate getters.

**Acceptance Criteria:**
- GetProductByIdUseCase uses EnrichedProductBuilder
- GetAllProductsUseCase uses EnrichedProductBuilder
- Aggregate provideStateTo() is called with builder
- External article data (pricing, stock) pushed via receiveArticleData()
- GetProductByIdResult wraps EnrichedProduct (not ProductSummary)
- GetAllProductsResult wraps List<EnrichedProduct>
- No direct getter calls on Product (except getId())
- Existing tests continue to pass
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** application
- **Locations:**
  - `product/application/getproductbyid/GetProductByIdUseCase.java`
  - `product/application/getproductbyid/GetProductByIdResult.java`
  - `product/application/getallproducts/GetAllProductsUseCase.java`
  - `product/application/getallproducts/GetAllProductsResult.java`
- **Patterns:** Interest Interface Pattern, Use Case Pattern, Enriched Read Model
- **Constraints:**
  - Use case orchestrates: load aggregate, provide state to builder, enrich, return result
  - No direct aggregate state access except ID

---

### US-121: Update Product Context ViewModels
**Epic:** interest-interface-pattern
**Depends on:** US-120

**Description:** Update product context ViewModels (ProductCatalogPageViewModel, ProductDetailPageViewModel) to work with EnrichedProduct domain read model instead of Result objects.

**Acceptance Criteria:**
- ProductCatalogPageViewModel.ProductItemViewModel.fromEnrichedProduct() factory method
- ProductDetailPageViewModel.fromResult() updated to use EnrichedProduct
- ProductDtoConverter.toDto(EnrichedProduct) for REST API
- ProductCatalogService (Open Host Service) updated to use EnrichedProduct
- All web templates continue to work with updated ViewModels
- REST API responses unchanged (backward compatible)
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `product/adapter/incoming/web/ProductCatalogPageViewModel.java`
  - `product/adapter/incoming/web/ProductDetailPageViewModel.java`
  - `product/adapter/incoming/api/ProductDtoConverter.java`
  - `product/adapter/incoming/openhost/ProductCatalogService.java`
- **Patterns:** ViewModel Pattern, DTO Converter, Open Host Service
- **Constraints:**
  - ViewModels use primitives only
  - DTOs must remain backward compatible for API consumers
  - Controllers map Result -> ViewModel in adapter layer

---

### US-122: Update Cart Context ViewModels
**Epic:** interest-interface-pattern
**Depends on:** US-114

**Description:** Update cart context ViewModels (CartPageViewModel) to work with EnrichedCart domain read model. ViewModels convert enriched read models to primitives for templates.

**Acceptance Criteria:**
- CartPageViewModel in cart/adapter/incoming/web/
- CartPageViewModel.fromEnrichedCart() factory method
- CartLineItemViewModel with primitives only (no domain types)
- ShoppingCartDtoConverter.toDto(EnrichedCart) for REST API
- CartPageController uses CartPageViewModel
- All web templates continue to work with updated ViewModels
- REST API responses unchanged (backward compatible)
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `cart/adapter/incoming/web/CartPageViewModel.java`
  - `cart/adapter/incoming/web/CartPageController.java`
  - `cart/adapter/incoming/api/ShoppingCartDtoConverter.java`
- **Patterns:** ViewModel Pattern, DTO Converter
- **Constraints:**
  - ViewModels use primitives only (String, BigDecimal, int, boolean)
  - DTOs must remain backward compatible for API consumers
  - Controllers map Result -> ViewModel in adapter layer
  - No domain types in ViewModels

---

### US-123: Update Checkout Context ViewModels
**Epic:** interest-interface-pattern
**Depends on:** US-110

**Description:** Update checkout context ViewModels to work with CheckoutCart domain read model. ViewModels convert enriched read models to primitives for checkout flow templates.

**Acceptance Criteria:**
- CheckoutPageViewModel in checkout/adapter/incoming/web/
- CheckoutPageViewModel.fromCheckoutCart() factory method
- CheckoutLineItemViewModel with primitives only
- Step-specific ViewModels (BuyerInfoViewModel, DeliveryViewModel, etc.) if needed
- CheckoutPageController uses CheckoutPageViewModel
- All checkout flow templates continue to work
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** adapter
- **Locations:**
  - `checkout/adapter/incoming/web/CheckoutPageViewModel.java`
  - `checkout/adapter/incoming/web/CheckoutPageController.java`
- **Patterns:** ViewModel Pattern, Page-Specific ViewModel
- **Constraints:**
  - ViewModels use primitives only
  - Each checkout step may have its own ViewModel
  - Controllers map Result -> ViewModel in adapter layer

---

## Epic: Enriched Domain Model Pattern

### US-124: Remove Interest Interface Pattern Infrastructure
**Epic:** enriched-domain-model-pattern
**Depends on:** -

**Description:** Remove StateInterest and ReadModelBuilder marker interfaces and all related Interest interface implementations. Replace with simpler factory method approach.

**Acceptance Criteria:**
- StateInterest marker interface removed from sharedkernel/marker/tactical/
- ReadModelBuilder marker interface removed from sharedkernel/marker/tactical/
- CartStateInterest and EnrichedCartStateInterest removed
- ProductStateInterest and EnrichedProductStateInterest removed
- CheckoutStateInterest removed
- EnrichedCartBuilder, EnrichedProductBuilder, CheckoutCartBuilder removed
- provideStateTo() methods removed from aggregates
- Package-info.java updated to remove references
- Architecture tests pass: ./gradlew test-architecture

**Architectural Guidance:**
- **Affected Layers:** domain
- **Locations:**
  - `sharedkernel/marker/tactical/StateInterest.java` (deleted)
  - `sharedkernel/marker/tactical/ReadModelBuilder.java` (deleted)
  - `cart/domain/model/CartStateInterest.java` (deleted)
  - `cart/domain/readmodel/EnrichedCartBuilder.java` (deleted)
  - `product/domain/model/ProductStateInterest.java` (deleted)
  - `product/domain/readmodel/EnrichedProductBuilder.java` (deleted)
- **Patterns:** Enriched Domain Model Pattern
- **Constraints:**
  - No breaking changes to use cases
  - Factory methods replace Interest Interface usage

---

### US-125: Add Factory Methods to Enriched Domain Models
**Epic:** enriched-domain-model-pattern
**Depends on:** US-124

**Description:** Add static factory methods to enriched domain models that combine aggregate state with external context data. Factory methods replace the builder pattern from Interest Interface.

**Acceptance Criteria:**
- EnrichedCart.from(ShoppingCart, Map<ProductId, CartArticle>) factory method
- EnrichedProduct.from(Product, ProductArticle) factory method
- CheckoutCartSnapshot.from(CheckoutSession) factory method
- Factory methods validate inputs (null checks)
- Use cases updated to use factory methods
- All tests pass: ./gradlew build

**Architectural Guidance:**
- **Affected Layers:** domain, application
- **Locations:**
  - `cart/domain/model/EnrichedCart.java`
  - `product/domain/model/EnrichedProduct.java`
  - `checkout/domain/readmodel/CheckoutCartSnapshot.java`
  - `cart/application/getcartbyid/GetCartByIdUseCase.java`
  - `cart/application/checkoutcart/CheckoutCartUseCase.java`
  - `product/application/getproductbyid/GetProductByIdUseCase.java`
- **Patterns:** Factory Method, Enriched Domain Model Pattern
- **Constraints:**
  - Factory methods are static
  - Factory methods reside on the enriched model class itself

---

### US-126: Add Business Logic to Enriched Domain Models
**Epic:** enriched-domain-model-pattern
**Depends on:** US-125

**Description:** Add business logic methods to enriched domain models for cross-context rules. Enriched models own business rules that require data from multiple bounded contexts.

**Acceptance Criteria:**
- EnrichedCart.isValidForCheckout() - checkout eligibility rule
- EnrichedCart.hasAnyPriceChanges() - price change detection
- EnrichedCart.calculateCurrentSubtotal() - calculation with current prices
- EnrichedCartItem.isValidForCheckout() - item-level eligibility
- EnrichedCartItem.hasPriceChanged() - price comparison
- EnrichedCartItem.hasSufficientStock() - stock check
- EnrichedProduct.canPurchase() - purchase eligibility
- EnrichedProduct.hasStockFor(quantity) - quantity stock check
- CheckoutCartUseCase uses enrichedCart.isValidForCheckout() for business decision
- All tests pass: ./gradlew build

**Architectural Guidance:**
- **Affected Layers:** domain, application
- **Locations:**
  - `cart/domain/model/EnrichedCart.java`
  - `cart/domain/model/EnrichedCartItem.java`
  - `product/domain/model/EnrichedProduct.java`
  - `cart/application/checkoutcart/CheckoutCartUseCase.java`
- **Patterns:** Enriched Domain Model Pattern, Rich Domain Model
- **Constraints:**
  - Aggregates own mutations, Enriched Models own cross-context rules
  - Business logic uses data from aggregate + external contexts
  - No anemic domain models

---

### US-127: Document Enriched Domain Model Pattern
**Epic:** enriched-domain-model-pattern
**Depends on:** US-126

**Description:** Update architecture documentation to describe Enriched Domain Model Pattern as the recommended approach. Move Interest Interface Pattern to advanced/optional section.

**Acceptance Criteria:**
- architecture-principles.md updated with Enriched Domain Model Pattern section
- Pattern documented as first-class domain concept, not just read model
- Responsibility split table: Aggregate owns mutations, Enriched Model owns cross-context rules
- Code examples show business logic in enriched models
- Interest Interface Pattern moved to Advanced section
- When to use each pattern clearly documented
- Table of contents updated
- Key Concepts in References section updated
- dto-vs-viewmodel-analysis.md references updated
- package-structure.md updated

**Architectural Guidance:**
- **Affected Layers:** documentation
- **Locations:**
  - `docs/architecture/architecture-principles.md`
  - `docs/architecture/dto-vs-viewmodel-analysis.md`
  - `docs/architecture/package-structure.md`
- **Patterns:** Enriched Domain Model Pattern
- **Constraints:**
  - Documentation matches actual code implementation
  - Clear guidance on when to use each pattern

---

### US-128: Architecture Alignment & Quality Fix Session
**Epic:** code-quality
**Depends on:** US-127

**Description:** Comprehensive alignment session to upgrade dependencies, add missing domain events across bounded contexts, fix and extend ArchUnit tests, overhaul architecture documentation, add OutputPort markers, and improve E2E test reliability.

**Acceptance Criteria:**
- Spring Boot upgraded from 3.5.6 to 3.5.10
- Spring AI upgraded from 1.1.0-M3 to 1.1.2 GA, milestone repository removed
- Spring Cloud upgraded from 2025.0.0 to 2025.1.1
- Domain events added to Product aggregate (ProductNameChanged, ProductDescriptionChanged, ProductCategoryChanged)
- Domain events added to Account aggregate (AccountClosed, AccountLoggedIn, AccountPasswordChanged, AccountReactivated, AccountSuspended)
- Domain events added to ShoppingCart aggregate (CartAbandoned, CartCompleted)
- Domain event added to StockLevel aggregate (StockReleased)
- PackageCyclesArchUnitTest rewritten with correct adapter.incoming/outgoing patterns
- Previously @Ignored ArchUnit tests resolved (DomainEvent/DomainService naming rules enforce marker interfaces)
- New ArchUnit rules added: OutputPort hierarchy, Enriched Model location, ViewModel location
- OutputPort marker interface added to TokenService, IdentitySession, RegisteredUserValidator
- ShoppingCartRepository Spring Data imports removed
- PageResult and PagingRequest added to shared kernel domain model
- 3 new ADRs created: ADR-021 (Enriched Domain Model), ADR-022 (ViewModel Pattern), ADR-023 (Optional Results)
- package-structure.md fully overhauled with correct paths and all 7 bounded contexts
- architecture-principles.md Interest Pattern section removed
- E2E tests: screenshot-on-failure added, hard-coded waits replaced with explicit waits
- Deprecated BaseArchUnitTest constants removed
- README.md updated to reflect all changes (ADR count, bounded context list)
- All architecture tests pass: `./gradlew test-architecture`

**Architectural Guidance:**
- **Affected Layers:** Domain, Application, Adapter, Infrastructure, Test, Documentation
- **Locations:**
  - `product/domain/event/ProductNameChanged, ProductDescriptionChanged, ProductCategoryChanged`
  - `account/domain/event/AccountClosed, AccountLoggedIn, AccountPasswordChanged, AccountReactivated, AccountSuspended`
  - `cart/domain/event/CartAbandoned, CartCompleted`
  - `inventory/domain/event/StockReleased`
  - `account/application/shared/TokenService, IdentitySession, RegisteredUserValidator`
  - `cart/application/shared/ShoppingCartRepository`
  - `sharedkernel/domain/model/PageResult, PagingRequest`
  - `src/test-architecture/groovy/ (all 8 ArchUnit test files)`
  - `docs/architecture/adr/adr-021, adr-022, adr-023`
  - `docs/architecture/package-structure.md`
  - `docs/architecture/architecture-principles.md`
  - `src/test-e2e/ (BaseE2ETest, page objects)`
- **Patterns:** Domain Events, Output Port, Enriched Domain Model, ViewModel Pattern, Architecture Testing
- **Constraints:**
  - Run `./gradlew test-architecture` to verify architectural compliance
  - No Spring annotations in domain layer
  - Domain events implement DomainEvent marker interface
  - Output ports extend OutputPort marker interface
  - Documentation matches actual code implementation

---
