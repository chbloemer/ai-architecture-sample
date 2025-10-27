# ADR-015: ArchUnit for Architecture Governance

**Date**: October 24, 2025
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

Architectural rules are often documented but not enforced:
- "Domain should not depend on infrastructure" ← Written in docs
- Developer accidentally adds Spring dependency to domain
- No automated check catches it
- Architecture erodes over time

**The Problem**: Architecture documentation ≠ Architecture reality

---

## Decision

**Use ArchUnit to automatically enforce architectural rules as executable tests.**

### Test Structure

```
src/test-architecture/groovy/
├── DddTacticalPatternsArchUnitTest      # Aggregate, Entity, Value Object, Repository rules
├── DddAdvancedPatternsArchUnitTest      # Domain Events, Services, Factories, Specifications
├── DddStrategicPatternsArchUnitTest     # Bounded Context isolation
├── HexagonalArchitectureArchUnitTest    # Ports and Adapters rules
├── OnionArchitectureArchUnitTest        # Dependency flow rules
├── LayeredArchitectureArchUnitTest      # Layer access rules
├── NamingConventionsArchUnitTest        # Naming standards
└── PackageCyclesArchUnitTest            # Circular dependency detection
```

---

## Rationale

### 1. **Automated Enforcement**

```groovy
// OnionArchitectureArchUnitTest.groovy
def "Domain darf keine Abhängigkeiten auf Infrastructure haben"() {
  expect:
  noClasses()
    .that().resideInAPackage("..domain..")
    .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
    .check(allClasses)
}
```

**If violated**: Build fails ❌
**No manual code review needed**

### 2. **Living Documentation**

```groovy
def "Aggregate Roots dürfen keine anderen Aggregate Roots enthalten"() {
  // This test IS the documentation
  // Always up-to-date (or build fails)
}
```

Tests document AND enforce architecture.

### 3. **Prevent Architecture Erosion**

```
Developer adds: import org.springframework.stereotype.Service;
                to domain/model/product/Product.java

./gradlew build
> Task :test-architecture FAILED

Domain sollte keine Framework-Abhängigkeiten haben
Expected: no classes that reside in package '..domain..'
should depend on classes that reside in package 'org.springframework..'
Violated: Product depends on Service
```

Build fails immediately! Architecture protected.

### 4. **Examples of Enforced Rules**

**DDD Tactical Patterns**:
- Aggregates reference each other by ID only
- Entities must have ID field
- Value Objects must be immutable
- Repositories only for Aggregate Roots

**Hexagonal Architecture**:
- Domain doesn't depend on adapters
- Adapters don't communicate directly
- Repository interfaces in domain, implementations in secondary adapters

**Framework Independence**:
- Domain has no Spring dependencies
- Domain has no JPA dependencies
- Domain uses only Java SE + null annotations

**Naming Conventions**:
- REST controllers end with "Resource"
- MVC controllers end with "Controller"
- Domain events named in past tense

---

## Consequences

### Positive

✅ **Automated Enforcement**: Rules checked automatically
✅ **Fast Feedback**: Violations caught at build time
✅ **Living Documentation**: Tests document architecture
✅ **Prevent Erosion**: Architecture stays clean over time
✅ **No Manual Reviews**: Don't need to check rules manually
✅ **Onboarding**: New developers learn architecture from tests

### Neutral

⚠️ **Test Maintenance**: ArchUnit tests need updates when architecture changes
⚠️ **Build Time**: Adds ~10 seconds to build

### Negative

❌ **None identified**

---

## Implementation

### Run Architecture Tests

```bash
./gradlew test-architecture

# Expected output:
# DddTacticalPatternsArchUnitTest > Aggregate Roots dürfen keine anderen Aggregate Roots enthalten PASSED ✅
# DddTacticalPatternsArchUnitTest > Entities müssen ein ID Feld haben PASSED ✅
# OnionArchitectureArchUnitTest > Domain darf nicht auf Infrastructure zugreifen PASSED ✅
# HexagonalArchitectureArchUnitTest > Portadapter dürfen nicht direkt miteinander kommunizieren PASSED ✅
# ... 20+ more tests
```

### Example Test

```groovy
class DddTacticalPatternsArchUnitTest extends Specification {

  @Shared
  JavaClasses allClasses

  def setupSpec() {
    allClasses = new ClassFileImporter()
        .importPackages("de.sample.aiarchitecture")
  }

  def "Aggregate Roots dürfen keine Felder mit anderen Aggregate Root Typen haben"() {
    expect:
    "Aggregate Roots should only reference other Aggregates by ID, not by direct reference"

    noFields()
        .that().areDeclaredInClassesThat(implementAggregateRoot())
        .should().haveRawType(anyAggregateRootType())
        .because("Vernon's Rule #3: Reference other Aggregates by Identity")
        .check(allClasses)
  }

  def "Repository Interfaces müssen im domain Package liegen"() {
    expect:
    classes()
        .that().haveSimpleNameEndingWith("Repository")
        .and().areInterfaces()
        .should().resideInAPackage("..domain.model..")
        .because("Repository interfaces belong to domain layer (DIP)")
        .check(allClasses)
  }
}
```

---

## References

- **ArchUnit**: https://www.archunit.org/
- **Tom Hombergs - Get Your Hands Dirty on Clean Architecture**: Uses ArchUnit extensively

### Related ADRs

All other ADRs are enforced by ArchUnit tests:
- ADR-002: Framework Independence
- ADR-003: Aggregate Reference by ID
- ADR-007: Hexagonal Architecture
- ADR-008: Repository Interfaces in Domain
- And more...

---

## Validation

```bash
# Run tests
./gradlew test-architecture

# Should pass with 20+ architectural rules verified
```

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0
