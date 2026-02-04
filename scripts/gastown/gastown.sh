#!/bin/bash
#
# Gastown PRD Integration - Multi-Agent Story Execution
# Uses epic field for convoys, depends_on for execution order
#
# Usage:
#   ./gastown.sh init     - Create beads and convoys from prd.json
#   ./gastown.sh status   - Show convoy progress and ready beads
#   ./gastown.sh sling    - Assign ready beads to polecats
#   ./gastown.sh sync     - Sync bead status back to prd.json
#   ./gastown.sh reset    - Clear all beads and start fresh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PRD_FILE="$PROJECT_ROOT/tasks/prd.json"
BEAD_MAP_FILE="$PROJECT_ROOT/tasks/.bead-map.json"
RIG_NAME="ai_architecture_sample"
BEAD_PREFIX="aas"
GT_DIR="$HOME/gt"

# Run gt/bd commands from gastown directory
gt_cmd() {
    (cd "$GT_DIR" && gt "$@")
}

bd_cmd() {
    (cd "$GT_DIR" && bd "$@")
}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[GASTOWN]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[GASTOWN]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[GASTOWN]${NC} $1"
}

log_error() {
    echo -e "${RED}[GASTOWN]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    local missing=false

    if [ ! -f "$PRD_FILE" ]; then
        log_error "PRD file not found: $PRD_FILE"
        missing=true
    fi

    if ! command -v jq &> /dev/null; then
        log_error "jq is required but not installed. Install with: brew install jq"
        missing=true
    fi

    if ! command -v bd &> /dev/null; then
        log_error "beads CLI (bd) not found. Please install gastown beads."
        missing=true
    fi

    if ! command -v gt &> /dev/null; then
        log_error "gastown CLI (gt) not found. Please install gastown."
        missing=true
    fi

    if [ ! -d "$GT_DIR" ]; then
        log_error "Gastown directory not found: $GT_DIR"
        missing=true
    fi

    if [ "$missing" = true ]; then
        exit 1
    fi
}

# Initialize beads from PRD
init_beads() {
    log_info "Creating beads from prd.json..."

    # Initialize bead map (story_id -> bead_id)
    echo "{}" > "$BEAD_MAP_FILE"

    # Get incomplete stories with epic/depends_on fields (the new stories US-52+)
    local stories=$(jq -r '.stories[] | select(.passes == false and .epic != null) | @base64' "$PRD_FILE")

    local count=0
    for story in $stories; do
        local id=$(echo "$story" | base64 -d | jq -r '.id')
        local title=$(echo "$story" | base64 -d | jq -r '.title')
        local desc=$(echo "$story" | base64 -d | jq -r '.description')
        local criteria=$(echo "$story" | base64 -d | jq -r '.acceptance_criteria | map("- " + .) | join("\n")')
        local epic=$(echo "$story" | base64 -d | jq -r '.epic // "default"')
        local guidance=$(echo "$story" | base64 -d | jq -r 'if .architectural_guidance then "Affected layers: " + (.architectural_guidance.affected_layers | join(", ")) + "\nLocations: " + (.architectural_guidance.locations | join(", ")) + "\nPatterns: " + (.architectural_guidance.patterns | join(", ")) else "" end')

        # Build description for bead
        local bead_desc="Story: $id
Epic: $epic

$desc

Acceptance Criteria:
$criteria"

        if [ -n "$guidance" ]; then
            bead_desc="$bead_desc

Architectural Guidance:
$guidance"
        fi

        # Create bead
        local bead_id=$(bd_cmd create "$title" \
            --type task \
            --rig "$RIG_NAME" \
            --description "$bead_desc" 2>&1 | grep -oE "${BEAD_PREFIX}-[a-z0-9]+" | head -1)

        if [ -n "$bead_id" ]; then
            # Store mapping
            local tmp_file=$(mktemp)
            jq --arg sid "$id" --arg bid "$bead_id" \
                '. + {($sid): $bid}' "$BEAD_MAP_FILE" > "$tmp_file" && mv "$tmp_file" "$BEAD_MAP_FILE"

            log_success "Created $bead_id for $id ($title)"
            count=$((count + 1))
        else
            log_warn "Failed to create bead for $id"
        fi
    done

    log_info "Created $count beads"
}

