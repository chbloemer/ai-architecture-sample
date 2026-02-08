---
name: sprint
description: Execute stories from tasks/prd.json using an Agent Team. Reads the PRD, resolves dependencies, assigns stories to specialized agents, and orchestrates wave-based execution.
argument-hint: "[--all | --epic <name> | --stories US-X,US-Y | --next N] [--dry-run]"
disable-model-invocation: true
---

# Sprint — Agent Team PRD Orchestration

Execute stories from `tasks/prd.json` using an Agent Team. Reads the PRD, resolves dependencies, assigns stories to specialized agents, and orchestrates wave-based execution.

## Arguments

$ARGUMENTS — Optional flags (combinable):

| Flag | Effect |
|------|--------|
| *(none)* | All currently ready stories (satisfied deps only, single wave) |
| `--all` | ALL incomplete stories (rolling waves until all done) |
| `--epic <name>` | Stories from a specific epic |
| `--stories US-128,US-130` | Specific story IDs |
| `--next N` | Target N stories total (rolling waves until N done) |
| `--dry-run` | Show plan without executing |

Examples: `/sprint --dry-run`, `/sprint --epic pricing-context`, `/sprint --next 5`, `/sprint --all --dry-run`

---

## Phase 1: Read PRD and Resolve Dependencies

1. **Read** `tasks/prd.json`
2. **Filter stories** based on arguments:
   - Default (no args): all stories where `passes == false`
   - `--epic <name>`: stories where `epic == <name>` AND `passes == false`
   - `--stories US-X,US-Y`: exactly those story IDs (must have `passes == false`)
   - `--next N`: target N completions total (start with ready ones, wave through deps)
   - `--all`: all `passes == false` stories (wave through deps)
3. **Build dependency graph**: a story is **ready** when ALL stories in its `depends_on` array have `passes: true`
4. **Classify** each filtered story as:
   - **Ready**: all dependencies satisfied — can start immediately
   - **Blocked**: has unsatisfied dependencies — must wait for prerequisite stories
   - **Out of scope**: `passes: true` or filtered out by arguments

5. **Display summary table:**

```
Sprint Plan
═══════════════════════════════════════════
Ready (Wave 1):
  US-131: Story Title                    → ddd-expert
  US-132: Another Story                  → spring-boot-specialist

Blocked (future waves):
  US-133: Depends on US-131              → general-purpose
  US-134: Depends on US-131, US-132      → frontend-developer

Total: 4 stories | Ready: 2 | Blocked: 2
═══════════════════════════════════════════
```

6. **If `--dry-run`**: display the summary and STOP. Do not create team or agents.

---

## Phase 2: Agent Assignment

Map each story to a specialist agent type using `architectural_guidance` from the PRD:

| Signal in `architectural_guidance` | Agent (`subagent_type`) |
|-|-|
| `affected_layers` includes `domain` + patterns mention DDD (Aggregate, Entity, Value Object, Domain Event, Domain Service, Specification, Factory) | `ddd-expert` |
| `affected_layers` includes `infrastructure` + patterns mention migration, upgrade, Spring config, dependency management | `spring-boot-specialist` |
| patterns contain `ArchUnit` or `Architecture Testing` | `arch-compliance` |
| patterns contain `ViewModel`, `BEM`, `Pug`, `CSS`, `ControllerAdvice`, `Template` | `frontend-developer` |
| patterns contain `E2E` or `Playwright` | `e2e-tester` |
| patterns contain `Documentation` or `affected_layers` includes `documentation` | `dca-guardian` |
| **No clear match / multiple layers / ambiguous** | `general-purpose` |

**One story = one task = one agent assignment.** If a story spans multiple concerns, use `general-purpose`.

---

## Phase 3: Create Team and Initial Tasks

1. **TeamCreate:**
   ```
   team_name: "sprint-{YYYYMMDD}"   (e.g., "sprint-20260208")
   description: "Sprint: {N} stories from PRD"
   ```

2. **TaskCreate** for each **ready** story (Wave 1):
   - `subject`: `"US-{id}: {title}"`
   - `description`: Combine all of the following into the task description:
     ```
     ## Story: US-{id}
     **Title:** {title}
     **Epic:** {epic}
     **Description:** {description}

     ## Acceptance Criteria
     {numbered list of acceptance_criteria}

     ## Architectural Guidance
     **Affected Layers:** {affected_layers}
     **Locations:** {locations}
     **Patterns:** {patterns}
     **Constraints:** {constraints}

     ## Project Conventions
     Read the project conventions file for all naming, package structure, and quality rules:
     .claude/skills/sprint/prompt.md
     ```
   - `activeForm`: `"Working on US-{id}"`

