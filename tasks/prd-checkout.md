# PRD: AI Architecture Sample - E-Commerce Application

## Overview

Reference implementation demonstrating Domain-Centric Architecture with DDD, Hexagonal Architecture, and Clean Architecture patterns.

> **Note:** Stories were renumbered in January 2026. US-1 through US-10 cover foundational/pre-checkout work. Original checkout stories (former US-1 through US-36) are now US-11 through US-46.

---

## Foundational Stories (US-1 through US-10)

### US-1: Initial Project Setup & Spring Boot Application
**As a** developer
**I want** a Spring Boot project with basic infrastructure
**So that** I can build the e-commerce sample application

**Acceptance Criteria:**
- Spring Boot 3.x project with Java 21
- Gradle build configuration with required dependencies
- Pug4j template engine integration
- Basic layout template with navigation
- Home page controller and template

---

### US-2: ArchUnit Architecture Tests
**As a** developer
**I want** automated architecture rule enforcement
**So that** the codebase maintains consistent architectural patterns

**Acceptance Criteria:**
- Separate test-architecture source set with Spock/Groovy
- Tests for hexagonal architecture (ports and adapters)
- Tests for onion architecture (layer dependencies)
- Tests for DDD tactical patterns (aggregates, entities, value objects)
- Tests for naming conventions

---

### US-3: Product Catalog Bounded Context
**As a** customer
**I want** to browse products in a catalog
**So that** I can find items to purchase

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
**As a** developer
**I want** a consistent use case pattern
**So that** application layer follows Clean Architecture principles

**Acceptance Criteria:**
- UseCase<INPUT, OUTPUT> base interface
- InputPort marker interface
- Use cases organized by bounded context
- Command objects for write operations
- Query objects for read operations

---

### US-5: Shared Kernel & DDD Marker Interfaces
**As a** developer
**I want** shared DDD building blocks
**So that** bounded contexts can use common patterns

**Acceptance Criteria:**
- AggregateRoot, Entity, Value marker interfaces
- DomainEvent and DomainService markers
- Repository and OutputPort base interfaces
- BaseAggregateRoot with event collection
- Shared value objects (CustomerId, Money)
- All in sharedkernel package

---

### US-6: Domain-Centric Architecture Refactoring
**As a** developer
**I want** a clear package structure
**So that** the codebase follows domain-centric architecture

**Acceptance Criteria:**
- Package structure: domain, application, adapter, infrastructure
- Adapters split into incoming (controllers) and outgoing (repositories)
- Spring application class in infrastructure layer
- Naming conventions documented and applied
- Event listeners moved to adapter layer

---

### US-7: Shopping Cart Bounded Context
**As a** customer
**I want** to add products to a shopping cart
**So that** I can purchase multiple items together

**Acceptance Criteria:**
- ShoppingCart aggregate with CartId, CustomerId, line items
- CartLineItem entity with product reference and quantity
- AddToCartUseCase for adding products
- CartRepository interface and implementation
- CartPageController for viewing cart
- Cart view template

---

### US-8: Cross-Context Integration Events
**As a** developer
**I want** event-driven communication between contexts
**So that** bounded contexts remain decoupled

**Acceptance Criteria:**
- DomainEvent interface for internal events
- IntegrationEvent for cross-context communication
- Event publishing infrastructure with Spring events
- Event consumers in adapter.incoming.event package
- ProductAddedToCart example integration

---

### US-9: In-Memory Repository Implementation
**As a** developer
**I want** thread-safe in-memory repositories
**So that** the application works without external databases

**Acceptance Criteria:**
- ConcurrentHashMap-based storage
- Thread-safe operations
- Secondary indexes for efficient lookups
- InMemoryProductRepository implementation
- InMemoryCartRepository implementation

---

### US-10: Specification Pattern
**As a** developer
**I want** a specification pattern implementation
**So that** business rules can be encapsulated and composed

**Acceptance Criteria:**
- Specification<T> interface with isSatisfiedBy method
- Composite specifications (and, or, not)
- Example specification implementations
- Repository support for specification queries

