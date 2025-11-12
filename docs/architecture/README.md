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

Each bounded context (product, cart, portal) follows the same structure:
```
{context}/
├── domain/             # Domain layer (innermost)
│   ├── model/          # Aggregates, entities, value objects
│   ├── service/        # Domain services
│   └── event/          # Domain events
├── application/        # Application layer
│   ├── port/           # Ports (interfaces)
│   │   └── out/        # Output ports (repositories, external services)
│   └── usecase/        # Use cases (input ports)
└── adapter/            # Adapter layer (outermost)
    ├── incoming/       # Incoming adapters (primary/driving)
    │   ├── api/        # REST API
    │   ├── web/        # Web MVC
    │   ├── mcp/        # MCP server
    │   └── event/      # Domain event listeners
    └── outgoing/       # Outgoing adapters (secondary/driven)
        └── persistence/ # Repository implementations
```

**Shared Kernel:** Cross-context value objects and DDD marker interfaces in `sharedkernel/`

**Full structure:** See [package-structure.md](package-structure.md) for complete details

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
