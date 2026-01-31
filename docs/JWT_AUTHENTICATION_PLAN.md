# JWT Authentication Plan for ai_architecture_sample

> **Status: IMPLEMENTED** - All 6 phases completed on 2026-01-31

## Overview

Add JWT-based identity from the **first visit**. Every user (anonymous or registered) receives a JWT token that identifies them throughout their journey. No cart or session IDs in URLs - the JWT carries the identity.

## Key Concepts

### Soft Identity (Anonymous JWT)

When a user first visits the shop:
1. No JWT cookie → Generate **anonymous JWT** with a new `UserId`
2. Set JWT in HttpOnly cookie
3. All subsequent requests include this identity
4. Cart and checkout URLs become: `/cart`, `/checkout/buyer`, etc.

**Anonymous JWT claims:**
```json
{
  "sub": "anon-550e8400-e29b-41d4-a716-446655440000",
  "type": "anonymous",
  "iat": 1706688000,
  "exp": 1707292800
}
```

### Account Registration (Hard Login)

When an anonymous user registers:
1. Create Account with email + secure password (BCrypt)
2. **Link** the anonymous `UserId` to the new Account
3. Issue new JWT with `type: "registered"`
4. Cart remains associated (same UserId)

**Registered JWT claims:**
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "type": "registered",
  "email": "user@example.com",
  "roles": ["CUSTOMER"],
  "iat": 1706688000,
  "exp": 1707292800
}
```

### Account Login (Cart Recovery)

When a registered user logs in from a new device/browser:
1. Validate credentials
2. Issue JWT with their existing `UserId`
3. **Recover** their existing cart (if any)
4. Optionally merge with any anonymous cart items

## URL Design (No IDs)

| Before | After |
|--------|-------|
| `/cart/{cartId}` | `/cart` |
| `/checkout/{sessionId}/buyer` | `/checkout/buyer` |
| `/checkout/{sessionId}/delivery` | `/checkout/delivery` |
| `/api/carts/{cartId}` | `/api/carts/me` |
| `/api/checkout-sessions/{id}` | `/api/checkout-sessions/current` |

**UCP Compatibility**: REST API can still support explicit IDs for external integrations:
- `/api/checkout-sessions/{id}` - With Bearer token + ownership check

## Account Bounded Context

```
account/
├── domain/
│   ├── model/
│   │   ├── Account.java           # Aggregate root
│   │   ├── AccountId.java         # Account identity (different from UserId!)
│   │   ├── Email.java             # Value object with validation
│   │   ├── HashedPassword.java    # Value object (BCrypt hashed)
│   │   └── AccountStatus.java     # ACTIVE, SUSPENDED, etc.
│   └── event/
│       ├── AccountRegistered.java
│       └── AccountLinkedToIdentity.java
├── application/
│   ├── shared/
│   │   └── AccountRepository.java
│   ├── registeraccount/
│   │   ├── RegisterAccountInputPort.java
│   │   ├── RegisterAccountCommand.java  # email, password, currentUserId
│   │   ├── RegisterAccountResponse.java
│   │   └── RegisterAccountUseCase.java
│   ├── authenticateaccount/
│   │   ├── AuthenticateAccountInputPort.java
│   │   ├── AuthenticateAccountCommand.java  # email, password
│   │   ├── AuthenticateAccountResponse.java # returns UserId for JWT
│   │   └── AuthenticateAccountUseCase.java
│   └── linkidentity/
│       └── LinkIdentityToAccountUseCase.java  # Links anon UserId to Account
└── adapter/
    ├── incoming/
    │   ├── api/AuthResource.java
    │   └── web/LoginPageController.java
    └── outgoing/
        └── persistence/InMemoryAccountRepository.java
