# Architecture Documentation

Comprehensive architectural documentation for the AI Architecture Sample Project.

## Core Documentation

**[architecture-principles.md](architecture-principles.md)** - Main architecture reference
- Domain-Driven Design (Strategic and Tactical patterns)
- Hexagonal Architecture (Ports and Adapters)
- Onion Architecture (Dependency Inversion)
- Package structure and bounded contexts
- Architectural rules enforced by ArchUnit

**[comprehensive-architecture-analysis.md](comprehensive-architecture-analysis.md)** - Architecture assessment and analysis

**[transaction-management.md](transaction-management.md)** - Transaction handling patterns

**[package-structure.md](package-structure.md)** - Detailed package organization

**[dto-vs-viewmodel-analysis.md](dto-vs-viewmodel-analysis.md)** - DTO vs ViewModel analysis

**[Architecture Decision Records](adr/README.md)** - All ADRs documenting key decisions

## Quick Reference

**Package Structure:**
```
de.sample.aiarchitecture
├── domain.model        # Domain layer (framework-independent core)
├── application         # Application services (use cases)
├── infrastructure      # Infrastructure configuration
│   ├── api            # Public SPI (interfaces only)
│   └── config         # Spring configuration
└── portadapter        # Adapters
    ├── incoming       # Primary adapters (REST, Web, MCP)
    └── outgoing       # Secondary adapters (Persistence)
```

**Key Principles:**
1. Dependencies flow inward toward domain core
2. Domain layer is framework-independent
3. One repository per aggregate root
4. Aggregates reference each other by ID only
5. Domain events enable eventual consistency

**Architecture Tests:**
```bash
./gradlew test-architecture
```

## Related Documentation

- [CLAUDE.md](../../CLAUDE.md) - Guidelines for AI assistants
- [README.md](../../README.md) - Project overview
- [Integration Guides](../integrations/README.md) - Technology integrations
