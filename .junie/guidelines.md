Project Guidelines for ai-architecture

Audience: Senior engineers working on this repository. This document captures project-specific practices that aren’t obvious from conventional Spring/Gradle setups.

1. Build and Configuration
- Java/Gradle
  - Requires Java 21 (Gradle toolchain is configured; Gradle 9 wrapper included).
  - Build without tests: ./gradlew -x test build
  - Package is disabled (jar.enabled = false). Use bootRun for local app startup.
- Spring Boot
  - Main class: de.sample.aiarchitecture.AiArchitectureApplication
  - Run the app: ./gradlew bootRun
    - Remote debug: ./gradlew bootRun — runs with JDWP enabled at address=5005
    - Extra Boot debug logs: ./gradlew -Plog-debug bootRun (adds --debug to args)
  - App configuration: src/main/resources/application.yml
- Dependency management
  - Managed by Spring Dependency Management plugin and Spring Cloud BOM (2025.0.0).
  - Lombok 1.18.42 configured via io.franzbecker.gradle-lombok.
- Nullability
  - JSpecify 1.0.0 is used for @NonNull annotations in domain/application layers.

2. Testing
There are two distinct test setups: conventional unit/integration tests (JUnit Platform) and architecture tests (ArchUnit + Spock/Groovy) in a separate source set.

2.1 Unit tests (JUnit Platform)
- Source roots: src/test/java and/or src/test/groovy
- Runner: JUnit Platform via Spring Boot Starter Test
- Dependencies: configured in gradle/plugins/test-common.gradle
- Typical commands
  - Run all unit tests: ./gradlew test
  - Run one test class: ./gradlew test --tests "*YourTestClass*"
  - Run tests with richer logs: ./gradlew -Plog-debug test
  - Filter by name substring (Gradle property): ./gradlew -Pfilter=Cart test
    - The test task maps -Pfilter to includeTestsMatching "*<value>"
- Verified example (JUnit 5)
  - A minimal JUnit test executed successfully locally using:
    ./gradlew test --tests "*SampleJUnitTest*"
  - Note: We used a temporary SampleJUnitTest under src/test/java to validate the pipeline and then removed it (see cleanup section). This confirms JUnit 5 tests are discovered and executed correctly.

2.2 Architecture tests (separate source set)
- Source roots: src/test-architecture/java, src/test-architecture/groovy, src/test-architecture/resources
- Custom Gradle task: test-architecture
  - Run: ./gradlew test-architecture
  - Uses ArchUnit 1.4.1 and runs on JUnit Platform with Groovy/Spock test classes.
  - Logging: same -Plog-debug flag works, and HTML report is written to build/reports/test-architecture.
- Notes on ArchUnit suites in this repo
  - Tests enforce DDD tactical/strategic and hexagonal boundaries. Some tests are marked @Ignore and will appear as SKIPPED by design.

2.3 Spock/Groovy notes
- The project’s test-common.gradle sets:
  - groovyVersion = 5.0.2
  - spockVersion = 2.4-M6-groovy-4.0
- Groovy 5 + Spock 2.4 for Groovy 4 can lead to non-discovery of Spock specs in the default unit test source set. Architecture tests still run under the dedicated source set due to explicit wiring. If you add Spock specs for unit tests, ensure version alignment (e.g., use Spock variant matching the configured Groovy or switch unit tests to JUnit 5).
- Recommendation: Prefer JUnit 5 for new unit tests unless/until Groovy/Spock versions are aligned for the main test source set.

2.4 Reports and outputs
- Unit tests: build/reports/test and build/test-results/test
- Architecture tests: build/reports/test-architecture and build/test-results/test-architecture

3. How to add and run new tests
- JUnit 5 unit test
  - Create: src/test/java/.../YourTest.java
    import org.junit.jupiter.api.Test;
    import static org.junit.jupiter.api.Assertions.*;
    class YourTest {
      @Test void example() { assertTrue(true); }
    }
  - Run: ./gradlew test --tests "*YourTest*"
- Spock spec (if needed)
  - Create: src/test/groovy/.../YourSpec.groovy (unit tests) or src/test-architecture/groovy/... (architecture tests)
  - Caveat: For src/test/groovy discovery under current versions, you may need to align groovyVersion/spockVersion. Alternatively, place Spock-based rules under src/test-architecture where the plumbing is already validated.
- Architecture test
  - Add Groovy/Java tests under src/test-architecture/...; they’ll run with: ./gradlew test-architecture
- Filtering
  - Unit tests: -Pfilter and/or --tests as shown above
  - Architecture tests: use --tests with the test-architecture task as needed, e.g.
    ./gradlew test-architecture --tests "*HexagonalArchitectureArchUnitTest*"

