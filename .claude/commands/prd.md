# Add User Story to PRD

Add a new user story to the PRD document in the `tasks/` folder.

## Arguments

$ARGUMENTS - The user story details. Can be:
- Just a title/description for interactive mode
- Full details in format: "US-XX: Title | Description | AC1, AC2, AC3"
- With epic: "US-XX: Title | Description | AC1, AC2 | epic:pricing-context"
- With dependencies: "US-XX: Title | Description | AC1, AC2 | depends:US-51,US-52"

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
   - Epic (which feature group this belongs to)
   - Dependencies (which stories must pass first)

4. **Determine the epic:**
   - Check existing epics in prd.json: `jq -r '[.stories[].epic // empty] | unique | .[]' tasks/prd.json`
   - Existing epics: `pricing-context`, `inventory-context`, `cart-resolver`, `checkout-resolver`, `data-migration`
   - If the story fits an existing epic, use it
   - If creating a new feature area, create a new epic name (lowercase, hyphenated)
   - Stories without an epic get `"epic": null` (works with all epics in RALPH/Gastown)

5. **Determine dependencies (`depends_on`):**
   - List story IDs that MUST pass before this story can be started
   - Used by `ralph.sh` and `gastown.sh` to order work
   - If no dependencies, use empty array: `"depends_on": []`
   - Example: A repository implementation depends on the aggregate being created first

6. **Add explicit DCA architectural guidance** based on the story type:

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

7. **Add the new user story to both files:**

   **In prd.json:**
   ```json
   {
     "id": "US-XX",
     "epic": "pricing-context",
     "title": "Story Title",
     "description": "Story description",
     "depends_on": ["US-52", "US-53"],
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
   **Epic:** pricing-context
   **Depends on:** US-52, US-53

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

8. **Always include these standard constraints:**
   - "Run `./gradlew test-architecture` to verify architectural compliance"
   - "No Spring annotations in domain layer"
   - For cross-context: "No direct imports from other bounded context domains"

9. **Output confirmation:**
   - Show the new user story ID, title, and epic
   - Show dependencies if any
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
/prd US-25: Add Payment Validation | Validate payment tokens before processing | Token format validation, Expiry check
/prd US-90: Price History | Track price changes over time | Store old prices, Query history | epic:pricing-context | depends:US-53
```

## Integration with RALPH and Gastown

The `epic` and `depends_on` fields are used by automation scripts:

- **RALPH (`scripts/ralph/ralph.sh`):** Single-agent loop that processes stories sequentially
  - `--epic <name>` flag to focus on one epic
  - Only starts stories whose dependencies have `passes: true`

- **Gastown (`scripts/gastown/gastown.sh`):** Multi-agent orchestration
  - Creates convoys from epics (groups related work)
  - Sets up bead dependencies from `depends_on` field
  - Enables parallel execution across epics