# Set up dependencies between beads
setup_dependencies() {
    log_info "Setting up dependencies..."

    local stories=$(jq -r '.stories[] | select(.passes == false and .epic != null and .depends_on != null and (.depends_on | length) > 0) | @base64' "$PRD_FILE")

    local count=0
    for story in $stories; do
        local id=$(echo "$story" | base64 -d | jq -r '.id')
        local deps=$(echo "$story" | base64 -d | jq -r '.depends_on[]')

        local bead_id=$(jq -r --arg id "$id" '.[$id] // empty' "$BEAD_MAP_FILE")
        if [ -z "$bead_id" ]; then
            continue
        fi

        for dep in $deps; do
            local dep_bead=$(jq -r --arg id "$dep" '.[$id] // empty' "$BEAD_MAP_FILE")
            if [ -n "$dep_bead" ]; then
                bd_cmd dep "$dep_bead" --blocks "$bead_id" 2>/dev/null || true
                log_info "  $dep_bead blocks $bead_id ($dep -> $id)"
                count=$((count + 1))
            fi
        done
    done

    log_info "Set up $count dependencies"
}

# Create convoys by epic
create_convoys() {
    log_info "Creating convoys by epic..."

    # Get unique epics
    local epics=$(jq -r '[.stories[] | select(.passes == false and .epic != null) | .epic] | unique | .[]' "$PRD_FILE")

    for epic in $epics; do
        # Get all bead IDs for this epic
        local story_ids=$(jq -r --arg epic "$epic" \
            '.stories[] | select(.passes == false and .epic == $epic) | .id' "$PRD_FILE")

        local bead_ids=""
        for sid in $story_ids; do
            local bid=$(jq -r --arg id "$sid" '.[$id] // empty' "$BEAD_MAP_FILE")
            if [ -n "$bid" ]; then
                bead_ids="$bead_ids $bid"
            fi
        done

        if [ -n "$bead_ids" ]; then
            gt_cmd convoy create "$epic" $bead_ids 2>/dev/null || log_warn "Failed to create convoy '$epic'"
            log_success "Created convoy '$epic' with:$bead_ids"
        fi
    done
}

# Get ready beads (all epics)
get_ready_beads() {
    bd_cmd ready --rig "$RIG_NAME" 2>/dev/null || true
}

# Get ready beads for specific epics
get_ready_beads_for_epics() {
    local epics=("$@")
    local all_bead_ids=""

    for epic in "${epics[@]}"; do
        # Get story IDs for this epic
        local story_ids=$(jq -r --arg epic "$epic" \
            '.stories[] | select(.passes == false and .epic == $epic) | .id' "$PRD_FILE")

        # Get corresponding bead IDs
        for sid in $story_ids; do
            local bid=$(jq -r --arg id "$sid" '.[$id] // empty' "$BEAD_MAP_FILE")
            [ -n "$bid" ] && all_bead_ids="$all_bead_ids|$bid"
        done
    done

    # Remove leading pipe
    all_bead_ids="${all_bead_ids#|}"

    # Filter to only ready beads
    if [ -n "$all_bead_ids" ]; then
        bd_cmd ready --rig "$RIG_NAME" 2>/dev/null | grep -E "($all_bead_ids)" || true
    fi
}

