# Backoffice — Ubiquitous Language (Bootstrap)

> **Bootstrap status:** This glossary was initially derived from the existing code
> (`application/`, `adapter/`). Backoffice currently has no `domain/` model of its
> own — according to `package-info.java` it is an **operational module**, not a
> business Bounded Context. Terms are taken from the application and adapter
> layers. Please review and extend from a business perspective.

## Module Character

Backoffice is an **operational cross-cutting module** for administrative views
(monitoring, event log, future dashboards/admin navigation). Context-specific
admin pages (products, prices, inventory) live in their respective Bounded
Contexts under `/backoffice/{context}/`, not here.

## Concepts

### EventPublicationLog

**Definition:** Logical, append-only audit log of all domain events published by
Spring Modulith, including their processing status. Backoffice reads this log;
writes are performed exclusively by Spring Modulith.

**Type:** Concept (operational data set, not an Aggregate)

**Related terms:** `EventPublicationLogStore`, `EventPublicationEntry`

**Notes:** The log has no Aggregate lifecycle of its own — it is therefore
accessed via a **Store** (not via a Repository).

---

### EventPublicationEntry

**Definition:** A single entry from the event publication log: an event
published to a specific listener, with publication and completion timestamps
and a serialized payload.

**Type:** Value Object (read schema of the Store)

**Identity:** `UUID id` (technical Modulith ID, not a business identifier)

**Related terms:** `EventPublicationLogStore`, `EventPublicationSummary`

**Operations:**
- `isCompleted()` — did the listener complete successfully?
- `shortEventType()` — simple class name of the event type

**Notes:** Belongs to the Store as its **read schema** and is deliberately
decoupled from the use-case API (see `EventPublicationSummary`).

---

### EventPublicationSummary

**Definition:** Application-layer view of an event publication entry. Exposes
exactly the fields the view needs and decouples the use-case API from the
Store's read schema.

**Type:** Value Object (use-case result payload)

**Identity:** `UUID id`

**Synonyms (avoid):** "EventPublication", "event entry" — please consistently
distinguish `EventPublicationSummary` (application layer) from
`EventPublicationEntry` (store layer).

**Related terms:** `EventPublicationEntry`, `GetEventPublicationsResult`

**Operations:**
- `from(EventPublicationEntry)` — mapping from the Store entry
- `isCompleted()`, `shortEventType()`

**Notes:** **Clarification Entry vs. Summary:**
- `EventPublicationEntry` = persistence-near representation returned by the
  Store (owned by the port `EventPublicationLogStore`).
- `EventPublicationSummary` = view-oriented representation returned by the
  use case (owned by the use case). Intentional duplication so the two
  contracts can evolve independently.

---

### Completion Status

**Definition:** Status of an event publication: **completed** when the listener
has processed the event successfully (`completionDate != null`), otherwise
**incomplete**.

**Type:** Concept (derived attribute)

**Related terms:** `EventPublicationEntry.isCompleted()`,
`GetEventPublicationsResult.completedCount` / `incompleteCount`

---

## Store / Repository — Distinction

**Store** is used in Backoffice instead of **Repository** because the data has
**no Aggregate lifecycle**:

| Aspect          | Repository                                | Store (here)                                    |
|-----------------|-------------------------------------------|-------------------------------------------------|
| Lifecycle       | Load → mutate → save                      | Append-only, written externally                 |
| Content         | Aggregate Roots                           | Operational entries without business identity   |
| Operations      | `findById`, `save`, `deleteById`          | `findAll`, `count`, `record(...)` (no save)     |
| Example         | `OrderRepository`                         | `EventPublicationLogStore`                      |

Backoffice does **not** write to the log — Spring Modulith owns the
`EVENT_PUBLICATION` table. The `JdbcEventPublicationLogStore` is
**read-only**.

---

## Ports & Use Cases

### EventPublicationLogStore (Output Port)

**Definition:** Read port onto the Spring Modulith event log.

**Type:** Output Port (Store marker)

**Operations:** `findAll(): List<EventPublicationEntry>`

**Notes:** Implementation: `JdbcEventPublicationLogStore` (reads directly from
the `EVENT_PUBLICATION` table).

---

### GetEventPublications (Use Case)

**Definition:** Returns all event publications with processing status and
aggregated statistics (`totalCount`, `completedCount`, `incompleteCount`) for
the Backoffice overview page.

**Type:** Use Case (Query)

**Related terms:** `GetEventPublicationsQuery`, `GetEventPublicationsResult`,
`EventPublicationSummary`

**Notes:** Query is currently parameterless. Possible extensions: filter by
event type, time range, completion status.

---

## Open Questions

- Should the term **Event Publication** be promoted to the Shared Kernel
  glossary? (It will likely be relevant for other operational views as well.)
- Planned extensions (retry, republish) — will these live in Backoffice or in
  Spring Modulith configuration?
- Should Backoffice become its own Bounded Context in the long term, or remain
  classified as a Generic Subdomain?