---

## Checkout Bounded Context Stories (US-11 through US-46)

### US-11: Domain Value Objects (Renamed from US-1)
**As a** developer
**I want** checkout domain value objects created
**So that** the checkout session can store buyer, delivery, and payment information

**Acceptance Criteria:**
- CheckoutSessionId value object with generate() and of() methods
- CheckoutStep enum (STARTED, BUYER_INFO, DELIVERY, PAYMENT, REVIEW, CONFIRMED, COMPLETED, ABANDONED, EXPIRED)
- BuyerInfo value object (email, firstName, lastName, phone)
- DeliveryAddress value object (recipientName, street, city, postalCode, country)
- ShippingOption value object (id, name, cost, estimatedDelivery)
- PaymentProviderId value object
- PaymentSelection value object (providerId, token, displayInfo)
- CheckoutTotals value object (subtotal, shipping, tax, total)
- CheckoutLineItemId and CheckoutLineItem value objects
- All implement Value interface with proper validation

---

### US-12: CheckoutSession Aggregate (Renamed from US-2)
**As a** developer
**I want** a CheckoutSession aggregate root
**So that** checkout state can be managed with proper invariants

**Acceptance Criteria:**
- Extends AggregateRoot<CheckoutSession, CheckoutSessionId>
- Factory method: start(cartId, customerId, lineItems, subtotal)
- Methods: submitBuyerInfo(), submitDelivery(), submitPayment(), confirm(), complete(), abandon(), expire()
- Step validation: cannot skip steps, can go back
- Expiration after 30 minutes
- Raises domain events for each step
- All tests pass

---

### US-13: Domain Events (Renamed from US-3)
**As a** developer
**I want** checkout domain events
**So that** other contexts can react to checkout state changes

**Acceptance Criteria:**
- CheckoutStarted event (sessionId, cartId, customerId)
- BuyerInfoSubmitted event (sessionId, email, fullName)
- DeliverySubmitted event (sessionId, recipientName, city, country, shippingOptionName)
- PaymentSubmitted event (sessionId, providerId)
- CheckoutConfirmed integration event (full order details for Cart context)
- All implement DomainEvent or IntegrationEvent interface

---

### US-14: Repository and Persistence (Renamed from US-4)
**As a** developer
**I want** checkout session repository
**So that** checkout sessions can be persisted and retrieved

**Acceptance Criteria:**
- CheckoutSessionRepository interface in application/shared
- Methods: save(), findById(), findByCartId(), findExpiredSessions()
- InMemoryCheckoutSessionRepository implementation in adapter/outgoing/persistence
- Follows existing repository patterns from Cart context

---

### US-15: Start Checkout Use Case (Renamed from US-5)
**As a** developer
**I want** a StartCheckout use case
**So that** customers can begin the checkout process

**Acceptance Criteria:**
- StartCheckoutInputPort extends UseCase<StartCheckoutCommand, StartCheckoutResponse>
- StartCheckoutCommand(cartId)
- StartCheckoutResponse(sessionId, redirectUrl)
- StartCheckoutUseCase implementation
- Loads cart, creates checkout session, saves session
- Returns session ID for redirect

---

### US-16: Submit Buyer Info Use Case (Renamed from US-6)
**As a** developer
**I want** a SubmitBuyerInfo use case
**So that** customers can provide their contact information

**Acceptance Criteria:**
- SubmitBuyerInfoInputPort extends UseCase
- SubmitBuyerInfoCommand(sessionId, email, firstName, lastName, phone)
- SubmitBuyerInfoResponse(success, nextStep)
- Validates session exists and is in correct step
- Updates session with buyer info

---

### US-17: Submit Delivery Use Case (Renamed from US-7)
**As a** developer
**I want** a SubmitDelivery use case
**So that** customers can provide delivery address and shipping option

**Acceptance Criteria:**
- SubmitDeliveryInputPort extends UseCase
- SubmitDeliveryCommand(sessionId, recipientName, street, city, postalCode, country, shippingOptionId)
- SubmitDeliveryResponse(success, nextStep, updatedTotals)
- Validates session and step
- Updates session with delivery info

