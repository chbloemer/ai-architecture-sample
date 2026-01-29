# RALPH Prompt Template

This file contains the base prompt template for RALPH iterations.

## Project Context

You are implementing a **Checkout bounded context** for a Domain-Centric Architecture sample application.

## Codebase Conventions

### Package Structure
```
de.sample.aiarchitecture.checkout/
├── domain/
│   ├── model/          # Aggregates, Entities, Value Objects
│   └── event/          # Domain Events
├── application/
│   ├── {usecasename}/  # Use case folders (lowercase)
│   │   ├── *InputPort.java
│   │   ├── *UseCase.java
│   │   ├── *Command.java or *Query.java
│   │   └── *Response.java
│   └── shared/         # Repository interfaces
└── adapter/
    ├── incoming/
    │   └── web/        # Controllers
    └── outgoing/
        ├── persistence/  # Repository implementations
        └── payment/      # Payment provider adapters
```

### Naming Conventions
- Use case folders: lowercase (e.g., `startcheckout`, `submitbuyerinfo`)
- Input Ports: `*InputPort extends UseCase<Command, Response>`
- Commands: `*Command` (writes), Queries: `*Query` (reads)
- Domain Events: Past tense (e.g., `CheckoutStarted`)
- Controllers: `*PageController`

### Quality Commands
```bash
./gradlew build                    # Full build with tests
./gradlew build -x test            # Compile only
./gradlew test                     # Unit tests
./gradlew test-architecture        # Architecture tests
./gradlew bootRun                  # Run application
```

### Dependencies
- Domain layer: NO Spring annotations, NO external dependencies
- Use `@Service` for use cases
- Use `@Component` for adapters
- Use `@Controller` for web controllers

### Reference Patterns
- Look at `cart/` bounded context for patterns
- Look at `productcatalog/` for simpler examples
- Shared kernel in `sharedkernel/domain/`

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

1. **One story at a time** - Focus only on the current story
2. **Follow existing patterns** - Match the code style in cart/ and productcatalog/
3. **Test your work** - Run `./gradlew build` before signaling completion
4. **Small commits** - Each story should be a small, focused change
5. **No shortcuts** - Implement properly, don't skip validation or patterns
