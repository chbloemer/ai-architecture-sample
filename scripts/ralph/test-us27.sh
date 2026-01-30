#!/bin/bash
# Test script to debug US-27 prompt issue

cd "$(dirname "$0")/../.."

echo "=== Test 1: Simple prompt ==="
claude --print "Say hello" 2>&1 | head -3
echo ""

echo "=== Test 2: US-27 title only ==="
claude --print "Implement US-27: Remove Deprecated CartCheckedOut Availability Logic" 2>&1 | head -3
echo ""

echo "=== Test 3: Full prompt from file ==="
cat > /tmp/us27-prompt.txt << 'PROMPT_EOF'
# RALPH Loop - Iteration Task

## Your Task
Implement the following user story:

**US-27: Remove Deprecated CartCheckedOut Availability Logic**

The CartCheckedOut event was previously used to reduce product availability. Since US-26 now handles this via CheckoutConfirmed event, the old CartCheckedOut event consumer should be removed.

**Acceptance Criteria:**
- CartCheckedOutEventListener in product context is removed (if exists)
- Build and architecture tests pass

## Instructions
1. Search for CartCheckedOutEventListener in the product context
2. Remove it if it exists
3. Run: ./gradlew build
4. If complete, output: <promise>STORY_COMPLETE</promise>
PROMPT_EOF

echo "Prompt file created. Running claude with file input..."
claude --print "$(cat /tmp/us27-prompt.txt)" 2>&1 | head -10
echo ""

echo "=== Test 4: Using stdin ==="
cat /tmp/us27-prompt.txt | claude --print - 2>&1 | head -10

echo ""
echo "Tests complete"