---

### US-18: Get Shipping Options Use Case (Renamed from US-8)
**As a** developer
**I want** a GetShippingOptions use case
**So that** customers can see available shipping methods

**Acceptance Criteria:**
- GetShippingOptionsInputPort extends UseCase
- GetShippingOptionsQuery()
- GetShippingOptionsResponse(list of ShippingOptionDto)
- Returns hardcoded shipping options for demo

---

### US-19: Payment Provider System (Renamed from US-9)
**As a** developer
**I want** a payment provider plugin architecture
**So that** multiple payment methods can be supported

**Acceptance Criteria:**
- PaymentProvider output port interface (providerId, displayName, iconUrl, authorize)
- PaymentProviderRegistry output port interface (getAvailableProviders, getProvider)
- MockPaymentProvider adapter implementation (always succeeds)
- InMemoryPaymentProviderRegistry adapter implementation
- PaymentResult record (success, authorizationId, errorMessage)

---

### US-20: Submit Payment Use Case (Renamed from US-10)
**As a** developer
**I want** a SubmitPayment use case
**So that** customers can select and authorize payment

**Acceptance Criteria:**
- SubmitPaymentInputPort extends UseCase
- SubmitPaymentCommand(sessionId, providerId, token)
- SubmitPaymentResponse(success, nextStep)
- Validates provider exists
- Updates session with payment selection

---

### US-21: Get Payment Providers Use Case (Renamed from US-11)
**As a** developer
**I want** a GetPaymentProviders use case
**So that** customers can see available payment methods

**Acceptance Criteria:**
- GetPaymentProvidersInputPort extends UseCase
- GetPaymentProvidersQuery()
- GetPaymentProvidersResponse(list of PaymentProviderDto)
- Uses PaymentProviderRegistry to get available providers

---

### US-22: Get Checkout Session Use Case (Renamed from US-12)
**As a** developer
**I want** a GetCheckoutSession use case
**So that** controllers can load session data for display

**Acceptance Criteria:**
- GetCheckoutSessionInputPort extends UseCase
- GetCheckoutSessionQuery(sessionId)
- GetCheckoutSessionResponse with all session data (found, currentStep, buyerInfo, delivery, payment, lineItems, totals)
- Returns found=false if session doesn't exist

---

### US-23: Confirm Checkout Use Case (Renamed from US-13)
**As a** developer
**I want** a ConfirmCheckout use case
**So that** customers can complete their order

**Acceptance Criteria:**
- ConfirmCheckoutInputPort extends UseCase
- ConfirmCheckoutCommand(sessionId)
- ConfirmCheckoutResponse(success, orderId)
- Validates all required info is present
- Marks session as confirmed
- Publishes CheckoutConfirmed event

---

### US-24: Checkout Step Validator (Renamed from US-14)
**As a** developer
**I want** a step validator component
**So that** controllers can enforce step navigation rules

**Acceptance Criteria:**
- CheckoutStepValidator component in adapter/incoming/web
- validateStepAccess(sessionId, requestedStep) returns Optional<String> redirect URL
- Invalid session -> redirect to /cart
- Skipping ahead -> redirect to current valid step
- Going back -> always allowed
- Terminal states -> redirect to confirmation

---

### US-25: Start Checkout Controller (Renamed from US-15)
**As a** developer
**I want** a start checkout controller
**So that** customers can initiate checkout from cart

**Acceptance Criteria:**
- StartCheckoutPageController at /checkout/start
- GET with cartId parameter
- Calls StartCheckoutUseCase
- Redirects to /checkout/{id}/buyer

---

### US-26: Buyer Info Controller (Renamed from US-16)
**As a** developer
**I want** a buyer info page controller
**So that** customers can enter their contact information

**Acceptance Criteria:**
- BuyerInfoPageController at /checkout/{id}/buyer
- GET: validates step, loads session, returns buyer.pug template
- POST: validates step, calls SubmitBuyerInfoUseCase, redirects to delivery
- Form fields: email, firstName, lastName, phone (optional)