# Show status
show_status() {
    echo ""
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║              Gastown PRD Status                              ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""

    # Overall progress
    local total=$(jq '[.stories[] | select(.epic != null)] | length' "$PRD_FILE")
    local passed=$(jq '[.stories[] | select(.epic != null and .passes == true)] | length' "$PRD_FILE")
    local remaining=$((total - passed))
    echo -e "Overall: ${GREEN}$passed${NC}/$total passed (${YELLOW}$remaining${NC} remaining)"
    echo ""

    # Epic breakdown
    echo "Epics/Convoys:"
    echo "──────────────────────────────────────────────────────────────"
    printf "%-25s %8s %8s %8s %8s\n" "EPIC" "PASSED" "PENDING" "READY" "TOTAL"
    echo "──────────────────────────────────────────────────────────────"

    # Get unique epics
    local epics=$(jq -r '[.stories[] | select(.epic != null) | .epic] | unique | .[]' "$PRD_FILE")

    for epic in $epics; do
        local epic_total=$(jq --arg e "$epic" '[.stories[] | select(.epic == $e)] | length' "$PRD_FILE")
        local epic_passed=$(jq --arg e "$epic" '[.stories[] | select(.epic == $e and .passes == true)] | length' "$PRD_FILE")
        local epic_pending=$((epic_total - epic_passed))

        # Count ready beads for this epic
        local epic_ready=0
        if [ -f "$BEAD_MAP_FILE" ]; then
            epic_ready=$(get_ready_beads_for_epics "$epic" 2>/dev/null | wc -l | tr -d ' ')
        fi

        printf "%-25s %8d %8d %8d %8d\n" "$epic" "$epic_passed" "$epic_pending" "$epic_ready" "$epic_total"
    done

    echo "──────────────────────────────────────────────────────────────"
    echo ""

    # Show ready beads
    if [ -f "$BEAD_MAP_FILE" ]; then
        local ready_beads=$(get_ready_beads 2>/dev/null)
        local ready_count=$(echo "$ready_beads" | grep -c . 2>/dev/null || echo "0")

        echo -e "Ready to sling: ${GREEN}$ready_count${NC} bead(s)"

        if [ -n "$ready_beads" ] && [ "$ready_count" -gt 0 ]; then
            echo ""
            echo "Ready beads:"
            echo "$ready_beads" | while read -r bead; do
                if [ -n "$bead" ]; then
                    # Get story ID for this bead
                    local story_id=$(jq -r --arg bid "$bead" 'to_entries[] | select(.value == $bid) | .key' "$BEAD_MAP_FILE" 2>/dev/null)
                    local title=$(jq -r --arg id "$story_id" '.stories[] | select(.id == $id) | .title' "$PRD_FILE" 2>/dev/null)
                    echo -e "  ${CYAN}$bead${NC} ($story_id): $title"
                fi
            done
        fi
    else
        log_warn "Bead map not found. Run 'init' first."
    fi
}

