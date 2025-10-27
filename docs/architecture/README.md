# Architecture Documentation

This directory contains comprehensive architectural documentation for the AI Architecture Sample Project.

## Documentation Index

### Core Documentation

- **[architecture-principles.md](architecture-principles.md)** - Main architecture reference
  - Domain-Driven Design patterns (Strategic and Tactical)
  - Hexagonal Architecture (Ports and Adapters)
  - Onion Architecture (Dependency Inversion)
  - Layered Architecture
  - Package structure and organization
  - Bounded contexts and context mapping
  - Architectural rules enforced by ArchUnit

- **[transaction-management.md](transaction-management.md)** - Transaction handling patterns
  - Application services as transactional boundaries
  - Read-only transaction optimization
  - Transactional event listeners
  - Transaction flow and lifecycle
  - Best practices and common patterns

### Additional Resources

- **[../../CLAUDE.md](../../CLAUDE.md)** - Guidelines for AI assistants working on this project
- **[../../README.md](../../README.md)** - Project overview and API documentation

## Quick Reference

### Architecture Patterns Used

1. **Domain-Driven Design (DDD)**
   - Strategic: Bounded Contexts, Context Mapping
   - Tactical: Aggregates, Entities, Value Objects, Repositories, Domain Services, Domain Events

2. **Hexagonal Architecture**
   - Primary Adapters: REST Controllers
   - Secondary Adapters: Repository Implementations
   - Ports: Application Services, Repository Interfaces

3. **Onion Architecture**
   - Core: Domain Model (framework-independent)
   - Layer 2: Application Services
   - Outer: Infrastructure and Adapters

### Package Structure

```
de.sample.aiarchitecture
├── domain.model              # Domain layer (CORE)
│   ├── ddd                   # DDD marker interfaces
│   ├── product               # Product bounded context
│   └── cart                  # Shopping Cart bounded context
├── application               # Application services
├── infrastructure            # Infrastructure
│   ├── api                   # Public SPI
│   ├── config                # Spring configuration
│   └── event                 # Event listeners
└── portadapter               # Adapters
    ├── incoming              # Incoming adapters (Primary/Driving: REST, Web, MCP)
    └── outgoing              # Outgoing adapters (Secondary/Driven: Persistence)
```

### Key Principles

1. **Dependencies flow inward** toward the domain core
2. **Domain layer is framework-independent** (no Spring, JPA)
3. **One repository per aggregate root** (not per entity)
4. **Aggregates reference each other by ID only** (Vernon's Rule #2)
5. **Domain events enable eventual consistency** (Vernon's Rule #4)
6. **Use ubiquitous language** from the business domain

### Documentation Maintenance

**⚠️ Important:** When making changes to the codebase, always update the architecture documentation to reflect:
- New patterns or components added
- Changes to existing patterns
- Architectural decisions made
- Updated code examples

See [CLAUDE.md](../../CLAUDE.md) for detailed guidelines on maintaining documentation.

## Architecture Tests

All architectural rules are automatically enforced using ArchUnit tests located in:
```
src/test-architecture/groovy/de/sample/aiarchitecture/
```

Run architecture tests:
```bash
./gradlew test-architecture
```

## Learning Path

### For New Team Members

1. **Start here:** Read `architecture-principles.md` to understand the overall architecture
2. **Review code:** Explore the domain model in `src/main/java/de/sample/aiarchitecture/domain/model/`
3. **Run the app:** `./gradlew bootRun` and try the REST API
4. **Examine tests:** Look at ArchUnit tests to see how rules are enforced
5. **Read guidelines:** Review `CLAUDE.md` for development standards

### For Understanding DDD

1. Read the **Domain-Driven Design** section in `architecture-principles.md`
2. Study the **Product** bounded context as an example
3. Examine **Domain Events** implementation
4. Review **Repository** pattern usage
5. Understand **Aggregate** boundaries

### For Understanding Hexagonal Architecture

1. Read the **Hexagonal Architecture** section
2. Trace a request: Controller → Application Service → Domain → Repository
3. See how **Primary Adapters** (controllers) call **Primary Ports** (application services)
4. Understand **Secondary Ports** (repository interfaces) and **Secondary Adapters** (implementations)

## References

### Books

- **Domain-Driven Design** by Eric Evans (2003)
- **Implementing Domain-Driven Design** by Vaughn Vernon (2013)

### Online Resources

- [Domain-Driven Design Reference](https://www.domainlanguage.com/ddd/)
- [Martin Fowler - Repository Pattern](https://martinfowler.com/eaaCatalog/repository.html)
- [ArchUnit Documentation](https://www.archunit.org/)

---

**Last Updated:** 2025-10-24

*This documentation is maintained as part of the codebase. All changes to architecture should be reflected here.*
