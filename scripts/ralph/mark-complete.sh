#!/bin/bash
#
# Mark a RALPH story as complete
# Usage: ./mark-complete.sh <story-id> [notes]
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PRD_FILE="$PROJECT_ROOT/tasks/prd.json"
PROGRESS_FILE="$PROJECT_ROOT/tasks/progress.txt"

STORY_ID="$1"
NOTES="$2"

if [ -z "$STORY_ID" ]; then
    echo "Usage: $0 <story-id> [notes]"
    echo "Example: $0 US-1 'Implemented all value objects'"
    exit 1
fi

# Check for jq
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required. Install with: brew install jq"
    exit 1
fi

# Verify story exists
STORY_EXISTS=$(jq -r --arg id "$STORY_ID" '.stories[] | select(.id == $id) | .id' "$PRD_FILE")
if [ -z "$STORY_EXISTS" ]; then
    echo "Error: Story $STORY_ID not found in PRD"
    exit 1
fi

# Get story info
STORY_TITLE=$(jq -r --arg id "$STORY_ID" '.stories[] | select(.id == $id) | .title' "$PRD_FILE")
ALREADY_DONE=$(jq -r --arg id "$STORY_ID" '.stories[] | select(.id == $id) | .passes' "$PRD_FILE")

if [ "$ALREADY_DONE" = "true" ]; then
    echo "Story $STORY_ID is already marked as complete"
    exit 0
fi

# Mark as complete in PRD
TMP_FILE=$(mktemp)
jq --arg id "$STORY_ID" '(.stories[] | select(.id == $id) | .passes) = true' "$PRD_FILE" > "$TMP_FILE"
mv "$TMP_FILE" "$PRD_FILE"

# Append to progress file
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
{
    echo ""
    echo "## [$TIMESTAMP] $STORY_ID - COMPLETED"
    echo "**$STORY_TITLE**"
    if [ -n "$NOTES" ]; then
        echo ""
        echo "$NOTES"
    fi
} >> "$PROGRESS_FILE"

# Show status
COMPLETED=$(jq '[.stories[] | select(.passes == true)] | length' "$PRD_FILE")
TOTAL=$(jq '.stories | length' "$PRD_FILE")

echo "✓ Marked $STORY_ID as complete"
echo "  Progress: $COMPLETED / $TOTAL stories"

# Check if all done
if [ "$COMPLETED" -eq "$TOTAL" ]; then
    echo ""
    echo "🎉 All stories complete!"
    echo "<promise>COMPLETE</promise>"
fi
