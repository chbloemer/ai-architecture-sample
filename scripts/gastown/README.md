# Gastown PRD Integration

Multi-agent story execution using gastown/beads tooling.

## Overview

Converts PRD stories to beads, groups them by epic into convoys, and manages parallel execution using depends_on for ordering.

## Prerequisites

- `jq` - JSON processor
- `bd` - Beads CLI
- `gt` - Gastown CLI
- Gastown directory at `~/gt/`
- Rig configured as `ai_architecture_sample`

The script automatically runs gt/bd commands from `~/gt/` where rigs are configured.

## Usage

```bash
# Initialize beads and convoys from prd.json
./gastown.sh init

# Show progress and ready beads
./gastown.sh status

# Sling ready beads to polecats
./gastown.sh sling                                    # Default: 3 beads
./gastown.sh sling --parallel 4                       # 4 beads from all epics
./gastown.sh sling --all                              # ALL ready beads
./gastown.sh sling --convoy pricing-context           # One epic only
./gastown.sh sling --convoy pricing-context --convoy inventory-context  # Multiple epics

# Sync completed beads back to prd.json
./gastown.sh sync

# Reset and start fresh
./gastown.sh reset
```

## Workflow

```
prd.json ─────┬──────> bd create (beads)
              │
epic field ───┴──────> gt convoy create (convoys)
              │
depends_on ───┴──────> bd dep (dependencies)
              │
              ▼
        ./gastown.sh sling
              │
              ▼
        Polecats work on beads
              │
              ▼
        ./gastown.sh sync
              │
              ▼
        prd.json updated (passes=true)
```

## PRD Fields Used

| Field | Usage |
|-------|-------|
| `id` | Story identifier (US-52, etc.) |
| `title` | Bead title |
| `description` | Bead description |
| `acceptance_criteria` | Included in bead description |
| `epic` | Groups stories into convoys |
| `depends_on` | Sets bead dependencies |
| `passes` | Completion status |

## Convoy Structure

Stories are grouped by epic:

- `pricing-context` - US-52 to US-60
- `inventory-context` - US-61 to US-69
- `cart-resolver` - US-70 to US-76
- `checkout-resolver` - US-77 to US-83
- `data-migration` - US-84 to US-87

## Parallel Execution

Stories without dependencies can run in parallel:

```bash
# Initial parallel start (no deps):
# US-52 (pricing), US-61 (inventory), US-70 (cart), US-77 (checkout)
./gastown.sh sling --parallel 4
```

After dependencies complete, more stories become ready.

## Files

| File | Purpose |
|------|---------|
| `gastown.sh` | Main integration script |
| `tasks/.bead-map.json` | Story ID → Bead ID mapping (gitignored) |
