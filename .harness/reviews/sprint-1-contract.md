SPRINT: 1
FEATURE: Shift Handover Bulk Status Update
STATUS: READY

## Deliverable

A working `PATCH /api/activities/bulk-status` endpoint with per-item partial failure handling, returning HTTP 207 Multi-Status with `succeeded` and `failed` lists. Includes `BulkStatusUpdateRequest` and `BulkStatusUpdateResponse` DTOs, `ActivityService.bulkUpdateStatus()` business logic, `ApplicationEventPublisher.publishEvent(ShiftHandoverEvent)` call per successfully updated task, and `ActivityControllerTest` covering all acceptance criteria below.

## Files to Create or Modify

- `src/main/java/com/storeops/activities/BulkStatusUpdateRequest.java` — create (record or class: `List<String> taskIds`, `String status`)
- `src/main/java/com/storeops/activities/BulkStatusUpdateResponse.java` — create (record or class: `List<String> succeeded`, `List<BulkFailureItem> failed`)
- `src/main/java/com/storeops/activities/BulkFailureItem.java` — create (record or class: `String taskId`, `String errorCode`, `String message`)
- `src/main/java/com/storeops/activities/ActivityController.java` — modify (add `PATCH /bulk-status` handler returning `ResponseEntity<BulkStatusUpdateResponse>` with HTTP 207)
- `src/main/java/com/storeops/activities/ActivityService.java` — modify (add `bulkUpdateStatus(BulkStatusUpdateRequest)` returning `BulkStatusUpdateResponse`; inject `ApplicationEventPublisher`; publish `ShiftHandoverEvent` per success)
- `src/main/java/com/storeops/shared/events/ShiftHandoverEvent.java` — create (extend `ApplicationEvent`: fields `String taskId`, `String newStatus`)
- `src/test/java/com/storeops/activities/ActivityControllerTest.java` — modify (add test methods for AC-1 through AC-5)

## Acceptance Criteria

AC-1: All tasks updated successfully returns HTTP 207 with full succeeded list
GIVEN the activity repository contains tasks with IDs `["t1","t2"]` both in status `IN_PROGRESS`
WHEN `PATCH /api/activities/bulk-status` is called with body `{"taskIds":["t1","t2"],"status":"DONE"}`
THEN the response status is `207`, `succeeded` contains `["t1","t2"]`, and `failed` is empty

AC-2: Non-existent task ID appears in failed list with TASK_NOT_FOUND
GIVEN the activity repository contains a task with ID `"t1"` but no task with ID `"ghost"`
WHEN `PATCH /api/activities/bulk-status` is called with body `{"taskIds":["t1","ghost"],"status":"DONE"}`
THEN the response status is `207`, `succeeded` contains `["t1"]`, and `failed` contains one entry with `taskId="ghost"` and `errorCode="TASK_NOT_FOUND"`

AC-3: Invalid target status appears in failed list with INVALID_STATUS
GIVEN the activity repository contains a task with ID `"t1"`
WHEN `PATCH /api/activities/bulk-status` is called with body `{"taskIds":["t1"],"status":"TODO"}`
THEN the response status is `207`, `succeeded` is empty, and `failed` contains one entry with `taskId="t1"` and `errorCode="INVALID_STATUS"`

AC-4: Empty taskIds list is rejected before processing with HTTP 400
GIVEN a request body containing an empty `taskIds` list `{"taskIds":[],"status":"DONE"}`
WHEN `PATCH /api/activities/bulk-status` is called
THEN the response status is `400` and the response body contains `errorCode="EMPTY_REQUEST"`

AC-5: ApplicationEventPublisher receives one ShiftHandoverEvent per successfully updated task
GIVEN the activity repository contains tasks `["t1","t2"]` and `ApplicationEventPublisher` is injected as a mock
WHEN `PATCH /api/activities/bulk-status` is called with body `{"taskIds":["t1","t2"],"status":"DONE"}`
THEN `ApplicationEventPublisher.publishEvent()` is invoked exactly twice, each invocation carrying a `ShiftHandoverEvent` with the respective task ID and `newStatus="DONE"`

AC-6: Mixed request — only valid DONE/BLOCKED tasks emit events, failed tasks do not
GIVEN the activity repository contains task `"t1"` and no task `"ghost"`, target status `BLOCKED`
WHEN `PATCH /api/activities/bulk-status` is called with body `{"taskIds":["t1","ghost"],"status":"BLOCKED"}`
THEN `ApplicationEventPublisher.publishEvent()` is invoked exactly once (for `"t1"`) and not invoked for `"ghost"`

## Architecture Constraints

- [ ] No cross-module repository imports — `ActivityService` must not import any repository outside `com.storeops.activities`
- [ ] `AppException` used for all errors — no `throw new RuntimeException(...)` or `throw new Exception(...)` anywhere in the changed files
- [ ] `ApplicationEventPublisher` used for `ShiftHandoverEvent` — no direct import of `AlertService` or any alerts-module class from `ActivityService`
- [ ] Controller → Service → Repository layer order maintained — `ActivityController` contains no business logic; `ActivityService` contains no `ResponseEntity` or `HttpStatus` imports
- [ ] HTTP 207 returned from controller for all bulk-status responses (except the HTTP 400 empty-request guard)