---

### US-27: Delivery Controller (Renamed from US-17)
**As a** developer
**I want** a delivery page controller
**So that** customers can enter shipping address

**Acceptance Criteria:**
- DeliveryPageController at /checkout/{id}/delivery
- GET: validates step, loads session and shipping options, returns delivery.pug
- POST: validates step, calls SubmitDeliveryUseCase, redirects to payment
- Form fields: recipientName, street, city, postalCode, country, shippingOptionId

---

### US-28: Payment Controller (Renamed from US-18)
**As a** developer
**I want** a payment page controller
**So that** customers can select payment method

**Acceptance Criteria:**
- PaymentPageController at /checkout/{id}/payment
- GET: validates step, loads session and payment providers, returns payment.pug
- POST: validates step, calls SubmitPaymentUseCase, redirects to review
- Form fields: providerId, paymentToken

---

### US-29: Review Controller (Renamed from US-19)
**As a** developer
**I want** a review page controller
**So that** customers can verify their order

**Acceptance Criteria:**
- ReviewPageController at /checkout/{id}/review
- GET: validates step, loads complete session data, returns review.pug
- Shows: line items, buyer info, delivery address, shipping option, payment method (masked), totals
- Edit links to previous steps

---

### US-30: Confirmation Controller (Renamed from US-20)
**As a** developer
**I want** a confirmation page controller
**So that** customers can confirm and see order completion

**Acceptance Criteria:**
- ConfirmationPageController
- POST /checkout/{id}/confirm: calls ConfirmCheckoutUseCase, redirects to confirmation page
- GET /checkout/{id}/confirmation: shows thank you page with order summary
- Only accessible after confirmation

---

### US-31: Checkout Templates (Renamed from US-21)
**As a** developer
**I want** Pug templates for checkout pages
**So that** customers have a UI for the checkout flow

**Acceptance Criteria:**
- buyer.pug: contact form with email, name fields
- delivery.pug: address form with shipping option selection
- payment.pug: payment provider selection
- review.pug: order summary with edit links
- confirmation.pug: thank you page
- All extend layout, show step progress, include order summary sidebar

---

### US-32: Cart Integration (Renamed from US-22)
**As a** developer
**I want** checkout integrated with cart
**So that** customers can start checkout from cart

**Acceptance Criteria:**
- "Checkout" button on cart page linking to /checkout/start?cartId={cartId}
- CheckoutEventConsumer in cart context listens for CheckoutConfirmed
- Calls existing CheckoutCartUseCase to mark cart as CHECKED_OUT
- Cart status updates after checkout completion

---

### US-33: Architecture Tests Pass (Renamed from US-23)
**As a** developer
**I want** all architecture tests to pass
**So that** the checkout context follows project patterns

**Acceptance Criteria:**
- ./gradlew test-architecture passes
- Checkout context has no direct imports from Cart domain (uses shared kernel)
- Domain layer has no Spring annotations
- All use cases follow InputPort pattern
- Events use DTOs, not domain objects

---

### US-34: Build and Manual Test (Renamed from US-24)
**As a** developer
**I want** the complete checkout flow working
**So that** we can verify end-to-end functionality

**Acceptance Criteria:**
- ./gradlew build passes
- Application starts with ./gradlew bootRun
- Can add products to cart
- Can click "Checkout" button
- Can complete full flow: Buyer -> Delivery -> Payment -> Review -> Confirm
- Confirmation page shows order details
- Cart status is CHECKED_OUT after completion

---

### US-35: Connect Cart Checkout Button to Checkout Flow (Renamed from US-25)
**As a** customer
**I want** the cart checkout button to start the real checkout flow
**So that** I can complete my purchase through the multi-step checkout process

**Acceptance Criteria:**
- Cart page checkout button redirects to /checkout/start?cartId={cartId} instead of /cart/{cartId}/checkout
- Old /cart/{cartId}/checkout endpoint is removed or deprecated
- Checkout flow can be started from the cart page
- Architecture tests pass

