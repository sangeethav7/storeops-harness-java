FEATURE: Shift Handover Bulk Status Update
PROMPT: Add shift handover bulk update to activities — PATCH /api/activities/bulk-status allowing outgoing shift staff to mark multiple operational activities as DONE or BLOCKED in a single request, with partial failure handling and an audit entry per updated task.
STATUS: AWAITING APPROVAL

## Summary

Adds a `PATCH /api/activities/bulk-status` endpoint to the activities module that allows outgoing shift staff to atomically mark a list of operational tasks as `DONE` or `BLOCKED` in a single HTTP call. The endpoint returns a per-item success/failure breakdown (HTTP 207 Multi-Status) so callers can handle partial failures gracefully. Each successfully updated task triggers a `ShiftHandoverEvent` consumed by the alerts module to create one audit `Notification` per task, decoupled via Spring's `ApplicationEventPublisher`.

## Modules Affected

- **activities**: New `PATCH /api/activities/bulk-status` endpoint; `BulkStatusUpdateRequest` and `BulkStatusUpdateResponse` DTOs; bulk update logic with per-item error collection in `ActivityService`; `ActivityController` wired to return HTTP 207
- **shared/events**: New `ShiftHandoverEvent` class carrying task ID and new status for cross-module audit trail
- **alerts**: New `ShiftHandoverEventListener` consuming `ShiftHandoverEvent` and persisting one `Notification` of type `SHIFT_HANDOVER` per successfully updated task

## Architecture Notes

- Cross-module audit trail follows Rule 2: `ActivityService` publishes `ShiftHandoverEvent` per successful update via `ApplicationEventPublisher`; `AlertService`/listener consumes it independently — no direct import of `AlertService` from the activities module
- Only `DONE` and `BLOCKED` are valid target statuses for this endpoint; any other value is treated as a per-item failure with `errorCode: INVALID_STATUS`
- Partial failure handling: response body contains `succeeded` (list of updated task IDs) and `failed` (list of `{taskId, errorCode, message}`); HTTP status is always `207 Multi-Status` for bulk calls
- An empty `taskIds` list in the request is rejected with HTTP 400 / `AppException("EMPTY_REQUEST", ...)` before processing begins
- All error paths use `AppException` (Rule 3); no raw `RuntimeException` throws
- Controller delegates to service; service contains all business logic; no `ResponseEntity` in service (Rule 4)
- No cross-module repository imports (Rule 1)

## Sprint Plan

| Sprint | Deliverable |
|--------|-------------|
| Sprint 1 | `PATCH /api/activities/bulk-status` endpoint with partial failure response — `BulkStatusUpdateRequest`, `BulkStatusUpdateResponse` DTOs, `ActivityController` handler, `ActivityService.bulkUpdateStatus()` with per-item error collection, `ApplicationEventPublisher.publishEvent()` call per success, and `ActivityControllerTest` covering all acceptance criteria |
| Sprint 2 | `ShiftHandoverEvent` in `com.storeops.shared.events`, `ShiftHandoverEventListener` in alerts module creating one `SHIFT_HANDOVER` Notification per event, and `ShiftHandoverEventListenerTest` verifying audit entry creation |