3. **TaskCreate** for **blocked** stories (for `--next N` and `--all`):
   - Same format as above
   - Use `addBlockedBy` to set dependencies on the prerequisite task IDs
   - These tasks won't be assigned until their blockers complete

4. **TaskCreate** a final verification task:
   - `subject`: `"Quality Gate: Full Build + All Tests"`
   - `description`: `"Run ./gradlew clean build && ./gradlew test-architecture after all stories complete"`
   - `addBlockedBy`: all story task IDs

---

## Phase 4: Spawn Agents

Spawn agents for currently ready stories. Rules:

- **One instance per agent type** (prevents git conflicts from parallel edits)
- **Max 4 concurrent agents** at any time
- **Only spawn types needed** for current wave's stories

Agent spawn template (use with Task tool):
```
subagent_type: "{agent_type}"
team_name: "sprint-{YYYYMMDD}"
name: "{agent_type}"
mode: "bypassPermissions"

prompt: |
  You are "{agent_type}" on team "sprint-{YYYYMMDD}".

  Read team config: ~/.claude/teams/sprint-{YYYYMMDD}/config.json
  Read project conventions: .claude/skills/sprint/prompt.md

  Workflow:
  1. TaskList → find tasks assigned to you (owner = your name)
  2. TaskGet → read full story details
  3. TaskUpdate → status: in_progress
  4. Implement following acceptance criteria + architectural guidance
  5. Run: ./gradlew build (must pass before completing)
  6. Run: ./gradlew test-architecture (if you touched domain/application/adapter)
  7. TaskUpdate → status: completed
  8. SendMessage to team lead: summary of changes (files modified, patterns, decisions)
  9. TaskList → check for more tasks, or go idle

  Rules:
  - Read .claude/skills/sprint/prompt.md FIRST for all project conventions
  - Follow existing code patterns in the codebase
  - No Spring annotations in domain layer
  - One story at a time, fully complete before moving on
  - If blocked, message team lead immediately with specifics
```

After spawning, **assign tasks** to agents using `TaskUpdate` with `owner` set to the agent's name.

---

## Phase 5: Execute Waves and Monitor

This is the core execution loop. Maintain two counters:
- `target`: N (from `--next N`), infinity (from `--all`), or count of ready stories (default)
- `completed`: 0 (increments as stories finish)

### Wave Loop

1. **Assign** ready tasks to spawned agents via `TaskUpdate` (set `owner`)
2. **Wait** for agents to complete tasks (they will message you)
3. **On each task completion:**
   a. Increment `completed` counter
   b. **Update PRD**: read `tasks/prd.json`, set the completed story's `passes: true`, write back
   c. **Check target**: if `completed >= target` → proceed to Phase 6
   d. **Re-evaluate dependencies**: which blocked stories are now unblocked?
   e. **Create new tasks** for newly ready stories (if using `--next N` or `--all`)
   f. **Assign** new tasks to idle agents, or **spawn** new agent types if needed
4. **Handle blockers**: if an agent reports being stuck:
   - Read their message for specifics
   - Provide guidance via SendMessage
   - If unresolvable, attempt the fix directly or skip the story
5. **Shutdown idle agents** that have no remaining tasks in their specialty
6. **Repeat** until `completed >= target` or no more stories can be unblocked

### Important: PRD Update Format

When marking a story as completed in `tasks/prd.json`:
- Read the file, find the story by ID, set `"passes": true`
- Write the file back (preserve formatting)
- This is critical for dependency resolution in subsequent waves

---

## Phase 6: Quality Verification

After all waves complete (or target reached), run quality checks directly (not via agent):

1. `./gradlew clean build` — compile + unit tests
2. `./gradlew test-architecture` — ArchUnit rules

**If failures occur:**
- Analyze the failure
- Create a fix task and assign to the appropriate agent type
- Re-run verification after fix
- Repeat until green

---

## Phase 7: Commit

Commit all changes in a single commit:
- Stage all modified/new files (but NOT `.env`, credentials, or large binaries)
- Commit message format:
  ```
  US-{id1}, US-{id2}, ...: {brief summary of completed stories}

  Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
  ```

---

## Phase 8: Cleanup

1. **SendMessage** `shutdown_request` to all remaining agents
2. **Wait** for agents to confirm shutdown
3. **TeamDelete** to remove team files
4. **Print final summary:**

```
Sprint Complete
═══════════════════════════════════════════
Completed: {N} stories
  US-131: Story Title                    ✓
  US-132: Another Story                  ✓

Failed/Blocked: {M} stories
  US-133: Story Title                    ✗ (reason)

Waves executed: {W}
Total agents spawned: {A}
═══════════════════════════════════════════
```

## Supporting Files

- For full project conventions (package structure, naming, quality commands, agent protocol), see [prompt.md](prompt.md)
