# RALPH Prompt Template

This file contains the base prompt template for RALPH iterations.

## Project Context

You are implementing features for a **Domain-Centric Architecture** sample application. This project demonstrates DDD, Hexagonal Architecture, and Clean Architecture patterns.

### Bounded Contexts
- `product/` - Product catalog (identity, description)
- `pricing/` - Product pricing
- `inventory/` - Stock levels
- `cart/` - Shopping cart
- `checkout/` - Checkout process
- `sharedkernel/` - Shared value objects and markers

## Dependent Story Logs

**Before starting work**, check if your story has dependencies. If so, read the output logs of dependent stories in `tasks/logs/` to understand what was already implemented.

**Log file pattern:** `US-<id>_<timestamp>.log`

Example:
```bash
# Find logs for US-89
ls tasks/logs/US-89_*.log

# Read the most recent log for US-89
cat tasks/logs/US-89_*.log | tail -100
```

This gives you context about:
- What classes were created
- What patterns were used
- Any decisions or trade-offs made

## Codebase Conventions

### Package Structure
```
de.sample.aiarchitecture.{context}/
├── domain/
│   ├── model/          # Aggregates, Entities, Value Objects
│   └── event/          # Domain Events
├── application/
│   ├── {usecasename}/  # Use case folders (lowercase)
│   │   ├── *InputPort.java
│   │   ├── *UseCase.java
│   │   ├── *Command.java or *Query.java
│   │   └── *Result.java
│   └── shared/         # Repository interfaces, output ports
└── adapter/
    ├── incoming/
    │   ├── web/        # Controllers
    │   └── openhost/   # Open Host Services (OHS)
    └── outgoing/
        ├── persistence/  # Repository implementations
        └── {external}/   # External service adapters
```

### Naming Conventions
- Use case folders: lowercase (e.g., `startcheckout`, `submitbuyerinfo`)
- Input Ports: `*InputPort extends UseCase<Command, Result>`
- Commands: `*Command` (writes), Queries: `*Query` (reads)
- Results: `*Result`
- Domain Events: Past tense (e.g., `CheckoutStarted`, `ProductPriceChanged`)
- Controllers: `*PageController`
- Open Host Services: `*Service` in `adapter/incoming/openhost/`

### Quality Commands
```bash
./gradlew build                    # Full build with tests
./gradlew build -x test            # Compile only
./gradlew test                     # Unit tests
./gradlew test-integration         # Integration tests
./gradlew test-architecture        # Architecture tests
./gradlew bootRun                  # Run application
```

### Dependencies
- Domain layer: NO Spring annotations, NO external dependencies
- Use `@Service` for use cases
- Use `@Component` for adapters
- Use `@Controller` for web controllers

### Reference Patterns
- Look at existing bounded contexts for patterns
- Shared kernel in `sharedkernel/domain/`
- Architecture documentation in `docs/architecture/`
- Plan files in `docs/plans/` for architectural context

## Completion Signals

When you complete a story successfully:
```
<promise>STORY_COMPLETE</promise>
```

When blocked:
```
<promise>BLOCKED: [specific reason]</promise>
```

## Important Rules

1. **Check dependencies first** - Read logs of dependent stories in `tasks/logs/`
2. **One story at a time** - Focus only on the current story
3. **Follow existing patterns** - Match the code style in existing bounded contexts
4. **Test your work** - Run `./gradlew build` before signaling completion
5. **Small commits** - Each story should be a small, focused change
6. **No shortcuts** - Implement properly, don't skip validation or patterns