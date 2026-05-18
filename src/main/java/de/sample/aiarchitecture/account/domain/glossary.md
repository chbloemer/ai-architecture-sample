# Glossary — Bounded Context: Account

> **Bootstrap draft** — Automatically extracted from the current domain code.
> Terms reflect the as-is implementation, not necessarily the target Ubiquitous Language.
> Conflicts and polysemy are marked under "Notes" and should be resolved with the business
> stakeholder before this draft is adopted as authoritative.

Sources: `de.sample.aiarchitecture.account.domain.{model,event,gateway}`.

---

## Aggregate Roots

### Account

**Definition:** Registered user account with credentials and profile. Links a cross-context
identity (`UserId`) to a context-local aggregate identity (`AccountId`) and encapsulates
authentication, roles, and the lifecycle (active, suspended, closed).

**Type:** Aggregate Root

**Identity:** `AccountId`

**Related terms:** `Email`, `HashedPassword`, `AccountStatus`, `UserId`, `PasswordHasher`,
`Role` (planned).

**Operations:** `register`, `reconstitute`, `checkPassword`, `recordLogin`, `changePassword`,
`suspend`, `reactivate`, `close`, `addRole`, `removeRole`.

**Notes:** `roles` is currently a `Set<String>`. Planned cleanup: introduce a `Role` Value Object
(instead of String) to enforce valid values and improve domain vocabulary. On registration the
`UserId` from the JWT is preserved unchanged to guarantee cart/checkout continuity across the
guest → account transition.

---

## Value Objects

### Email

**Definition:** Validated, normalized (lower-cased) email address that also serves as the unique
login credential.

**Type:** Value Object

**Operations:** `of`, `localPart`, `domain`.

### HashedPassword

**Definition:** Securely hashed password; encapsulates the hash value, strength validation
against minimum requirements, and comparison operations. Never stores plaintext and delegates
the actual cryptographic operation to the `PasswordHasher` domain gateway.

**Type:** Value Object

**Related terms:** `PasswordHasher` (Domain Gateway in `account.domain.gateway`).

**Operations:** `fromPlaintext`, `of`, `fromHash`, `matches`, `validatePasswordStrength`.

**Notes:** `toString()` is deliberately masked. Password rules (minimum length, upper/lower-case,
digit) are encoded here — when changing them, consider adding a "Password Policy" entry to the
Ubiquitous Language.

### AccountId

**Definition:** Aggregate-internal identifier of an account. Distinct from `UserId`; an account
has exactly one `AccountId` and is linked to exactly one `UserId`.

**Type:** Value Object (ID)

**Operations:** `of`, `generate`.

### AccountStatus

**Definition:** Lifecycle status of an account: `ACTIVE` (can log in), `SUSPENDED` (temporarily
blocked), `CLOSED` (permanently ended).

**Type:** Value Object (Enum)

**Operations:** `canLogin`, `isTerminal`.

### Role (planned)

**Definition:** Planned Value Object for a business role of the account (currently modelled as
`String` in `Account.roles`, e.g. `CUSTOMER`).

**Type:** Value Object (planned)

**Synonyms (avoid):** raw `String` role.

**Notes:** DCA review recommendation: migrate `Set<String> roles` → `Set<Role>` to encapsulate
permitted values and rule out typos at compile time.

### Token (planned)

**Definition:** Planned Value Object for authentication/refresh tokens that today flow through
the adapters implicitly as strings.

**Type:** Value Object (planned)

**Notes:** Not currently present in the domain model. If tokens have business meaning in the
Account context (e.g. refresh-token rotation, password-reset token), model them as a VO.

### UserId

**Definition:** Cross-context user identity (Shared Kernel) that uniformly identifies anonymous
and registered users and is carried in the JWT.

**Type:** Value Object (ID, Shared Kernel)

**Related terms:** `Account`, `AccountId`, `AccountRegistered`, `AccountLinkedToIdentity`.

**Notes:** Lives in the `sharedkernel` and is consumed here. Resolve the polysemy with
`CustomerId` in the `checkout`/`cart` contexts — often semantically identical, formally
separate.

---

## Domain Events

### AccountRegistered

**Definition:** A new account has been registered; contains `AccountId`, `Email`, and the
linked `UserId`.

**Type:** Domain Event

**Related terms:** `Account`, `Email`, `UserId`.

### AccountLinkedToIdentity

**Definition:** The cross-context `UserId` has been linked to an `AccountId` — signals to other
contexts that the `UserId` now belongs to a registered user (e.g. for cart takeover).

**Type:** Domain Event

**Related terms:** `Account`, `UserId`.

### AccountLoggedIn

**Definition:** A user has successfully logged in to their account.

**Type:** Domain Event

### AccountPasswordChanged

**Definition:** The password of an account has been changed.

**Type:** Domain Event

### AccountSuspended

**Definition:** An account has been temporarily blocked and can no longer log in.

**Type:** Domain Event

### AccountReactivated

**Definition:** A previously suspended account has been re-enabled.

**Type:** Domain Event

### AccountClosed

**Definition:** An account has been permanently closed; terminal state of the lifecycle.

**Type:** Domain Event

---

## Domain Gateways

### PasswordHasher

**Definition:** Domain gateway for hashing plaintext passwords and timing-safe comparison
against a stored hash. The interface belongs to the domain (`account.domain.gateway`) and is
invoked by the `Account` aggregate as well as `HashedPassword` when a rule requires hashed
credentials. The technical implementation (BCrypt/Argon2/...) lives in the adapter.

**Type:** Domain Gateway (extends `DomainGateway`)

**Location:** `de.sample.aiarchitecture.account.domain.gateway.PasswordHasher`

**Implementation:** `SpringSecurityPasswordHasher` in `account.adapter.outgoing.security`
(BCrypt via Spring Security).

**Related terms:** `HashedPassword`, `Account`.

**Operations:** `hash(String) → String`, `matches(String, String) → boolean`.

**Notes:** Follows Vernon (IDDD `iddd_identityaccess` sample, where the `User` aggregate calls
`EncryptionService` via `DomainRegistry`). The service locator is replaced by a typed parameter
that the use case injects into the aggregate. Classification of the *service* remains
Infrastructure (Vernon, IDDD Ch. 7); classification of the *interface* is Domain Gateway,
because it is consumed by domain code and expressed in domain language.

---

## Open issues from DCA review

- **`Role` as a VO instead of `String`** — replace `Account.roles : Set<String>` with
  `Set<Role>`.
- **Introduce `Token` as a VO** if tokens have business relevance (e.g. refresh,
  password reset).
- **Polysemy `CustomerId` (cart, checkout) vs `UserId` (sharedkernel) vs `AccountId`** —
  decide authoritatively which identity is referenced in which context.
- **Password policy** should be extracted as a standalone domain specification rather than
  being buried in `HashedPassword.validatePasswordStrength`.
