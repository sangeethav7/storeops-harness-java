# Demonstration Run Prompt

## Feature Prompt Used

```
@planner Add shift handover bulk update to activities.
PATCH /api/activities/bulk-status — allows outgoing shift staff to mark multiple
activities as DONE or BLOCKED in a single request, with partial failure handling
and an audit entry per updated task.
```

## Feature Details

- **Endpoint:** `PATCH /api/activities/bulk-status`
- **Module:** activities
- **Purpose:** Outgoing shift staff can close or block multiple operational tasks in one call
- **Partial failure:** If some task IDs are not found, the request continues for valid IDs and reports failures
- **Audit:** Each successful status change creates an audit log entry
