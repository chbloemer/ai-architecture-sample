# Add User Story to PRD

Add a new user story to the PRD document in the `tasks/` folder.

## Arguments

$ARGUMENTS - The user story details. Can be:
- Just a title/description for interactive mode
- Full details in format: "US-XX: Title | Description | AC1, AC2, AC3"

## Instructions

1. **Read the current PRD files:**
   - `tasks/prd.json` - structured data
   - `tasks/prd-checkout.md` - human-readable format

2. **Determine the next user story ID:**
   - Find the highest existing US-XX number
   - New story ID = highest + 1

3. **If only a title/description was provided, ask for:**
   - Full title (short, descriptive)
   - Description (what and why)
   - Acceptance criteria (testable conditions)

4. **Add the new user story to both files:**

   **In prd.json:**
   ```json
   {
     "id": "US-XX",
     "title": "Story Title",
     "description": "Story description",
     "acceptance_criteria": [
       "Criterion 1",
       "Criterion 2"
     ],
     "passes": false
   }
   ```

   **In prd-checkout.md:**
   ```markdown
   ### US-XX: Story Title
   **As a** developer
   **I want** [what]
   **So that** [why]

   **Acceptance Criteria:**
   - Criterion 1
   - Criterion 2

   ---
   ```

5. **Output confirmation:**
   - Show the new user story ID and title
   - Confirm both files were updated

## Example Usage

```
/prd Add payment validation
/prd US-25: Add Payment Validation | Validate payment tokens before processing | Token format validation, Expiry check, Provider verification
```
