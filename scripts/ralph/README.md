# RALPH Loop Scripts

RALPH (named after Ralph Wiggum) is an autonomous AI agent loop that implements PRD stories one at a time until all are complete.

## Prerequisites

```bash
# Install jq for JSON processing
brew install jq

# Ensure Claude Code CLI is installed
# https://claude.ai/code
```

## Quick Start

```bash
# Check current status
./scripts/ralph/status.sh

# Run interactive mode (recommended)
./scripts/ralph/ralph-interactive.sh

# Mark a story complete after implementation
./scripts/ralph/mark-complete.sh US-1 "Implemented all value objects"

# Run automated loop (advanced)
./scripts/ralph/ralph.sh [max_iterations]
```

## Scripts

| Script | Purpose |
|--------|---------|
| `status.sh` | Show PRD progress and next story |
| `ralph-interactive.sh` | Get prompt for next story (manual mode) |
| `mark-complete.sh` | Mark a story as done |
| `ralph.sh` | Automated loop runner |

## Files

| File | Purpose |
|------|---------|
| `tasks/prd.json` | PRD with `passes` field for each story |
| `tasks/prd.md` | Human-readable PRD |
| `tasks/progress.txt` | Learnings and iteration history |

## Workflow

### Interactive Mode (Recommended)

1. Run `./scripts/ralph/status.sh` to see progress
2. Run `./scripts/ralph/ralph-interactive.sh` to get the next task
3. Copy the prompt to Claude Code
4. Implement the story
5. Run `./scripts/ralph/mark-complete.sh US-X "notes"` when done
6. Repeat

### Automated Mode

```bash
# Run up to 10 iterations
./scripts/ralph/ralph.sh 10

# Run with custom iteration count
./scripts/ralph/ralph.sh 25
```

The loop exits when:
- All stories have `passes: true`
- Max iterations reached
- Output includes `<promise>COMPLETE</promise>`

## Completion Signals

When working with Claude Code, use these signals:

```
# Story completed successfully
<promise>STORY_COMPLETE</promise>

# Blocked by something
<promise>BLOCKED: [reason]</promise>

# All stories done
<promise>COMPLETE</promise>
```

## Progress Tracking

Progress is tracked in three places:

1. **prd.json** - `passes: true/false` for each story
2. **progress.txt** - Append-only log of iterations and learnings
3. **git history** - Commits for each completed story

## Tips

- Keep stories small (implementable in one session)
- Run `./gradlew build` before marking complete
- Add learnings to progress.txt for future iterations
- Use `status.sh` frequently to track progress
