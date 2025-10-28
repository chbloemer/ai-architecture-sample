# Claude AI Assistant Guidelines

This document provides guidelines for AI assistants (like Claude) working on this project.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture Documentation](#architecture-documentation)
3. [Development Workflow](#development-workflow)
4. [Code Standards](#code-standards)
5. [Testing Requirements](#testing-requirements)
6. [Documentation Requirements](#documentation-requirements)

---

## Project Overview

This is a **sample e-commerce application** demonstrating best practices for:

- **Domain-Driven Design (DDD)** - Strategic and tactical patterns
- **Hexagonal Architecture** - Ports and Adapters pattern
- **Onion Architecture** - Dependency inversion with layers pointing inward
- **Clean Architecture** - Framework-independent business logic

**Tech Stack:**
- Java 21
- Spring Boot 3.5.6
- Gradle 9.1
- ArchUnit for architecture testing
- Spock/Groovy for architecture tests

**Purpose:**
This project serves as a reference implementation showing how to properly structure an enterprise application using modern architectural patterns. It is designed to be educational and demonstrate best practices.

---

## Architecture Documentation

### Critical Rule: Always Update Architecture Documentation

**⚠️ IMPORTANT: After making ANY changes to the codebase, you MUST update the architecture documentation.**

### When to Update Documentation

Update architecture documentation in the following scenarios:

1. **Adding new DDD patterns** (Aggregates, Entities, Value Objects, Services, Events, etc.)
2. **Modifying existing patterns** (changing interfaces, adding methods, refactoring)
3. **Adding new architectural layers or components**
4. **Changing package structure**
5. **Adding new bounded contexts**
6. **Modifying repository interfaces or implementations**
7. **Adding or changing domain events**
8. **Updating application services**
9. **Any significant architectural decision**

### Documentation Structure

```
docs/
└── architecture/
    ├── architecture-principles.md    # Main architecture documentation
    ├── design-decisions.md           # ADRs (Architecture Decision Records)
    ├── bounded-contexts.md           # Context mapping and relationships
    └── patterns/                     # Pattern-specific guides
        ├── repository-pattern.md
        ├── domain-events.md
        └── aggregate-design.md

```

### How to Update Documentation

1. **Read the current documentation** to understand what exists
2. **Identify affected sections** based on your changes
3. **Update relevant sections** with:
   - New code examples
   - Updated diagrams (if applicable)
   - Revised explanations
   - Additional rules or best practices
4. **Add new sections** if introducing new patterns
5. **Update cross-references** to maintain consistency

### Documentation Update Checklist

After making code changes, verify:

- [ ] `architecture-principles.md` reflects current patterns
- [ ] Code examples in documentation match actual implementation
- [ ] New patterns are documented with examples
- [ ] Rules and best practices are updated
- [ ] Package structure diagrams are current
- [ ] References to specific files/classes are accurate
- [ ] Benefits and trade-offs are documented

### Example: Documenting a New Pattern

When adding a new DDD pattern:

```markdown
#### [Pattern Name]

[Brief description of what this pattern is and when to use it]

**Example: [Concrete Example from Codebase]**

```java
// Real code from the project
public class ExampleClass implements Pattern {
    // ...
}
```

**Rules:**
1. [Rule 1]
2. [Rule 2]
...

**Implementation:**
- Interface: `de.sample.aiarchitecture.domain.model.ddd.Pattern`
- Example: `de.sample.aiarchitecture.domain.model.product.ConcreteExample`
```

---

## Development Workflow

### Standard Development Process

1. **Understand the requirement**
   - Read existing code and documentation
   - Identify affected components
   - Plan the changes

2. **Make code changes**
   - Follow DDD patterns
   - Maintain architectural boundaries
   - Write clean, well-documented code

3. **Update architecture documentation** ⚠️
   - This is NOT optional
   - Update `docs/architecture/architecture-principles.md`
   - Add examples from your actual changes
   - Update any affected diagrams or references

4. **Run tests**
   - `./gradlew build` - Compile and build
   - `./gradlew test-architecture` - Verify architecture rules
   - Ensure all tests pass

5. **Verify the application**
   - `./gradlew bootRun` - Start the application
   - Test the changes manually if needed

### Architecture Test Failures

If architecture tests fail:

1. **Understand the violation** - Read the error message carefully
2. **Determine if the change is correct**:
   - If the code violates architecture rules → Fix the code
   - If the rule is too strict → Discuss with the team before changing tests
3. **Never disable tests** without documenting why
4. **Update documentation** to reflect any architectural decisions

---

## Code Standards

### Domain-Driven Design Patterns

#### Aggregates
- Extend `BaseAggregateRoot<T, ID>`
- Enforce invariants within aggregate boundaries
- Raise domain events for state changes
- Reference other aggregates by ID only

#### Entities
- Implement `Entity<T, ID>`
- Have a unique identity
- Equality based on ID, not attributes

#### Value Objects
- Implement `Value` interface
- Use Java records for immutability
- Include validation in constructor
- Equality based on attributes

#### Repositories
- Interface in domain layer (`domain.model.*`)
- Extend `Repository<T, ID>` base interface
- Implementation in outgoing adapters (`portadapter.outgoing.*`)
- Use domain language in method names

#### Domain Events
- Implement `DomainEvent` interface
- Named in past tense (e.g., `ProductPriceChanged`)
- Include `eventId`, `occurredOn`, and `version`
- Immutable (use records)

#### Domain Services
- Implement `DomainService` marker interface
- Stateless (only final fields)
- Framework-independent (no Spring annotations)

### Package Structure Rules

```
de.sample.aiarchitecture
├── domain.model              # Domain layer (core business logic)
│   ├── ddd                   # DDD marker interfaces
│   ├── product               # Product bounded context
│   └── cart                  # Shopping Cart bounded context
├── application               # Application services (use cases)
├── infrastructure            # Infrastructure configuration
│   ├── api                   # Public SPI
│   └── config                # Spring configuration
└── portadapter               # Adapters (Hexagonal Architecture)
    ├── incoming              # Incoming adapters (Primary/Driving: REST, Web, MCP)
    └── outgoing              # Outgoing adapters (Secondary/Driven: Persistence)
```

### Dependency Rules

1. **Domain** → No dependencies (framework-independent)
2. **Application** → Depends on domain + infrastructure.api only
3. **Infrastructure** → Depends on domain
4. **Adapters** → Depend on application and domain
5. **Adapters** → Must NOT communicate directly with each other

---

## Testing Requirements

### Architecture Tests (ArchUnit)

Location: `src/test-architecture/groovy/de/sample/aiarchitecture/`

**Test Categories:**
- `DddTacticalPatternsArchUnitTest` - Aggregate, Entity, Value Object, Repository patterns
- `DddAdvancedPatternsArchUnitTest` - Domain Events, Services, Factories, Specifications
- `DddStrategicPatternsArchUnitTest` - Bounded Context isolation
- `HexagonalArchitectureArchUnitTest` - Ports and Adapters rules
- `OnionArchitectureArchUnitTest` - Dependency flow rules
- `LayeredArchitectureArchUnitTest` - Layer access rules
- `NamingConventionsArchUnitTest` - Naming standards
- `PackageCyclesArchUnitTest` - Circular dependency detection

**Run architecture tests:**
```bash
./gradlew test-architecture
```

### Unit Tests

- Test domain logic in isolation
- Mock external dependencies
- Focus on business rules and invariants

### Integration Tests

- Test application services with real repository implementations
- Verify event publishing and handling
- Test REST endpoints

---

## Documentation Requirements

### Code Documentation

1. **All public classes** must have JavaDoc
2. **All public methods** must have JavaDoc explaining:
   - Purpose
   - Parameters
   - Return values
   - Exceptions
   - Domain events raised (if applicable)
3. **Domain patterns** must reference DDD concepts in JavaDoc
4. **Architecture decisions** should be documented in code comments

### Architecture Documentation

**Main Document:** `docs/architecture/architecture-principles.md`

This document must include:

1. **Overview** - Project purpose and patterns used
2. **Domain-Driven Design** section with:
   - Strategic Patterns (Bounded Contexts, Context Mapping)
   - Tactical Patterns (Aggregates, Entities, Value Objects, Repositories, etc.)
   - Code examples from the actual codebase
   - Rules and best practices
3. **Hexagonal Architecture** section
4. **Onion Architecture** section
5. **Package Structure** with visual representation
6. **Bounded Contexts** description
7. **Architectural Rules** enforced by ArchUnit

### Design Decisions

For significant architectural decisions, create an Architecture Decision Record (ADR) in `docs/architecture/design-decisions.md`:

```markdown
## ADR-XXX: [Decision Title]

**Date:** YYYY-MM-DD

**Status:** Accepted | Proposed | Deprecated | Superseded

**Context:**
[What is the issue that we're seeing that is motivating this decision?]

**Decision:**
[What is the change that we're proposing and/or doing?]

**Consequences:**
[What becomes easier or more difficult to do because of this change?]
```

---

## Best Practices

### Domain Modeling

1. **Use Ubiquitous Language** - Names in code match business terminology
2. **Enforce Invariants** - Aggregates maintain consistency
3. **Raise Domain Events** - Capture important business occurrences
4. **Reference by ID** - Aggregates reference each other by identity only
5. **Keep Aggregates Small** - Focus on transactional consistency boundaries

### Event-Driven Design

1. **Events are Immutable** - Use records or final classes
2. **Events Capture Facts** - Named in past tense
3. **Events Enable Decoupling** - Bounded contexts coordinate via events
4. **Events Support Audit** - Include timestamp and event ID
5. **Publish After Persistence** - Only publish events for successfully saved aggregates

### Layered Architecture

1. **Dependencies Point Inward** - Toward the domain core
2. **Framework-Free Domain** - No Spring, JPA, or infrastructure in domain
3. **Ports Define Contracts** - Interfaces in domain, implementations in adapters
4. **Application Services Orchestrate** - Thin coordination layer
5. **Adapters Are Replaceable** - Easy to swap implementations

---

## Common Tasks

### Adding a New Aggregate

1. Create aggregate root class extending `BaseAggregateRoot`
2. Create value objects for properties
3. Create entity classes for aggregate entities
4. Create repository interface in domain
5. Implement repository in outgoing adapter
6. Create domain events for important state changes
7. Create factory if creation is complex
8. Update application service to use aggregate
9. **Update `architecture-principles.md`** with new aggregate example
10. Run architecture tests

### Adding a New Domain Event

1. Create event record implementing `DomainEvent`
2. Include eventId, occurredOn, version, and domain-specific data
3. Add static factory method (e.g., `now()`)
4. Raise event in aggregate when state changes
5. Publish events in application service after save
6. Create event listener if needed
7. **Update `architecture-principles.md`** with event example
8. Test event publishing

### Adding a New Repository Method

1. Add method to repository interface in domain layer
2. Use domain language in method name
3. Implement method in repository implementation
4. Update application service to use new method
5. **Update `architecture-principles.md`** if pattern changes
6. Add tests

---

## Anti-Patterns to Avoid

### Domain Layer

❌ **Don't:**
- Use Spring annotations in domain (@Service, @Component, @Entity)
- Use JPA annotations in domain (@Entity, @Table, @Column)
- Reference infrastructure classes from domain
- Create anemic domain models (getters/setters only)
- Have aggregates directly reference other aggregates

✅ **Do:**
- Keep domain framework-independent
- Put business logic in domain objects
- Reference other aggregates by ID
- Enforce invariants in domain
- Use value objects for concepts

### Repository Pattern

❌ **Don't:**
- Create repositories for entities (only for aggregate roots)
- Use generic CRUD names unless appropriate for domain
- Put business logic in repository
- Return infrastructure objects from repository

✅ **Do:**
- Create one repository per aggregate root
- Use ubiquitous language in method names
- Return domain objects
- Keep repositories simple (data access only)

### Application Services

❌ **Don't:**
- Put business logic in application services
- Have application services call other application services
- Make application services stateful
- Access portadapters from application services

✅ **Do:**
- Keep application services thin (orchestration only)
- Delegate business logic to domain
- Publish domain events after save
- Only use infrastructure.api (not implementations)

---

## Troubleshooting

### Architecture Test Failures

**Problem:** `Domain must not have dependencies on Infrastructure`
- **Solution:** Remove infrastructure imports from domain layer

**Problem:** `Application Services must only use infrastructure.api (not infrastructure implementations)`
- **Solution:** Move class from `infrastructure.*` to `infrastructure.api.*`

**Problem:** `Entities must have an ID field`
- **Solution:** Add an `id` field to entity class

**Problem:** `Aggregate Roots must not have fields with other Aggregate Root types`
- **Solution:** Reference other aggregates by ID, not by direct reference

### Build Failures

**Problem:** Compilation errors after refactoring
- **Solution:** Update all references, check imports

**Problem:** Spring can't find bean
- **Solution:** Check @Component/@Service annotations, verify package scanning

---

## Summary

### Key Principles

1. ✅ **Always update architecture documentation** after code changes
2. ✅ Keep domain layer framework-independent
3. ✅ Follow DDD patterns and principles
4. ✅ Respect architectural boundaries
5. ✅ Run architecture tests before committing
6. ✅ Use ubiquitous language throughout
7. ✅ Document design decisions
8. ✅ Keep aggregates small and focused
9. ✅ Raise domain events for important occurrences
10. ✅ Maintain clean, well-documented code

### Documentation Workflow

```
Code Change → Update architecture-principles.md → Run Tests → Commit
      ↑                                                         |
      └─────────────────────────────────────────────────────────┘
                    (Documentation is part of the change!)
```

---

**Remember:** Good architecture is about communication. Keep the documentation up-to-date so the next person (or AI) working on this code understands the decisions and patterns used.