```

### Identity Model

**Key distinction:**
- `UserId` - The identity in JWT, used across Cart/Checkout contexts
- `AccountId` - Internal Account aggregate identity
- `CartId` - Unique identifier for each cart (still exists, not in URLs)
- `CheckoutSessionId` - Unique identifier for each checkout session (still exists, not in URLs)
- One Account can be linked to one UserId (1:1)
- Anonymous users have a UserId but no Account

### How UserId, CartId, and CheckoutSessionId Relate

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           UserId (JWT subject)                          │
│                   "550e8400-e29b-41d4-a716-446655440000"                │
│                        Shared across all contexts                       │
│                     (Anonymous or linked to Account)                    │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                    maps to (same value)
                                │
        ┌───────────────────────┴───────────────────────┐
        ▼                                               ▼
┌───────────────────────┐                   ┌───────────────────────┐
│    Cart Context       │                   │   Checkout Context    │
│    CustomerId         │                   │   CustomerId          │
│  (= UserId.value())   │                   │  (= UserId.value())   │
└───────────┬───────────┘                   └───────────┬───────────┘
            │                                           │
            │ 1:N (history)                             │ 1:N (history)
            │ but only 1 ACTIVE                         │ but only 1 ACTIVE
            ▼                                           ▼
┌───────────────────────┐      creates      ┌───────────────────────┐
│       Cart            │ ──────────────────►│   CheckoutSession     │
│    CartId (unique)    │        1:1        │ CheckoutSessionId     │
│   "cart-abc-123"      │                   │ "chk-xyz-789"         │
│   status: ACTIVE      │                   │ step: BUYER_INFO      │
└───────────────────────┘                   └───────────────────────┘
```

**Relationships explained:**
- `UserId` → `CustomerId`: **1:1 value mapping** (same UUID value, context-specific types)
- `CustomerId` → `Cart`: **1:N** (one user has many carts over time, only one `ACTIVE`)
- `CustomerId` → `CheckoutSession`: **1:N** (one user has many sessions over time, only one `ACTIVE`)
- `Cart` → `CheckoutSession`: **1:1** (one cart creates one checkout session)

**Why CartId/CheckoutSessionId still exist:**
- They're the aggregate identities (DDD principle)
- Used internally for repository lookups: `cartRepository.findById(cartId)`
- Used in domain events: `CartCheckedOut(cartId)`
- Used in REST API for UCP compatibility: `/api/checkout-sessions/{id}`
- **Just not exposed in MVC URLs** - we look up by UserId instead

**URL lookup flow:**
```
GET /cart
    │
    ▼
JwtFilter extracts UserId from cookie
    │
    ▼
Controller: identityProvider.getCurrentIdentity().userId()
    │
    ▼
UseCase: cartRepository.findActiveByCustomerId(CustomerId.of(userId.value()))
    │
    ▼
Returns Cart with its CartId (but CartId never shown to user)
```

## SharedKernel Security

```
sharedkernel/
├── domain/common/
│   └── UserId.java                      # Shared identity value object
└── application/port/security/
    ├── Identity.java                    # Current user's identity
    │   - userId: UserId
    │   - type: ANONYMOUS | REGISTERED
    │   - email: Optional<String>
    │   - roles: Set<String>
    ├── IdentityProvider.java            # Port to get current identity
    └── TokenService.java                # Port for token operations
```

## Infrastructure Security

```
infrastructure/security/
├── jwt/
│   ├── JwtProperties.java               # Config properties
│   ├── JwtTokenService.java             # Implements TokenService
│   │   - generateAnonymousToken()
│   │   - generateRegisteredToken(userId, email, roles)
│   │   - validateAndParse(token)
│   └── JwtAuthenticationFilter.java     # OncePerRequestFilter
│       - Extract JWT from cookie (or Authorization header)
│       - If no JWT → generate anonymous token, set cookie
│       - Validate token
│       - Set SecurityContext with Identity
├── password/
│   └── BCryptPasswordEncoder.java       # Password hashing
└── SpringSecurityIdentityProvider.java  # Implements IdentityProvider
```

## Password Security

**BCrypt with industry-standard settings:**
```java
// BCrypt with strength 12 (2^12 iterations = ~4096)
// OWASP recommends minimum strength 10
private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

// In HashedPassword value object
public record HashedPassword(String hash) {
    public static HashedPassword fromPlaintext(String plaintext, PasswordEncoder encoder) {
        // Validation: min 8 chars, complexity requirements
        validatePasswordStrength(plaintext);
        return new HashedPassword(encoder.encode(plaintext));
    }

    public boolean matches(String plaintext, PasswordEncoder encoder) {
        return encoder.matches(plaintext, hash);
    }
}
```

**Security measures:**
- BCrypt with cost factor 12 (industry standard)
- Passwords never stored in plaintext
- Passwords never logged or returned in responses
- Timing-safe comparison via BCrypt
- Password strength validation (min length, complexity)

## Implementation Phases

