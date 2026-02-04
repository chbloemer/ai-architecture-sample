#!/bin/bash
#
# RALPH Loop - Autonomous AI Agent for PRD Implementation
# Runs Claude Code repeatedly until all PRD stories pass
#
# Usage: ./ralph.sh [max_iterations] [--verbose|-v] [--epic <epic-name>]
#
# Options:
#   --epic <name>    Only work on stories from specified epic
#   --verbose, -v    Show detailed debug output
#   [number]         Maximum iterations (default: 10)
#

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PRD_FILE="$PROJECT_ROOT/tasks/prd.json"
PROGRESS_FILE="$PROJECT_ROOT/tasks/progress.txt"
LOG_DIR="$PROJECT_ROOT/tasks/logs"
PROMPT_FILE="$SCRIPT_DIR/prompt.md"
HEARTBEAT_INTERVAL=30  # seconds between heartbeat messages
TIMEOUT_WARNING=300    # warn after 5 minutes of no output

# Parse arguments
MAX_ITERATIONS=10
VERBOSE=false
EPIC_FILTER=""
while [[ $# -gt 0 ]]; do
    case $1 in
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        --epic)
            EPIC_FILTER="$2"
            shift 2
            ;;
        [0-9]*)
            MAX_ITERATIONS=$1
            shift
            ;;
        *)
            shift
            ;;
    esac
done

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[RALPH]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[RALPH]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[RALPH]${NC} $1"
}

log_error() {
    echo -e "${RED}[RALPH]${NC} $1"
}

log_verbose() {
    if [ "$VERBOSE" = true ]; then
        echo -e "${BLUE}[RALPH DEBUG]${NC} $1"
    fi
}

# Format elapsed time as HH:MM:SS
format_elapsed() {
    local seconds=$1
    printf "%02d:%02d:%02d" $((seconds/3600)) $((seconds%3600/60)) $((seconds%60))
}

# Heartbeat monitor - runs in background to show progress
HEARTBEAT_PID=""
start_heartbeat() {
    local story_id="$1"
    local start_time=$(date +%s)
    local output_file="$2"

    (
        while true; do
            sleep $HEARTBEAT_INTERVAL
            local now=$(date +%s)
            local elapsed=$((now - start_time))
            local elapsed_fmt=$(format_elapsed $elapsed)

            # Check if claude process is still running
            if pgrep -f "claude.*--print" > /dev/null 2>&1; then
                local cpu=$(ps -p $(pgrep -f "claude.*--print" | head -1) -o %cpu= 2>/dev/null || echo "?")
                local output_size=$(wc -c < "$output_file" 2>/dev/null | tr -d ' ' || echo "0")
                echo -e "${YELLOW}[RALPH ⏱ $elapsed_fmt]${NC} Still working on $story_id (CPU: ${cpu}%, Output: ${output_size} bytes)"

                # Timeout warning
                if [ $elapsed -gt $TIMEOUT_WARNING ]; then
                    echo -e "${YELLOW}[RALPH ⚠️]${NC} Claude has been running for over 5 minutes. Check if stuck."
                fi
            else
                break
            fi
        done
    ) &
    HEARTBEAT_PID=$!
}

stop_heartbeat() {
    if [ -n "$HEARTBEAT_PID" ]; then
        kill $HEARTBEAT_PID 2>/dev/null || true
        wait $HEARTBEAT_PID 2>/dev/null || true
        HEARTBEAT_PID=""
    fi
}

# Cleanup on exit
cleanup() {
    stop_heartbeat
}
trap cleanup EXIT

# Check prerequisites
check_prerequisites() {
    if [ ! -f "$PRD_FILE" ]; then
        log_error "PRD file not found: $PRD_FILE"
        exit 1
    fi

    if ! command -v jq &> /dev/null; then
        log_error "jq is required but not installed. Install with: brew install jq"
        exit 1
    fi

    if ! command -v claude &> /dev/null; then
        log_error "Claude Code CLI not found. Install from: https://claude.ai/code"
        exit 1
    fi
}

