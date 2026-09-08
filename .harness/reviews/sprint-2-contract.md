SPRINT: 2
FEATURE: Shift Handover Bulk Status Update
STATUS: READY

## Deliverable

A `ShiftHandoverEventListener` in the alerts module that consumes `ShiftHandoverEvent` (already in `com.storeops.common.events` from Sprint 1) and calls `AlertService.createAlert()` once per event to create an audit notification. Includes `ShiftHandoverEventListenerTest` verifying notification creation and message content.

## Files to Create or Modify

- `src/main/java/com/storeops/alerts/ShiftHandoverEventListener.java` — create (@Component with @EventListener on ShiftHandoverEvent; calls AlertService.createAlert with message containing taskId and newStatus)
- `src/test/java/com/storeops/alerts/ShiftHandoverEventListenerTest.java` — create (Mockito unit tests verifying AlertService.createAlert is called with correct arguments)

## Acceptance Criteria

AC-1: Listener creates one audit notification per ShiftHandoverEvent
GIVEN AlertService is injected and a ShiftHandoverEvent carries taskId "t1" and newStatus "DONE"
WHEN ShiftHandoverEventListener.onShiftHandover(event) is called
THEN AlertService.createAlert() is invoked exactly once with a message containing "t1" and "DONE" and severity "LOW"

AC-2: Notification message contains both task ID and new status
GIVEN a ShiftHandoverEvent with taskId "act-99" and newStatus "BLOCKED"
WHEN ShiftHandoverEventListener.onShiftHandover(event) is called
THEN the message argument passed to AlertService.createAlert() contains the substring "act-99" and the substring "BLOCKED"

AC-3: No cross-module repository import — listener calls AlertService only, not AlertRepository
GIVEN ShiftHandoverEventListener source code
WHEN imports are inspected
THEN no import of com.storeops.alerts.AlertRepository appears in the listener class

## Architecture Constraints

- [ ] No cross-module repository imports — ShiftHandoverEventListener must not import AlertRepository; it calls AlertService only
- [ ] AppException used for all errors — no raw RuntimeException in listener
- [ ] ApplicationEventPublisher not needed in listener — @EventListener annotation handles subscription
- [ ] Controller → Service → Repository layer order maintained — listener is a @Component, not a @Service or @Controller; it delegates to AlertService