### Phase 1: SharedKernel Identity
**Create:**
- `sharedkernel/domain/common/UserId.java`
- `sharedkernel/application/port/security/Identity.java`
- `sharedkernel/application/port/security/IdentityProvider.java`
- `sharedkernel/application/port/security/TokenService.java`

### Phase 2: JWT Infrastructure
**Create:**
- `infrastructure/security/jwt/JwtProperties.java`
- `infrastructure/security/jwt/JwtTokenService.java`
- `infrastructure/security/jwt/JwtAuthenticationFilter.java`
- `infrastructure/security/SpringSecurityIdentityProvider.java`

**Modify:**
- `infrastructure/config/SecurityConfiguration.java`
- `build.gradle` - Add dependencies
- `application.yml` - Add JWT config

### Phase 3: Account Bounded Context
**Create:**
- Full account domain model
- Register/Authenticate use cases
- In-memory repository
- Auth REST + MVC controllers
- Login/Register templates

### Phase 4: Controller Migration (Cart)
**Modify `CartPageController.java`:**
```java
@GetMapping  // Was: @GetMapping("/{cartId}")
public String showCart(Model model) {
    UserId userId = identityProvider.getCurrentIdentity().userId();
    CustomerId customerId = CustomerId.of(userId.value());

    var cart = getOrCreateActiveCartUseCase.execute(
        new GetOrCreateActiveCartCommand(customerId.value()));
    // ...
}

@PostMapping("/add-product")
public String addProductToCart(...) {
    // No more HttpSession - use IdentityProvider
    UserId userId = identityProvider.getCurrentIdentity().userId();
    // ...
    return "redirect:/cart";  // No cart ID in URL
}
```

### Phase 5: Controller Migration (Checkout)
**Modify all checkout controllers:**
- Remove `{id}` path variable
- Use `IdentityProvider` to get current user
- Add `FindActiveCheckoutSessionUseCase` to lookup by userId

### Phase 6: Cart Recovery on Login
**Create `RecoverCartOnLoginUseCase`:**
- On login, find cart for the registered UserId
- If anonymous user had items, offer merge or replace

## Dependencies (build.gradle)

```groovy
// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'

// Security
implementation 'org.springframework.boot:spring-boot-starter-security'
```

## Configuration (application.yml)

```yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET:dev-only-secret-key-must-be-at-least-256-bits}
      anonymous-expiration-days: 30
      registered-expiration-days: 7
      issuer: ai-architecture-sample
      cookie-name: shop-identity
    password:
      bcrypt-strength: 12
      min-length: 8
```

## Verification

1. **Build**: `./gradlew build`
2. **Architecture tests**: `./gradlew test-architecture`
3. **Anonymous flow**:
   - Visit homepage → JWT cookie set automatically
   - Add to cart → `/cart` (no ID in URL)
   - Start checkout → `/checkout/buyer`
   - Complete checkout anonymously
4. **Registration flow**:
   - Add items to cart as anonymous
   - Register account
   - Cart preserved (same UserId)
   - JWT updated to `type: registered`
5. **Login recovery flow**:
   - Clear cookies (new browser)
   - Login with existing account
   - Previous cart recovered
6. **Password security**:
   - Verify passwords are BCrypt hashed in storage
   - Verify plaintext never appears in logs

## Critical Files Modified

| File | Location | Changes |
|------|----------|---------|
| `CartPageController.java` | `cart/adapter/incoming/web/` | Remove `{cartId}`, use IdentityProvider |
| `StartCheckoutPageController.java` | `checkout/adapter/incoming/web/` | Remove `{id}`, use IdentityProvider |
| `BuyerInfoPageController.java` | `checkout/adapter/incoming/web/` | Remove `{id}`, use IdentityProvider |
| `DeliveryPageController.java` | `checkout/adapter/incoming/web/` | Remove `{id}`, use IdentityProvider |
| `PaymentPageController.java` | `checkout/adapter/incoming/web/` | Remove `{id}`, use IdentityProvider |
| `ReviewPageController.java` | `checkout/adapter/incoming/web/` | Remove `{id}`, use IdentityProvider |
| `ConfirmationPageController.java` | `checkout/adapter/incoming/web/` | Remove `{id}`, use IdentityProvider |
| `SecurityConfiguration.java` | `infrastructure/config/` | Add JWT filter, cookie config |
| `build.gradle` | root | Add dependencies |
