# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records (ADRs) for the AI-Architecture sample project.

## What is an ADR?

An Architecture Decision Record captures an important architectural decision made along with its context and consequences.

## ADR Index

### Accepted ✅

| ADR | Title | Date | Status |
|-----|-------|------|--------|
| [ADR-001](adr-001-api-web-package-separation.md) | Separate REST API and Web MVC Controllers into Different Packages | 2025-10-24 | ✅ Accepted |
| [ADR-002](adr-002-framework-independent-domain.md) | Framework-Independent Domain Layer | 2025-10-24 | ✅ Accepted |
| [ADR-003](adr-003-aggregate-reference-by-id.md) | Aggregate Reference by Identity Only | 2025-10-24 | ✅ Accepted |
| [ADR-004](adr-004-persistence-oriented-repository.md) | Persistence-Oriented Repository Pattern | 2025-10-24 | ✅ Accepted |
| [ADR-005](adr-005-domain-events-publishing.md) | Domain Events Publishing Strategy | 2025-10-24 | ✅ Accepted |
| [ADR-006](adr-006-domain-events-immutable-records.md) | Domain Events as Immutable Records | 2025-10-24 | ✅ Accepted |
| [ADR-007](adr-007-hexagonal-architecture.md) | Hexagonal Architecture with Explicit Port/Adapter Separation | 2025-10-24 | ✅ Accepted |
| [ADR-008](adr-008-repository-interfaces-in-domain.md) | Repository Interfaces in Domain Layer | 2025-10-24 | ✅ Accepted |
| [ADR-009](adr-009-value-objects-as-records.md) | Value Objects as Java Records | 2025-10-24 | ✅ Accepted |
| [ADR-010](adr-010-domain-services-multi-aggregate.md) | Domain Services Only for Multi-Aggregate Operations | 2025-10-24 | ✅ Accepted |
| [ADR-011](adr-011-bounded-context-isolation.md) | Bounded Context Isolation via Package Structure | 2025-10-24 | ✅ Accepted |
| [ADR-013](adr-013-specification-pattern.md) | Specification Pattern for Business Rules | 2025-10-24 | ✅ Accepted |
| [ADR-014](adr-014-factory-pattern.md) | Factory Pattern for Complex Aggregate Creation | 2025-10-24 | ✅ Accepted |
| [ADR-015](adr-015-archunit-governance.md) | ArchUnit for Architecture Governance | 2025-10-24 | ✅ Accepted |
| [ADR-016](adr-016-shared-kernel-pattern.md) | Shared Kernel Pattern for Cross-Context Value Objects | 2025-10-24 | ✅ Accepted |

### Proposed 🟡

| ADR | Title | Date | Status |
|-----|-------|------|--------|
| [ADR-012](adr-012-use-case-input-output-models.md) | Use Case Input/Output Models (Command/Query Pattern) | 2025-10-24 | 🟡 Proposed |

### Superseded 🔄

| ADR | Title | Superseded By |
|-----|-------|---------------|
| (none yet) | | |

### Deprecated ⚠️

| ADR | Title | Reason |
|-----|-------|--------|
| (none yet) | | |

---

## ADR Format

Each ADR follows this structure:

- **Context**: The issue motivating this decision
- **Decision**: The decision that was made
- **Rationale**: Why this decision was made
- **Consequences**: Positive, neutral, and negative outcomes
- **Alternatives Considered**: Other options that were evaluated
- **Implementation**: How the decision was/will be implemented
- **References**: Related patterns, ADRs, and external resources

---

## Creating a New ADR

1. Copy the template from `adr-template.md`
2. Name it `adr-XXX-short-title.md` (use next available number)
3. Fill in all sections
4. Update this README index
5. Get it reviewed and approved by the Architecture Team

---

## References

- [Architecture Decision Records](https://adr.github.io/)
- [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)

---

**Last Updated**: October 24, 2025