# Check if all dependencies of a story are satisfied (passed)
check_dependencies_satisfied() {
    local story_id="$1"

    # Get the depends_on array for this story
    local deps=$(jq -r --arg id "$story_id" '.stories[] | select(.id == $id) | .depends_on // [] | .[]' "$PRD_FILE" 2>/dev/null)

    # If no dependencies, return success
    if [ -z "$deps" ]; then
        return 0
    fi

    # Check each dependency
    for dep in $deps; do
        local dep_passed=$(jq -r --arg id "$dep" '.stories[] | select(.id == $id) | .passes' "$PRD_FILE" 2>/dev/null)
        if [ "$dep_passed" != "true" ]; then
            return 1  # Dependency not satisfied
        fi
    done

    return 0  # All dependencies satisfied
}

# Get story epic
get_story_epic() {
    local story_id="$1"
    jq -r --arg id "$story_id" '.stories[] | select(.id == $id) | .epic // "default"' "$PRD_FILE"
}

# Check if story matches epic filter (if set)
matches_epic_filter() {
    local story_id="$1"

    # If no filter set, all stories match
    if [ -z "$EPIC_FILTER" ]; then
        return 0
    fi

    local story_epic=$(get_story_epic "$story_id")
    [ "$story_epic" = "$EPIC_FILTER" ]
}

# Get next incomplete story with satisfied dependencies (filtered by epic if set)
get_next_story() {
    # Get all incomplete stories
    local incomplete_stories=$(jq -r '.stories[] | select(.passes == false) | .id' "$PRD_FILE")

    # Find first one with all dependencies satisfied and matching epic filter
    for story_id in $incomplete_stories; do
        if matches_epic_filter "$story_id" && check_dependencies_satisfied "$story_id"; then
            echo "$story_id"
            return 0
        fi
    done

    # No ready stories found
    return 1
}

# Get count of stories ready to work on (incomplete with satisfied deps, filtered by epic)
count_ready() {
    local count=0
    local incomplete_stories=$(jq -r '.stories[] | select(.passes == false) | .id' "$PRD_FILE")

    for story_id in $incomplete_stories; do
        if matches_epic_filter "$story_id" && check_dependencies_satisfied "$story_id"; then
            count=$((count + 1))
        fi
    done

    echo "$count"
}

# Get count of stories blocked by dependencies (filtered by epic)
count_blocked() {
    local count=0
    local incomplete_stories=$(jq -r '.stories[] | select(.passes == false) | .id' "$PRD_FILE")

    for story_id in $incomplete_stories; do
        if matches_epic_filter "$story_id" && ! check_dependencies_satisfied "$story_id"; then
            count=$((count + 1))
        fi
    done

    echo "$count"
}

# Count completed stories (filtered by epic)
count_completed_filtered() {
    if [ -z "$EPIC_FILTER" ]; then
        jq '[.stories[] | select(.passes == true)] | length' "$PRD_FILE"
    else
        jq --arg epic "$EPIC_FILTER" '[.stories[] | select(.passes == true and (.epic // "default") == $epic)] | length' "$PRD_FILE"
    fi
}

# Count total stories (filtered by epic)
count_total_filtered() {
    if [ -z "$EPIC_FILTER" ]; then
        jq '.stories | length' "$PRD_FILE"
    else
        jq --arg epic "$EPIC_FILTER" '[.stories[] | select((.epic // "default") == $epic)] | length' "$PRD_FILE"
    fi
}

# Check if all stories are complete (filtered by epic)
all_complete_filtered() {
    if [ -z "$EPIC_FILTER" ]; then
        local incomplete=$(jq '[.stories[] | select(.passes == false)] | length' "$PRD_FILE")
    else
        local incomplete=$(jq --arg epic "$EPIC_FILTER" '[.stories[] | select(.passes == false and (.epic // "default") == $epic)] | length' "$PRD_FILE")
    fi
    [ "$incomplete" -eq 0 ]
}

