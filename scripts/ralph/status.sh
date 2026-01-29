#!/bin/bash
#
# Show RALPH PRD status
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PRD_FILE="$PROJECT_ROOT/tasks/prd.json"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

if ! command -v jq &> /dev/null; then
    echo "Error: jq is required. Install with: brew install jq"
    exit 1
fi

if [ ! -f "$PRD_FILE" ]; then
    echo "Error: PRD file not found: $PRD_FILE"
    exit 1
fi

PRD_NAME=$(jq -r '.name' "$PRD_FILE")
COMPLETED=$(jq '[.stories[] | select(.passes == true)] | length' "$PRD_FILE")
TOTAL=$(jq '.stories | length' "$PRD_FILE")
REMAINING=$((TOTAL - COMPLETED))

echo ""
echo "═══════════════════════════════════════════════"
echo " RALPH Status: $PRD_NAME"
echo "═══════════════════════════════════════════════"
echo ""
echo -e " Progress: ${GREEN}$COMPLETED${NC} / $TOTAL complete ($REMAINING remaining)"
echo ""

# Progress bar
BAR_WIDTH=40
FILLED=$((COMPLETED * BAR_WIDTH / TOTAL))
EMPTY=$((BAR_WIDTH - FILLED))
printf " ["
printf "%${FILLED}s" | tr ' ' '█'
printf "%${EMPTY}s" | tr ' ' '░'
printf "] %d%%\n" $((COMPLETED * 100 / TOTAL))

echo ""
echo "───────────────────────────────────────────────"
echo " Stories"
echo "───────────────────────────────────────────────"

# List all stories
jq -r '.stories[] | "\(.passes)\t\(.id)\t\(.title)"' "$PRD_FILE" | while IFS=$'\t' read -r passes id title; do
    if [ "$passes" = "true" ]; then
        echo -e " ${GREEN}✓${NC} $id: $title"
    else
        echo -e " ${RED}○${NC} $id: $title"
    fi
done

echo ""

# Show next story
NEXT_ID=$(jq -r '.stories[] | select(.passes == false) | .id' "$PRD_FILE" | head -1)
if [ -n "$NEXT_ID" ]; then
    NEXT_TITLE=$(jq -r --arg id "$NEXT_ID" '.stories[] | select(.id == $id) | .title' "$PRD_FILE")
    echo -e " ${YELLOW}Next:${NC} $NEXT_ID - $NEXT_TITLE"
    echo ""
    echo " Run: ./scripts/ralph/ralph-interactive.sh"
else
    echo -e " ${GREEN}All stories complete!${NC}"
    echo " <promise>COMPLETE</promise>"
fi
echo ""
