# PRD: Checkout Bounded Context

## Overview
Create a new **Checkout** bounded context with a multi-step checkout flow supporting guest checkout and multiple payment providers.

## Goals
- 5-step checkout flow (Buyer Info → Delivery → Payment → Review → Confirmation)
- Guest checkout (no account required)
- Plugin architecture for payment providers
- Step validation with automatic redirects

## Non-Goals
- User authentication/registration
- Actual payment processing (mock provider only)
- Order management after checkout
- UCP REST API implementation

---

## User Stories

### US-1: Domain Value Objects
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

### US-2: CheckoutSession Aggregate
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

### US-3: Domain Events
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

### US-4: Repository and Persistence
**As a** developer
**I want** checkout session repository
**So that** checkout sessions can be persisted and retrieved

**Acceptance Criteria:**
- CheckoutSessionRepository interface in application/shared
- Methods: save(), findById(), findByCartId(), findExpiredSessions()
- InMemoryCheckoutSessionRepository implementation in adapter/outgoing/persistence
- Follows existing repository patterns from Cart context

---

### US-5: Start Checkout Use Case
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

### US-6: Submit Buyer Info Use Case
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

### US-7: Submit Delivery Use Case
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

### US-8: Get Shipping Options Use Case
**As a** developer
**I want** a GetShippingOptions use case
**So that** customers can see available shipping methods

**Acceptance Criteria:**
- GetShippingOptionsInputPort extends UseCase
- GetShippingOptionsQuery()
- GetShippingOptionsResponse(list of ShippingOptionDto)
- Returns hardcoded shipping options for demo

---

### US-9: Payment Provider System
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

### US-10: Submit Payment Use Case
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

### US-11: Get Payment Providers Use Case
**As a** developer
**I want** a GetPaymentProviders use case
**So that** customers can see available payment methods

**Acceptance Criteria:**
- GetPaymentProvidersInputPort extends UseCase
- GetPaymentProvidersQuery()
- GetPaymentProvidersResponse(list of PaymentProviderDto)
- Uses PaymentProviderRegistry to get available providers

---

### US-12: Get Checkout Session Use Case
**As a** developer
**I want** a GetCheckoutSession use case
**So that** controllers can load session data for display

**Acceptance Criteria:**
- GetCheckoutSessionInputPort extends UseCase
- GetCheckoutSessionQuery(sessionId)
- GetCheckoutSessionResponse with all session data (found, currentStep, buyerInfo, delivery, payment, lineItems, totals)
- Returns found=false if session doesn't exist

---

### US-13: Confirm Checkout Use Case
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

### US-14: Checkout Step Validator
**As a** developer
**I want** a step validator component
**So that** controllers can enforce step navigation rules

**Acceptance Criteria:**
- CheckoutStepValidator component in adapter/incoming/web
- validateStepAccess(sessionId, requestedStep) returns Optional<String> redirect URL
- Invalid session → redirect to /cart
- Skipping ahead → redirect to current valid step
- Going back → always allowed
- Terminal states → redirect to confirmation

---

### US-15: Start Checkout Controller
**As a** developer
**I want** a start checkout controller
**So that** customers can initiate checkout from cart

**Acceptance Criteria:**
- StartCheckoutPageController at /checkout/start
- GET with cartId parameter
- Calls StartCheckoutUseCase
- Redirects to /checkout/{id}/buyer

---

### US-16: Buyer Info Controller
**As a** developer
**I want** a buyer info page controller
**So that** customers can enter their contact information

**Acceptance Criteria:**
- BuyerInfoPageController at /checkout/{id}/buyer
- GET: validates step, loads session, returns buyer.pug template
- POST: validates step, calls SubmitBuyerInfoUseCase, redirects to delivery
- Form fields: email, firstName, lastName, phone (optional)

---

### US-17: Delivery Controller
**As a** developer
**I want** a delivery page controller
**So that** customers can enter shipping address

**Acceptance Criteria:**
- DeliveryPageController at /checkout/{id}/delivery
- GET: validates step, loads session and shipping options, returns delivery.pug
- POST: validates step, calls SubmitDeliveryUseCase, redirects to payment
- Form fields: recipientName, street, city, postalCode, country, shippingOptionId

---

### US-18: Payment Controller
**As a** developer
**I want** a payment page controller
**So that** customers can select payment method

**Acceptance Criteria:**
- PaymentPageController at /checkout/{id}/payment
- GET: validates step, loads session and payment providers, returns payment.pug
- POST: validates step, calls SubmitPaymentUseCase, redirects to review
- Form fields: providerId, paymentToken

---

### US-19: Review Controller
**As a** developer
**I want** a review page controller
**So that** customers can verify their order

**Acceptance Criteria:**
- ReviewPageController at /checkout/{id}/review
- GET: validates step, loads complete session data, returns review.pug
- Shows: line items, buyer info, delivery address, shipping option, payment method (masked), totals
- Edit links to previous steps

---

### US-20: Confirmation Controller
**As a** developer
**I want** a confirmation page controller
**So that** customers can confirm and see order completion

**Acceptance Criteria:**
- ConfirmationPageController
- POST /checkout/{id}/confirm: calls ConfirmCheckoutUseCase, redirects to confirmation page
- GET /checkout/{id}/confirmation: shows thank you page with order summary
- Only accessible after confirmation

---

### US-21: Checkout Templates
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

### US-22: Cart Integration
**As a** developer
**I want** checkout integrated with cart
**So that** customers can start checkout from cart

**Acceptance Criteria:**
- "Checkout" button on cart page linking to /checkout/start?cartId={cartId}
- CheckoutEventConsumer in cart context listens for CheckoutConfirmed
- Calls existing CheckoutCartUseCase to mark cart as CHECKED_OUT
- Cart status updates after checkout completion

---

### US-23: Architecture Tests Pass
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

### US-24: Build and Manual Test
**As a** developer
**I want** the complete checkout flow working
**So that** we can verify end-to-end functionality

**Acceptance Criteria:**
- ./gradlew build passes
- Application starts with ./gradlew bootRun
- Can add products to cart
- Can click "Checkout" button
- Can complete full flow: Buyer → Delivery → Payment → Review → Confirm
- Confirmation page shows order details
- Cart status is CHECKED_OUT after completion

---

### US-25: Connect Cart Checkout Button to Checkout Flow
**As a** customer
**I want** the cart checkout button to start the real checkout flow
**So that** I can complete my purchase through the multi-step checkout process

**Acceptance Criteria:**
- Cart page checkout button redirects to /checkout/start?cartId={cartId} instead of /cart/{cartId}/checkout
- Old /cart/{cartId}/checkout endpoint is removed or deprecated
- Checkout flow can be started from the cart page
- Architecture tests pass

---

### US-26: Reduce Product Availability on Checkout
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

### US-27: Remove Deprecated CartCheckedOut Availability Logic
**As a** developer
**I want** the old CartCheckedOut availability reduction logic removed
**So that** there is no duplicate/dead code for inventory management

**Acceptance Criteria:**
- CartCheckedOutEventListener in product context is removed (if exists)
- Any CartCheckedOut event handling for availability reduction is removed
- CartCheckedOut event itself remains if still used for cart status updates
- Build and architecture tests pass
- Product availability is only reduced via CheckoutConfirmed (US-26)

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