---

### US-36: Reduce Product Availability on Checkout (Renamed from US-26)
**As a** developer
**I want** product availability reduced when checkout is confirmed
**So that** inventory reflects purchased quantities

**Acceptance Criteria:**
- Product context has a CheckoutConfirmedEventListener that listens for CheckoutConfirmed events
- For each line item in the checkout, product availability is reduced by the purchased quantity
- Uses existing Product aggregate's reduceAvailability() method
- Architecture tests pass (no direct coupling between Checkout and Product domains)

**Architectural Guidance:**
- **Affected Layers:** Adapter
- **Locations:**
  - `product.adapter.incoming.event.CheckoutConfirmedEventListener`
- **Patterns:** Event Consumer, Cross-Context Integration
- **Constraints:**
  - Event listener in `product.adapter.incoming.event` package
  - No direct imports from `checkout.domain` - only checkout integration events
  - Call ProductRepository via application layer or directly (simple case)
  - Use `@EventListener` or `@TransactionalEventListener(phase = AFTER_COMMIT)`
  - Run `./gradlew test-architecture` to verify

---

### US-37: Remove Deprecated CartCheckedOut Availability Logic (Renamed from US-27)
**As a** developer
**I want** the old CartCheckedOut availability reduction logic removed
**So that** there is no duplicate/dead code for inventory management

**Acceptance Criteria:**
- CartCheckedOutEventListener in product context is removed (if exists)
- Any CartCheckedOut event handling for availability reduction is removed
- CartCheckedOut event itself remains if still used for cart status updates
- Build and architecture tests pass
- Product availability is only reduced via CheckoutConfirmed (US-36)

**Architectural Guidance:**
- **Affected Layers:** Adapter
- **Locations:**
  - `product.adapter.incoming.event.CartCheckedOutEventListener` (DELETE)
- **Patterns:** Code Removal, Dead Code Cleanup
- **Constraints:**
  - Search for CartCheckedOut references in product context
  - Verify event is not used elsewhere before removing listener
  - Keep CartCheckedOut event if cart context still publishes it for other purposes
  - Run `./gradlew test-architecture` after removal
  - Run `./gradlew build` to ensure no compilation errors

---

### US-38: Handle Missing Account After Application Restart (Renamed from US-28)
**As a** user
**I want** to be treated as anonymous when my account is lost after restart
**So that** I can re-register or login without errors

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

### US-39: E2E Test Infrastructure with Playwright (Renamed from US-29)
**As a** developer
**I want** end-to-end testing infrastructure
**So that** critical user flows can be verified automatically

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

### US-40: Data-Test Attributes for E2E Testing (Renamed from US-30)
**As a** developer
**I want** stable selectors for E2E tests
**So that** tests are decoupled from CSS classes and DOM structure

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

### US-41: Page Object Pattern for E2E Tests (Renamed from US-31)
**As a** developer
**I want** Page Object pattern for E2E tests
**So that** tests are maintainable and readable

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

### US-42: JWT Authentication Infrastructure (Renamed from US-32)
**As a** developer
**I want** JWT-based authentication
**So that** users have persistent identity across sessions

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

### US-43: Account Bounded Context with Registration and Login (Renamed from US-33)
**As a** customer
**I want** to create an account and login
**So that** my cart persists across sessions

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

### US-44: Migrate Controllers to JWT Identity (Renamed from US-34)
**As a** developer
**I want** controllers to use JWT identity
**So that** user identification is consistent

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

### US-45: Recover Cart on Login (Renamed from US-35)
**As a** customer
**I want** my cart recovered when I login
**So that** I don't lose items added while anonymous

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

### US-46: Checkout Login/Register Options on Buyer Page (Renamed from US-36)
**As a** customer
**I want** login/register options during checkout
**So that** I can create an account while purchasing

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
**As a** customer
**I want** my cart and checkout session preserved when I register during checkout
**So that** I don't lose my items and checkout progress

