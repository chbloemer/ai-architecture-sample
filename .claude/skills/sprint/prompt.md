# Sprint Agent — Project Conventions

This file is loaded by `/sprint` agents. It contains everything an agent needs to implement stories in this codebase.

## Tech Stack

- Java 25, Spring Boot 4.0.2, Gradle 9.3.1
- Spring Framework 7, Jakarta EE 11, Hibernate 7.2.1.Final
- ArchUnit tests in Spock/Groovy (Spock 2.4-groovy-5.0)
- JSpecify for nullability, Lombok for boilerplate reduction
- pug4j templates, CSS with BEM methodology

## Bounded Contexts

| Context | Package | Purpose |
|---------|---------|---------|
| product | `de.sample.aiarchitecture.product` | Product catalog (identity, description, images) |
| pricing | `de.sample.aiarchitecture.pricing` | Product pricing |
| inventory | `de.sample.aiarchitecture.inventory` | Stock levels |
| cart | `de.sample.aiarchitecture.cart` | Shopping cart |
| checkout | `de.sample.aiarchitecture.checkout` | Checkout process |
| account | `de.sample.aiarchitecture.account` | User accounts, auth |
| portal | `de.sample.aiarchitecture.portal` | Portal / storefront pages |
| sharedkernel | `de.sample.aiarchitecture.sharedkernel` | Shared markers, value objects, adapters |

## Package Structure

```
de.sample.aiarchitecture.{context}/
├── domain/
│   ├── model/          # Aggregates, Entities, Value Objects
│   └── event/          # Domain Events
├── application/
│   ├── {usecasename}/  # Use case folder (lowercase, e.g., createorder)
│   │   ├── *InputPort.java    # extends UseCase<Command/Query, Result>
│   │   ├── *UseCase.java      # implements *InputPort, @Service
│   │   ├── *Command.java      # write input (or *Query.java for reads)
│   │   └── *Result.java       # output
│   └── shared/         # Repository interfaces, output ports
└── adapter/
    ├── incoming/
    │   ├── web/        # *PageController (MVC controllers)
    │   └── event/      # *EventConsumer (event listeners)
    └── outgoing/
        └── persistence/  # Repository implementations (InMemory*)
```

## Naming Conventions

| Component | Convention | Example |
|-----------|-----------|---------|
| Use case folder | lowercase | `startcheckout`, `getproductbyid` |
| Input Port | `*InputPort` | `StartCheckoutInputPort extends UseCase<StartCheckoutCommand, StartCheckoutResult>` |
| Use Case impl | `*UseCase` | `StartCheckoutUseCase implements StartCheckoutInputPort` |
| Command (write) | `*Command` | `StartCheckoutCommand` |
| Query (read) | `*Query` | `GetProductByIdQuery` |
| Result | `*Result` | `StartCheckoutResult` |
| Domain Event | Past tense | `ProductPriceChanged`, `CheckoutStarted` |
| Integration Event | Past tense + Event | `OrderCreatedEvent` |
| Page Controller | `*PageController` | `CatalogPageController` |
| Event Consumer | `*EventConsumer` | `CheckoutConfirmedEventConsumer` |
| MCP Tool Provider | `*McpToolProvider` | `ProductMcpToolProvider` |
| Repository iface | `*Repository` in `application/shared/` | `ProductRepository` |
| Repository impl | `InMemory*Repository` in `adapter/outgoing/persistence/` | `InMemoryProductRepository` |
| Enriched Model | record implementing Value, in `domain/model/` | `EnrichedProduct` |
| ViewModel | record in `adapter/incoming/web/` | `ProductViewModel` |

## Dependency Rules

1. **Domain** — NO Spring annotations, NO external dependencies. Pure Java.
2. **Application** — Depends on domain only. Use cases annotated with `@Service`.
3. **Adapters** — Depend on application + domain. Annotated with `@Component`/`@Controller`.
4. **Infrastructure** — Cross-cutting config. `@Configuration` classes.
5. **Adapters must NOT call other adapters directly** — go through application layer.
6. **Cross-context communication** — via integration events only, never direct imports.

## DDD Markers (Shared Kernel)

All domain objects implement marker interfaces from `sharedkernel.marker`:
- `AggregateRoot` / `BaseAggregateRoot<T, ID>` — aggregate roots
- `Entity` — entities
- `Value` — value objects (use Java records)
- `DomainEvent` — domain events (use records with `eventId`, `occurredOn`, `version`)
- `DomainService` — stateless domain services
- `Repository<T, ID>` — repository interfaces (extends `OutputPort`)
- `OutputPort` — all output port interfaces
- `InputPort` / `UseCase<I, O>` — input port interfaces

## Quality Commands

```bash
./gradlew build                    # Full build with unit tests
./gradlew build -x test            # Compile only
./gradlew test                     # Unit tests (JUnit 6 / Spock)
./gradlew test-architecture        # ArchUnit architecture tests
./gradlew test-integration         # Integration tests
./gradlew bootRun                  # Run application (debug port 5005)
```

## Agent Work Protocol

When working as a sprint agent:

1. **TaskList** — find tasks assigned to you (owner = your name)
2. **TaskGet** — read full story details (acceptance criteria, architectural guidance)
3. **TaskUpdate** — set status to `in_progress`
4. **Read dependent story context** — if the story has `depends_on`, understand what was already implemented by exploring relevant code
5. **Explore existing patterns** — look at similar code in the same or other bounded contexts before writing new code
6. **Implement** — follow acceptance criteria and architectural guidance precisely
7. **Build** — run `./gradlew build` (must pass before completing)
8. **Architecture tests** — run `./gradlew test-architecture` if you touched domain/application/adapter layers
9. **TaskUpdate** — set status to `completed`
10. **SendMessage** to team lead — summarize changes (files modified, patterns used, decisions made)
11. **TaskList** — check for more tasks, or go idle

## Important Rules

- **Follow existing patterns** — match code style in existing bounded contexts
- **One story at a time** — complete fully before moving on
- **No shortcuts** — implement properly, don't skip validation or patterns
- **No Spring in domain** — domain layer must be framework-independent
- **Results, not Responses** — use `*Result` naming per ADR-020
- **EventConsumer, not EventListener** — for event handler class names
- **If blocked** — message team lead immediately with specifics