# Sling ready beads
sling_ready() {
    local convoy_filters=()
    local max_parallel=3
    local sling_all=false

    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --convoy)
                convoy_filters+=("$2")
                shift 2
                ;;
            --parallel)
                max_parallel="$2"
                shift 2
                ;;
            --all)
                sling_all=true
                shift
                ;;
            *)
                shift
                ;;
        esac
    done

    local ready_beads
    if [ ${#convoy_filters[@]} -gt 0 ]; then
        # Get ready beads from specified convoy(s)
        ready_beads=$(get_ready_beads_for_epics "${convoy_filters[@]}")
        log_info "Filtering by convoys: ${convoy_filters[*]}"
    else
        # Get ready beads from all epics
        ready_beads=$(get_ready_beads)
    fi

    # Apply limit unless --all specified
    if [ "$sling_all" = false ]; then
        ready_beads=$(echo "$ready_beads" | head -n "$max_parallel")
    fi

    if [ -z "$ready_beads" ]; then
        log_warn "No beads ready to sling"
        [ ${#convoy_filters[@]} -gt 0 ] && echo "(filtered by convoys: ${convoy_filters[*]})"
        return 0
    fi

    local bead_count=$(echo "$ready_beads" | grep -c . 2>/dev/null || echo "0")
    log_info "Slinging $bead_count bead(s) to polecats..."

    # Convert newlines to spaces for gt_cmd sling
    local bead_list=$(echo "$ready_beads" | tr '\n' ' ')

    gt_cmd sling $bead_list "$RIG_NAME"
    log_success "Slung beads: $bead_list"
}

# Sync status from beads back to prd.json
sync_status() {
    log_info "Syncing bead status to prd.json..."

    if [ ! -f "$BEAD_MAP_FILE" ]; then
        log_error "Bead map not found. Run 'init' first."
        return 1
    fi

    bd_cmd sync 2>/dev/null || true

    # Get closed beads
    local closed=$(bd_cmd list --status closed --rig "$RIG_NAME" 2>/dev/null | grep -oE "${BEAD_PREFIX}-[a-z0-9]+" || true)

    local count=0
    for bead_id in $closed; do
        # Find story ID from map
        local story_id=$(jq -r --arg bid "$bead_id" 'to_entries[] | select(.value == $bid) | .key' "$BEAD_MAP_FILE" 2>/dev/null)

        if [ -n "$story_id" ] && [ "$story_id" != "null" ]; then
            # Check if already passed
            local already_passed=$(jq -r --arg id "$story_id" '.stories[] | select(.id == $id) | .passes' "$PRD_FILE")
            if [ "$already_passed" != "true" ]; then
                # Mark story as passed in prd.json
                local tmp_file=$(mktemp)
                jq --arg id "$story_id" '(.stories[] | select(.id == $id) | .passes) = true' "$PRD_FILE" > "$tmp_file"
                mv "$tmp_file" "$PRD_FILE"
                log_success "Marked $story_id as passed"
                count=$((count + 1))
            fi
        fi
    done

    log_info "Synced $count stories"
}

# Reset all beads
reset_beads() {
    log_warn "This will delete all beads for rig '$RIG_NAME'"
    echo -n "Are you sure? (y/N) "
    read -r confirm

    if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
        log_info "Aborted"
        return 0
    fi

    log_info "Deleting beads..."

    # Delete all beads for this rig
    bd_cmd list --rig "$RIG_NAME" 2>/dev/null | grep -oE "${BEAD_PREFIX}-[a-z0-9]+" | while read -r bead_id; do
        bd_cmd delete "$bead_id" 2>/dev/null || true
        log_info "Deleted $bead_id"
    done

    # Delete convoys
    local epics=$(jq -r '[.stories[] | select(.epic != null) | .epic] | unique | .[]' "$PRD_FILE")
    for epic in $epics; do
        gt_cmd convoy delete "$epic" 2>/dev/null || true
        log_info "Deleted convoy $epic"
    done

    # Remove bead map
    rm -f "$BEAD_MAP_FILE"

    log_success "Reset complete"
}

# Show usage
show_usage() {
    cat << 'EOF'
Gastown PRD Integration - Multi-Agent Story Execution

Usage: ./gastown.sh <command> [options]

Commands:
  init                  Create beads and convoys from prd.json
  status                Show epic/convoy progress table and ready beads
  sling [options]       Assign ready beads to polecats
  sync                  Sync bead status back to prd.json
  reset                 Clear all beads and start fresh

Sling Options:
  --parallel <n>        Max number of beads to sling (default: 3)
  --all                 Sling ALL ready beads (no limit)
  --convoy <name>       Filter to specific epic/convoy (can be repeated)

Examples:
  ./gastown.sh init
  ./gastown.sh status
  ./gastown.sh sling --parallel 4                                    # 4 beads from all epics
  ./gastown.sh sling --all                                           # ALL ready beads
  ./gastown.sh sling --convoy pricing-context                        # One epic
  ./gastown.sh sling --convoy pricing-context --convoy inventory-context  # Multiple epics
  ./gastown.sh sling --convoy pricing-context --all                  # ALL ready in epic

Workflow:
  1. ./gastown.sh init      # Create beads from prd.json
  2. ./gastown.sh status    # See what's ready
  3. ./gastown.sh sling     # Assign to polecats
  4. [polecats work]
  5. ./gastown.sh sync      # Update prd.json with completions
  6. Repeat 2-5
EOF
}

# Main
main() {
    case "${1:-}" in
        init)
            check_prerequisites
            init_beads
            setup_dependencies
            create_convoys
            log_success "Initialization complete!"
            ;;
        status)
            show_status
            ;;
        sling)
            shift
            check_prerequisites
            sling_ready "$@"
            ;;
        sync)
            check_prerequisites
            sync_status
            ;;
        reset)
            check_prerequisites
            reset_beads
            ;;
        help|--help|-h)
            show_usage
            ;;
        *)
            show_usage
            ;;
    esac
}

main "$@"
