# Abandoned Cart Specification — Plan and Execution Log

Goal: Implement a DDD Specification that both governs business rules and drives DB-side filtering (SQL pushdown) for the use case "Find carts eligible for an Abandoned‑Cart reminder".

Scope of this initiative
- Introduce and use a reusable, framework-agnostic Specification infrastructure in the shared kernel.
- Express abandoned‑cart rules as domain specifications that can be evaluated in‑memory and translated to DB predicates by the JPA adapter.
- Prove pushdown via a dedicated integration test suite.

## Checklist

1. Document the use case and architecture approach in this plan. [✓]
2. Add shared-kernel Specification infrastructure (domain-only, no JPA types). [✓]
   - `Specification<T>` with `isSatisfiedBy` and `accept(SpecificationVisitor)` [✓]
   - `SpecificationVisitor<T,R>` [✓]
   - Combinators: `AndSpecification<T>`, `OrSpecification<T>`, `NotSpecification<T>` [✓]
3. Capture adapter translation strategy (visitor mapping to JPA/Criteria) in this plan. [✓]
4. Define cart-specific leaf specs in the cart domain. [✓]
   - `ActiveCart` [✓]
   - `LastUpdatedBefore` [✓]
   - `HasMinTotal` [✓]
   - `HasAnyAvailableItem` [ ] (planned – depends on product availability read model)
   - `CustomerAllowsMarketing` [ ] (planned – depends on customer/consent read model)
5. Extend `ShoppingCartRepository` and JPA adapter to accept/translate domain specs with pagination. [✓]
   - Port method `findBy(DomainSpecification<ShoppingCart>, Pageable)` with in‑memory fallback [✓]
   - `CartJpaRepository` extends `JpaSpecificationExecutor<CartEntity>` [✓]
   - `JpaShoppingCartRepository.findBy(...)` translates via `CartSpecToJpa` and pages at DB level [✓]
6. Tests. [partial]
   - Integration: repository IT proving SQL pushdown + pagination [✓]
   - Unit: spec algebra (AND/OR/NOT) and leaf behavior [ ]
7. Test infrastructure: Introduce `test-integration` source set and migrate JPA repository IT into it. [✓]
8. Smoke verification: Keep at least one simple JUnit 5 unit test to ensure unit pipeline runs. [✓]

## Use case summary
We need to page through carts that satisfy all of the following (composable rules):
- Cart is ACTIVE
- Not updated in the last N minutes (e.g., ≥ 30 minutes)
- Has at least one available item (product not discontinued and stock > 0)
- Cart total ≥ X
- Customer allows marketing

The same spec is evaluable in-memory and translatable to SQL predicates.

## Architecture approach
- Domain remains persistence-agnostic and declares the intent via specifications (and/or a `CartQuery` value object holding a spec tree).
- Adapter (JPA) implements a visitor to translate domain specs to Spring Data JPA `Specification<CartEntity>` (Criteria) or Querydsl.
- Repository port exposes `findBy(spec, pageable)` enabling SQL pushdown and paging at the database level.

## Adapter translation sketch
- `ActiveCart` → `status = 'ACTIVE'`
- `LastUpdatedBefore(t)` → `updated_at < t`
- `HasMinTotal(m)` → If denormalized total exists: `total_amount >= :m.amount` and `total_currency = :m.currency`.
  Otherwise: item join with `GROUP BY cart.id HAVING SUM(item.price_amount * item.quantity) >= :m.amount` with currency filter.
- `HasAnyAvailableItem` → (when product read model exists) `EXISTS (select 1 from cart_item i join product p on p.id=i.product_id where i.cart_id = cart.id and p.discontinued=false and p.stock>0)`.
  Until then, simplified join on items with `quantity > 0` is acceptable.
- `CustomerAllowsMarketing` → (when customer profile exists) join/subquery `customers.allows_marketing = true`; until then, no-op predicate.

Indexes recommended: `(status, updated_at)`, `(customer_id)`, and if denormalized totals are used: `(total_amount, total_currency)`; product `(discontinued, stock)`.

## Notes
- Keep domain pure; no JPA or Spring types inside specifications.
- Prefer a visitor to avoid `instanceof` in adapters and keep translation extendable.
- Consider Querydsl if the team prefers type-safe predicate composition.

---

## Execution log (cumulative)

- Domain spec infrastructure added under `sharedkernel/domain/spec` with combinators. [✓]
- Cart domain leaf specs added: `ActiveCart`, `LastUpdatedBefore`, `HasMinTotal`. [✓]
- Repository port extended: `ShoppingCartRepository.findBy(DomainSpecification<ShoppingCart>, Pageable)` with in‑memory fallback paging. [✓]
- JPA adapter pushdown implemented: `CartJpaRepository` extends `JpaSpecificationExecutor`; `JpaShoppingCartRepository.findBy(...)` translates via `CartSpecToJpa` and pages in DB. [✓]
- Introduced dedicated integration test source set `src/test-integration` and task `test-integration` (plugin `gradle/plugins/test-integration.gradle`). [✓]
- Migrated `ShoppingCartRepositoryJpaIT` to `src/test-integration/java/...`; disabled duplicate in `src/test/java`. [✓]
- Added `UnitSmokeTest` under `src/test/java` to verify unit pipeline. [✓]
- IntelliJ IDEA Gradle module updated to mark all test sources/resources as test (green). [✓]

### Usage example
```java
var spec = new ActiveCart()
    .and(new LastUpdatedBefore(Instant.now().minus(Duration.ofMinutes(30))))
    .and(new HasMinTotal(Money.euro(50)));

Page<ShoppingCart> page = shoppingCartRepository.findBy(spec, PageRequest.of(0, 100));
```

### How to run tests
- Unit tests (JUnit 5):
  - `./gradlew test`
  - One class: `./gradlew test --tests "*UnitSmokeTest*"`
- Integration tests:
  - `./gradlew test-integration`
  - One class: `./gradlew test-integration --tests "*ShoppingCartRepositoryJpaIT*"`
- Architecture tests (ArchUnit/Spock):
  - `./gradlew test-architecture`
- Richer logs (per task): add `-Plog-debug`

### CI guidance
- Ensure your pipeline runs all three tasks:
  - `test` (unit), `test-integration` (integration), and `test-architecture` (architecture).
- Publish reports from:
  - `build/reports/test`, `build/reports/test-integration`, and `build/reports/test-architecture`.

### Open items / planned
- Implement `HasAnyAvailableItem` with product availability join once a product read model is present; until then, keep simplified join on items. [ ]
- Implement `CustomerAllowsMarketing` with customer/consent join or subquery; until then, keep as no‑op predicate. [ ]
- Add unit tests for spec algebra and leaf behavior. [ ]
- Consider denormalized totals to avoid `GROUP BY/HAVING` over items for `HasMinTotal`; update translator and add an ADR if adopted. [ ]