# List unique epics with their status
show_epic_status() {
    echo ""
    log_info "Epic Status:"
    log_info "──────────────────────────────────────────────────────────"
    printf "  %-25s %8s %8s %8s %8s\n" "EPIC" "PASSED" "READY" "BLOCKED" "TOTAL"
    log_info "──────────────────────────────────────────────────────────"

    local epics=$(jq -r '[.stories[] | .epic // "default"] | unique | .[]' "$PRD_FILE")

    for epic in $epics; do
        local epic_total=$(jq --arg e "$epic" '[.stories[] | select((.epic // "default") == $e)] | length' "$PRD_FILE")
        local epic_passed=$(jq --arg e "$epic" '[.stories[] | select((.epic // "default") == $e and .passes == true)] | length' "$PRD_FILE")

        # Count ready and blocked for this epic
        local epic_ready=0
        local epic_blocked=0
        local epic_stories=$(jq -r --arg e "$epic" '.stories[] | select((.epic // "default") == $e and .passes == false) | .id' "$PRD_FILE")
        for sid in $epic_stories; do
            if check_dependencies_satisfied "$sid"; then
                epic_ready=$((epic_ready + 1))
            else
                epic_blocked=$((epic_blocked + 1))
            fi
        done

        # Highlight current epic filter
        local prefix="  "
        if [ "$epic" = "$EPIC_FILTER" ]; then
            prefix="▶ "
        fi

        printf "${prefix}%-25s %8d %8d %8d %8d\n" "$epic" "$epic_passed" "$epic_ready" "$epic_blocked" "$epic_total"
    done
    log_info "──────────────────────────────────────────────────────────"
}

# Get story title
get_story_title() {
    local story_id="$1"
    jq -r --arg id "$story_id" '.stories[] | select(.id == $id) | .title' "$PRD_FILE"
}

# Get story description
get_story_description() {
    local story_id="$1"
    jq -r --arg id "$story_id" '.stories[] | select(.id == $id) | .description' "$PRD_FILE"
}

# Get story acceptance criteria
get_story_criteria() {
    local story_id="$1"
    jq -r --arg id "$story_id" '.stories[] | select(.id == $id) | .acceptance_criteria | join("\n- ")' "$PRD_FILE"
}

# Count completed stories
count_completed() {
    jq '[.stories[] | select(.passes == true)] | length' "$PRD_FILE"
}

# Count total stories
count_total() {
    jq '.stories | length' "$PRD_FILE"
}

# Check if all stories are complete
all_complete() {
    local incomplete=$(jq '[.stories[] | select(.passes == false)] | length' "$PRD_FILE")
    [ "$incomplete" -eq 0 ]
}

# Mark story as complete
mark_complete() {
    local story_id="$1"
    local tmp_file=$(mktemp)
    jq --arg id "$story_id" '(.stories[] | select(.id == $id) | .passes) = true' "$PRD_FILE" > "$tmp_file"
    mv "$tmp_file" "$PRD_FILE"
    log_success "Marked $story_id as complete"
}

# Append to progress file
append_progress() {
    local story_id="$1"
    local status="$2"
    local notes="$3"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "" >> "$PROGRESS_FILE"
    echo "## [$timestamp] $story_id - $status" >> "$PROGRESS_FILE"
    if [ -n "$notes" ]; then
        echo "$notes" >> "$PROGRESS_FILE"
    fi
}

