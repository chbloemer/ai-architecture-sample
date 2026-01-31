# Add User Story to PRD

Add a new user story to the PRD document in the `tasks/` folder.

## Arguments

$ARGUMENTS - The user story details. Can be:
- Just a title/description for interactive mode
- Full details in format: "US-XX: Title | Description | AC1, AC2, AC3"

## Instructions

1. **Read the current PRD files:**
   - `tasks/prd.json` - structured data
   - `tasks/prd-checkout.md` - human-readable format

2. **Determine the next user story ID:**
   - Find the highest existing US-XX number
   - New story ID = highest + 1

3. **If only a title/description was provided, ask for:**
   - Full title (short, descriptive)
   - Description (what and why)
   - Acceptance criteria (testable conditions)

4. **Add explicit DCA architectural guidance** based on the story type:

   **For Domain Layer changes (Aggregates, Entities, Value Objects, Domain Events):**
   - Location: `{context}.domain.model` or `{context}.domain.event`
   - Must implement appropriate marker interface (AggregateRoot, Entity, Value, DomainEvent)
   - No Spring/JPA annotations allowed
   - Domain events named in past tense (e.g., `OrderPlaced`)

   **For Use Case changes:**
   - Location: `{context}.application.{usecasename}/` (lowercase folder)
   - Files: `*InputPort.java`, `*UseCase.java`, `*Command.java` or `*Query.java`, `*Response.java`
   - Use Case must implement InputPort, be annotated with `@Service`
   - Input/Output models use primitives only (no domain objects)

   **For Event Consumer changes (cross-context integration):**
   - Location: `{context}.adapter.incoming.event/*EventListener.java`
   - Must call Use Case via InputPort (never domain directly)
   - Use `@EventListener` or `@TransactionalEventListener(phase = AFTER_COMMIT)`
   - Create Anti-Corruption Layer if translating external events

   **For Repository changes:**
   - Interface: `{context}.application.shared/*Repository.java`
   - Implementation: `{context}.adapter.outgoing.persistence/*`
   - Only for Aggregate Roots (not entities)

   **For Controller changes:**
   - REST: `{context}.adapter.incoming.api/*Resource.java`
   - MVC: `{context}.adapter.incoming.web/*Controller.java`
   - Must call InputPort (not UseCase directly)

5. **Add the new user story to both files:**

   **In prd.json:**
   ```json
   {
     "id": "US-XX",
     "title": "Story Title",
     "description": "Story description",
     "acceptance_criteria": [
       "Criterion 1",
       "Criterion 2"
     ],
     "architectural_guidance": {
       "affected_layers": ["domain", "application", "adapter"],
       "locations": [
         "{context}.adapter.incoming.event/*EventListener"
       ],
       "patterns": ["Event Consumer", "Anti-Corruption Layer"],
       "constraints": [
         "No direct imports from other bounded context domains",
         "Event listener calls use case via InputPort"
       ]
     },
     "passes": false
   }
   ```

   **In prd-checkout.md:**
   ```markdown
   ### US-XX: Story Title
   **As a** developer
   **I want** [what]
   **So that** [why]

   **Acceptance Criteria:**
   - Criterion 1
   - Criterion 2

   **Architectural Guidance:**
   - **Affected Layers:** Domain, Application, Adapter
   - **Locations:**
     - `{context}.adapter.incoming.event/*EventListener`
   - **Patterns:** Event Consumer, Anti-Corruption Layer
   - **Constraints:**
     - No direct imports from other bounded context domains
     - Event listener calls use case via InputPort
     - Run `./gradlew test-architecture` to verify

   ---
   ```

6. **Always include these standard constraints:**
   - "Run `./gradlew test-architecture` to verify architectural compliance"
   - "No Spring annotations in domain layer"
   - For cross-context: "No direct imports from other bounded context domains"

7. **Output confirmation:**
   - Show the new user story ID and title
   - Confirm both files were updated
   - List the architectural guidance added

## Quick Reference: Component Locations

| Component | Location Pattern |
|-----------|-----------------|
| Aggregate | `{context}.domain.model.*` |
| Value Object | `{context}.domain.model.*` |
| Domain Event | `{context}.domain.event.*` |
| Domain Service | `{context}.domain.service.*` |
| Use Case | `{context}.application.{usecasename}/*UseCase` |
| Input Port | `{context}.application.{usecasename}/*InputPort` |
| Command/Query | `{context}.application.{usecasename}/*Command` or `*Query` |
| Response | `{context}.application.{usecasename}/*Response` |
| Repository Interface | `{context}.application.shared/*Repository` |
| Repository Impl | `{context}.adapter.outgoing.persistence/*` |
| REST Controller | `{context}.adapter.incoming.api/*Resource` |
| MVC Controller | `{context}.adapter.incoming.web/*Controller` |
| Event Listener | `{context}.adapter.incoming.event/*EventListener` |

## Example Usage

```
/prd Add payment validation
/prd US-25: Add Payment Validation | Validate payment tokens before processing | Token format validation, Expiry check, Provider verification
```