**Acceptance Criteria:**
- Anonymous tokens use raw UUID without 'anon-' prefix
- UserId remains unchanged after registration
- Cart items preserved after registration during checkout
- Checkout session preserved after registration during checkout
- User returns to checkout flow after registration without losing progress
- Architecture tests pass

**Architectural Guidance:**
- **Affected Layers:** Infrastructure, Domain, Shared Kernel
- **Locations:**
  - `infrastructure.security.jwt.JwtTokenService`
  - `sharedkernel.domain.common.UserId`
  - `account.domain.model.Account`
- **Patterns:** Identity Continuity
- **Constraints:**
  - Anonymous tokens must use raw UUID format (no prefix)
  - UserId must be identical before and after registration
  - Update `UserId.isAnonymous()` to use IdentityType from JWT instead of prefix
  - Remove UserId transformation in `Account.register()`
  - Run `./gradlew test-architecture` to verify

---

### US-48: Cart Merge Options on Login During Checkout
**As a** customer
**I want** to choose what happens to my carts when I log in with items in both my anonymous cart and account cart
**So that** I don't unexpectedly lose items from either cart

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
- **Affected Layers:** Domain, Application, Adapter
- **Locations:**
  - `cart.domain.model.ShoppingCart` (merge method)
  - `cart.application.mergecarts/MergeCartsUseCase`
  - `cart.application.mergecarts/MergeCartsInputPort`
  - `cart.application.mergecarts/MergeCartsCommand`
  - `cart.application.mergecarts/MergeCartsResponse`
  - `cart.application.getcartmergeoptions/GetCartMergeOptionsUseCase`
  - `cart.adapter.incoming.web.CartMergePageController`
  - `account.adapter.incoming.web.LoginPageController` (redirect logic)
  - `templates/cart/merge-options.pug`
- **Patterns:** Use Case Pattern, Domain Logic in Aggregate, Progressive Authentication
- **Constraints:**
  - Merge logic belongs in `ShoppingCart` aggregate
  - Use case orchestrates loading both carts and calling merge
  - No direct cart manipulation in controllers
  - Controller redirects to merge page when conflict detected
  - Run `./gradlew test-architecture` to verify

---

### US-49: Cart Merge Test Coverage
**As a** developer
**I want** comprehensive test coverage for cart merge functionality
**So that** I can confidently refactor and extend the feature

**Acceptance Criteria:**
- Unit tests for `ShoppingCart.merge()` covering: same product quantity combination, different products addition, empty cart handling
- Integration tests for `MergeCartsUseCase` covering all 3 strategies: merge, use-account, use-anonymous
- Integration tests verify correct cart deletion based on strategy
- One E2E test verifying the full flow: login with both carts having items, see merge options, select merge, verify combined cart
- All tests pass: `./gradlew test`, `./gradlew test-e2e`

**Architectural Guidance:**
- **Affected Layers:** Domain, Application, test-e2e
- **Locations:**
  - `src/test/java/.../cart/domain/model/ShoppingCartTest.java`
  - `src/test/java/.../cart/application/mergecarts/MergeCartsUseCaseTest.java`
  - `src/test-e2e/java/.../e2e/CartMergeE2ETest.java`
  - `src/test-e2e/java/.../e2e/pages/CartMergePage.java`
- **Patterns:** Layered Testing, Page Object Pattern, Unit Test Isolation
- **Constraints:**
  - Unit tests must not depend on Spring context
  - Integration tests use `@SpringBootTest` with test slices where appropriate
  - E2E test uses Page Object pattern for `CartMergePage`
  - E2E test covers only one happy path scenario (merge strategy)
  - Edge cases and error scenarios covered by unit/integration tests

---

## Goals
- 5-step checkout flow (Buyer Info -> Delivery -> Payment -> Review -> Confirmation)
- Guest checkout (no account required)
- Plugin architecture for payment providers
- Step validation with automatic redirects

## Non-Goals
- Actual payment processing (mock provider only)
- Order management after checkout
- UCP REST API implementation