4. Development conventions and project-specific notes
- Architectural boundaries
  - Domain model is framework-independent (no Spring annotations inside domain packages), validated by ArchUnit.
  - Aggregates reference other aggregates by ID only; see ADR-003 and ArchUnit tests.
  - Repositories are declared in the domain layer as interfaces (ADR-008), implemented in port adapters.
- DDD building blocks
  - Value objects are implemented as Java records (ADR-009) and are immutable; see de.sample.aiarchitecture.domain.model.ddd.Value and shared types (Price, Money, ProductId, etc.).
  - Domain events are immutable records (ADR-006) and are published via infrastructure.api.DomainEventPublisher; aggregates accumulate and clear events.
- Application layer
  - Application services orchestrate domain logic and publish domain events after repository operations (see ProductApplicationService.updateProductPrice for the canonical flow).
  - Transactions are managed declaratively (see infrastructure/config/TransactionConfiguration.java and docs/architecture/transaction-management.md).
- API layer
  - Spring MVC REST resources under portadapter/primary/api and a web layer using Pug4j templates (see docs/pug4j-integration.md). The template engine is registered via Pug4jConfiguration.
- Security
  - Basic Spring Security setup under infrastructure/config/SecurityConfiguration.java; adapt as needed for endpoints.
- Coding style & nullability
  - Favor records for values, small cohesive aggregates, and explicit domain invariants in constructors/factories.
  - Use org.jspecify.annotations.NonNull to document contracts at boundaries of application/domain services.
- Eventing
  - Infrastructure listeners subscribe to domain events (infrastructure/event/*Listener.java). When mutating aggregates, always publish-and-clear events through the provided publisher.

5. Troubleshooting
- No tests are executed for Spock specs in src/test/groovy
  - Symptom: gradlew test reports 0 tests. Cause: Groovy/Spock version mismatch (Groovy 5 vs Spock for Groovy 4). Workarounds:
    1) Write tests in JUnit 5 (recommended for unit tests), or
    2) Place Spock-based rules under src/test-architecture and run with test-architecture, or
    3) Align versions in gradle/plugins/test-common.gradle (requires build change beyond scope of typical feature work).
- Architecture tests show SKIPPED
  - Many ArchUnit specifications use @Ignore for guidance/optional rules; SKIPPED is expected.
- Boot app fails to start due to template errors
  - Check Pug4j templates in src/main/resources/templates and matching model attributes in controllers.
- Debugging tests
  - Use -Plog-debug to get per-test logs and started/passed events for test tasks.

6. Verified commands (executed during preparation of this guide)
- Unit tests (JUnit): ./gradlew test --tests "*SampleJUnitTest*"
- Architecture tests: ./gradlew test-architecture
- Boot run with debug: ./gradlew bootRun

7. Integration with CLAUDE.md (Assistant and Documentation Workflow)
- Mandatory documentation updates after code changes
  - After any change that affects architecture, patterns, or boundaries, update docs under docs/architecture. This is not optional; see CLAUDE.md.
- When to update
  - Adding/modifying aggregates, entities, value objects, repositories, domain services/events
  - Changing package structures, adapters, or boundaries (hexagonal/onion)
  - Introducing new bounded contexts or cross-context relationships
  - Adjusting application services or transaction management
- Where to update
  - docs/architecture/architecture-principles.md (primary overview)
  - docs/architecture/adr/* for decisions; add a new ADR when making a significant decision
  - docs/architecture/patterns/* for pattern-specific guides (repository, domain-events, aggregates)
  - docs/architecture/bounded-contexts.md if context mapping changes
- How to update (short checklist)
  1) Read current docs and identify affected sections
  2) Update sections with real code examples from this repo
  3) Add/adjust rules and any diagrams as needed
  4) Cross-link files and ensure package names and class references match
  5) If an ADR is added/changed, reference it from principles and related docs
- Verification workflow
  - Run: ./gradlew test-architecture to validate structural rules
  - Run: ./gradlew test (optionally with -Plog-debug) for unit tests
  - Boot: ./gradlew bootRun to sanity-check runtime where applicable
  - Before submitting, confirm the "Documentation Update Checklist" in CLAUDE.md is satisfied (examples synced, references accurate)
- Team norms
  - Do not disable architecture tests to make changes pass; instead, revisit the design or propose ADR updates
  - If a rule is too strict, discuss and document via a new ADR and reflect in tests and docs

Cleanup
- Any temporary sample tests used to validate the instructions (SampleJUnitTest and minimal Groovy specs) were created only for verification and should not be committed long-term. After following the examples, remove your temporary test files unless they provide lasting value.
