#!/bin/bash
#
# RALPH Interactive - Single iteration runner for Claude Code
# Use this for manual/interactive RALPH sessions
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PRD_FILE="$PROJECT_ROOT/tasks/prd.json"
PROGRESS_FILE="$PROJECT_ROOT/tasks/progress.txt"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

cd "$PROJECT_ROOT"

# Check for jq
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required. Install with: brew install jq"
    exit 1
fi

# Get next story
STORY_ID=$(jq -r '.stories[] | select(.passes == false) | .id' "$PRD_FILE" | head -1)

if [ -z "$STORY_ID" ]; then
    echo -e "${GREEN}All stories complete!${NC}"
    echo "<promise>COMPLETE</promise>"
    exit 0
fi

STORY_TITLE=$(jq -r --arg id "$STORY_ID" '.stories[] | select(.id == $id) | .title' "$PRD_FILE")
STORY_DESC=$(jq -r --arg id "$STORY_ID" '.stories[] | select(.id == $id) | .description' "$PRD_FILE")
STORY_CRITERIA=$(jq -r --arg id "$STORY_ID" '.stories[] | select(.id == $id) | .acceptance_criteria | map("- " + .) | join("\n")' "$PRD_FILE")
COMPLETED=$(jq '[.stories[] | select(.passes == true)] | length' "$PRD_FILE")
TOTAL=$(jq '.stories | length' "$PRD_FILE")

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}RALPH Interactive - Next Story${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""
echo -e "${YELLOW}Progress:${NC} $COMPLETED / $TOTAL stories complete"
echo ""
echo -e "${GREEN}Next Story: $STORY_ID - $STORY_TITLE${NC}"
echo ""
echo "Description:"
echo "$STORY_DESC"
echo ""
echo "Acceptance Criteria:"
echo "$STORY_CRITERIA"
echo ""
echo -e "${BLUE}================================================${NC}"
echo ""
echo "Copy this prompt to Claude Code:"
echo ""
cat << EOF
Implement user story $STORY_ID: $STORY_TITLE

$STORY_DESC

Acceptance Criteria:
$STORY_CRITERIA

Instructions:
1. Read existing code patterns in cart/ and productcatalog/ contexts
2. Implement ONLY this story
3. Follow DDD patterns and project conventions
4. Run \`./gradlew build\` to verify
5. When complete, tell me so I can mark it done

Reference: tasks/prd-checkout.md for full PRD
Progress: tasks/progress.txt for learnings
EOF

echo ""
echo -e "${BLUE}================================================${NC}"
echo ""
echo "After completing the story, run:"
echo -e "  ${GREEN}./scripts/ralph/mark-complete.sh $STORY_ID${NC}"
