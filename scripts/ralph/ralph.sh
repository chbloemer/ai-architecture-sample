#!/bin/bash
#
# RALPH Loop - Autonomous AI Agent for PRD Implementation
# Runs Claude Code repeatedly until all PRD stories pass
#
# Usage: ./ralph.sh [max_iterations] [--verbose|-v]
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
for arg in "$@"; do
    case $arg in
        -v|--verbose)
            VERBOSE=true
            ;;
        [0-9]*)
            MAX_ITERATIONS=$arg
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

# Get next incomplete story
get_next_story() {
    jq -r '.stories[] | select(.passes == false) | .id' "$PRD_FILE" | head -1
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

    echo ""
    log_info "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    log_info "Iteration $iteration: $story_id"
    log_info "Title: $story_title"
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
    log_info "Log directory: $LOG_DIR"
    log_info "Heartbeat interval: ${HEARTBEAT_INTERVAL}s"
    echo ""

    check_prerequisites

    # Initialize progress file if needed
    if [ ! -f "$PROGRESS_FILE" ]; then
        echo "# RALPH Progress Log" > "$PROGRESS_FILE"
        echo "# Started: $(date)" >> "$PROGRESS_FILE"
        echo "" >> "$PROGRESS_FILE"
    fi

    local completed=$(count_completed)
    local total=$(count_total)
    log_info "Progress: $completed / $total stories complete"

    # Show next story
    local next_story=$(get_next_story)
    if [ -n "$next_story" ]; then
        local next_title=$(get_story_title "$next_story")
        log_info "Next story: $next_story - $next_title"
    fi

    echo ""
    log_info "💡 Tip: Monitor logs in another terminal with:"
    log_info "   tail -f $LOG_DIR/*.log"
    echo ""

    # Check if already complete
    if all_complete; then
        log_success "All stories already complete!"
        echo "<promise>COMPLETE</promise>"
        exit 0
    fi

    # Run iterations
    for ((i=1; i<=MAX_ITERATIONS; i++)); do
        if ! run_iteration "$i"; then
            break
        fi

        # Check if all complete
        if all_complete; then
            log_success "All stories complete after $i iterations!"
            echo "<promise>COMPLETE</promise>"
            exit 0
        fi

        # Brief pause between iterations
        sleep 2
    done

    completed=$(count_completed)
    log_info "Loop finished. Progress: $completed / $total stories complete"

    if all_complete; then
        echo "<promise>COMPLETE</promise>"
    else
        log_warn "Max iterations reached. Run again to continue."
    fi
}

# Run main
main