# Build the prompt for Claude
build_prompt() {
    local story_id="$1"
    local story_title=$(get_story_title "$story_id")
    local story_desc=$(get_story_description "$story_id")
    local story_criteria=$(get_story_criteria "$story_id")
    local completed=$(count_completed)
    local total=$(count_total)

    cat << EOF
# RALPH Loop - Iteration Task

## Current Progress
- Completed: $completed / $total stories
- Working on: $story_id - $story_title

## Your Task
Implement the following user story:

**$story_id: $story_title**

$story_desc

**Acceptance Criteria:**
- $story_criteria

## Instructions

1. Read the existing code to understand patterns and conventions
2. Implement ONLY this story - do not work on other stories
3. Follow DDD patterns used in the project
4. Run tests after implementation: \`./gradlew build\`
5. If all acceptance criteria are met, output: \`<promise>STORY_COMPLETE</promise>\`
6. If you encounter blockers, output: \`<promise>BLOCKED: [reason]</promise>\`

## Context from Previous Iterations
$(cat "$PROGRESS_FILE" 2>/dev/null | tail -50 || echo "No previous progress")

## Quality Gates
- Code compiles: \`./gradlew build -x test\`
- Tests pass: \`./gradlew test\`
- Architecture tests pass: \`./gradlew test-architecture\`

Remember: Focus on THIS story only. Small, incremental progress.
EOF
}

# Run single iteration
run_iteration() {
    local iteration="$1"
    local story_id=$(get_next_story)

    if [ -z "$story_id" ]; then
        log_success "All stories complete!"
        return 1
    fi

    local story_title=$(get_story_title "$story_id")
    local story_desc=$(get_story_description "$story_id")
    local story_epic=$(get_story_epic "$story_id")

    echo ""
    log_info "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    log_info "Iteration $iteration: $story_id"
    log_info "Title: $story_title"
    log_info "Epic: $story_epic"
    log_info "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    # Build prompt
    local prompt=$(build_prompt "$story_id")

    # Create temp file for prompt
    local prompt_file=$(mktemp)
    echo "$prompt" > "$prompt_file"

    # Show prompt in verbose mode
    if [ "$VERBOSE" = true ]; then
        log_verbose "Prompt being sent to Claude:"
        echo "----------------------------------------"
        echo "$prompt" | head -30
        echo "... (truncated, see full prompt in verbose mode)"
        echo "----------------------------------------"
    fi

    # Run Claude Code
    log_info "Starting Claude Code session at $(date '+%H:%M:%S')..."

    cd "$PROJECT_ROOT"

    # Create persistent log file
    mkdir -p "$LOG_DIR"
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    local output_file="$LOG_DIR/${story_id}_${timestamp}.log"
    log_info "Output log: $output_file"

    # Start heartbeat monitor
    start_heartbeat "$story_id" "$output_file"

    # Run claude with unbuffered output (stdbuf if available)
    local tee_cmd="tee"
    if command -v stdbuf &> /dev/null; then
        tee_cmd="stdbuf -oL tee"
    fi

    if claude --print "$prompt" 2>&1 | $tee_cmd "$output_file"; then
        stop_heartbeat
        # Check for completion signal
        if grep -q "<promise>STORY_COMPLETE</promise>" "$output_file"; then
            log_success "Story $story_id completed!"
            mark_complete "$story_id"
            append_progress "$story_id" "COMPLETED" "Story implemented successfully"

            # Commit changes if any
            if [ -n "$(git status --porcelain)" ]; then
                git add -A
                git commit -m "feat(checkout): implement $story_id - $story_title

Implemented by RALPH autonomous loop.

Co-Authored-By: Claude <noreply@anthropic.com>" || true
            fi
        elif grep -q "<promise>BLOCKED:" "$output_file"; then
            local reason=$(grep "<promise>BLOCKED:" "$output_file" | sed 's/.*<promise>BLOCKED: \(.*\)<\/promise>.*/\1/')
            log_warn "Story $story_id blocked: $reason"
            append_progress "$story_id" "BLOCKED" "$reason"
        else
            log_warn "Story $story_id - no completion signal detected"
            append_progress "$story_id" "IN_PROGRESS" "Iteration completed without explicit completion signal"
        fi
    else
        stop_heartbeat
        log_error "Claude Code session failed"
        append_progress "$story_id" "ERROR" "Claude Code session failed"
    fi

    # Show completion stats
    local end_time=$(date '+%H:%M:%S')
    log_info "Iteration $iteration finished at $end_time"
    log_info "Full output saved to: $output_file"

    # Cleanup temp files only (keep output log)
    rm -f "$prompt_file"

    return 0
}

# Main loop
main() {
    echo ""
    log_info "╔══════════════════════════════════════════════════════════════╗"
    log_info "║              RALPH Loop - Starting                          ║"
    log_info "╚══════════════════════════════════════════════════════════════╝"
    log_info "PRD: $PRD_FILE"
    log_info "Max iterations: $MAX_ITERATIONS"
    log_info "Verbose mode: $VERBOSE"
    if [ -n "$EPIC_FILTER" ]; then
        log_info "Epic filter: $EPIC_FILTER"
    else
        log_info "Epic filter: (none - all epics)"
    fi
    log_info "Log directory: $LOG_DIR"
    log_info "Heartbeat interval: ${HEARTBEAT_INTERVAL}s"

    check_prerequisites

    # Initialize progress file if needed
    if [ ! -f "$PROGRESS_FILE" ]; then
        echo "# RALPH Progress Log" > "$PROGRESS_FILE"
        echo "# Started: $(date)" >> "$PROGRESS_FILE"
        echo "" >> "$PROGRESS_FILE"
    fi

    # Show epic status overview
    show_epic_status

    # Show filtered progress
    local completed=$(count_completed_filtered)
    local total=$(count_total_filtered)
    local ready=$(count_ready)
    local blocked=$(count_blocked)
    echo ""
    if [ -n "$EPIC_FILTER" ]; then
        log_info "Epic '$EPIC_FILTER' Progress: $completed / $total stories complete"
    else
        log_info "Overall Progress: $completed / $total stories complete"
    fi
    log_info "Ready: $ready | Blocked: $blocked"

    # Show next story
    local next_story=$(get_next_story)
    if [ -n "$next_story" ]; then
        local next_title=$(get_story_title "$next_story")
        local next_epic=$(get_story_epic "$next_story")
        log_info "Next story: $next_story - $next_title (epic: $next_epic)"
    else
        if [ -n "$EPIC_FILTER" ]; then
            log_warn "No ready stories in epic '$EPIC_FILTER'"
        else
            log_warn "No ready stories found"
        fi
    fi

    echo ""
    log_info "💡 Tip: Monitor logs in another terminal with:"
    log_info "   tail -f $LOG_DIR/*.log"
    echo ""

    # Check if already complete (filtered)
    if all_complete_filtered; then
        if [ -n "$EPIC_FILTER" ]; then
            log_success "All stories in epic '$EPIC_FILTER' already complete!"
        else
            log_success "All stories already complete!"
        fi
        echo "<promise>COMPLETE</promise>"
        exit 0
    fi

    # Run iterations
    for ((i=1; i<=MAX_ITERATIONS; i++)); do
        if ! run_iteration "$i"; then
            break
        fi

        # Check if all complete (filtered by epic if set)
        if all_complete_filtered; then
            if [ -n "$EPIC_FILTER" ]; then
                log_success "All stories in epic '$EPIC_FILTER' complete after $i iterations!"
            else
                log_success "All stories complete after $i iterations!"
            fi
            echo "<promise>COMPLETE</promise>"
            exit 0
        fi

        # Brief pause between iterations
        sleep 2
    done

    completed=$(count_completed_filtered)
    total=$(count_total_filtered)
    log_info "Loop finished. Progress: $completed / $total stories complete"

    if all_complete_filtered; then
        echo "<promise>COMPLETE</promise>"
    else
        log_warn "Max iterations reached. Run again to continue."
    fi
}

# Run main
main
