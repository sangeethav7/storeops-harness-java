# API Integration — StoreOps

## Purpose

Define the REST API patterns, endpoint structure, and cross-module integration rules for StoreOps. The Generator must follow these patterns exactly when adding new endpoints.

## Standard CRUD Endpoint Pattern

Every module follows this endpoint structure:

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| GET | /api/{module} | List all | 200 + List |
| POST | /api/{module} | Create | 201 + created entity |
| GET | /api/{module}/{id} | Get by ID | 200 + entity / 404 |
| PUT | /api/{module}/{id} | Full update | 200 + updated entity / 404 |
| DELETE | /api/{module}/{id} | Delete | 204 No Content / 404 |

## Activities Module Endpoints

```
GET    /api/activities              → List all activities
POST   /api/activities              → Create activity
GET    /api/activities/{id}         → Get activity by ID
PUT    /api/activities/{id}         → Update activity
DELETE /api/activities/{id}         → Delete activity
PATCH  /api/activities/bulk-status  → Bulk status update (shift handover)
```

## Request/Response Patterns

### Create Activity
```json
POST /api/activities
{
  "title": "Restock dairy section",
  "description": "Restock all dairy products before 8am",
  "priority": "HIGH",
  "category": "RESTOCKING",
  "assignedStaffId": "staff-123"
}

Response 201:
{
  "id": "act-uuid",
  "title": "Restock dairy section",
  "status": "TODO",
  "priority": "HIGH",
  "category": "RESTOCKING",
  "assignedStaffId": "staff-123",
  "createdAt": "2026-09-08T10:00:00Z"
}
```

### Error Response Format
```json
Response 404:
{
  "errorCode": "TASK_NOT_FOUND",
  "message": "Activity with id act-999 not found"
}

Response 400:
{
  "errorCode": "INVALID_STATUS_TRANSITION",
  "message": "Cannot transition from DONE to TODO"
}
```

## Cross-Module Integration Pattern

### Publishing Events (correct pattern)

```java
// In ActivitiesService — when a CRITICAL task becomes overdue
@Service
public class ActivityService {
    private final ApplicationEventPublisher eventPublisher;

    public void markOverdueTasks() {
        List<Activity> overdue = activityRepository.findOverdueCritical();
        for (Activity task : overdue) {
            // Publish event — AlertsModule listens and handles notification
            eventPublisher.publishEvent(new SlaBreachEvent(this, task.getId(), task.getAssignedStaffId()));
        }
    }
}

// In AlertsModule — event listener
@Component
public class SlaBreachListener {
    @EventListener
    public void handleSlaBreach(SlaBreachEvent event) {
        // AlertsModule handles creating the notification
        alertsService.createSlaBreachAlert(event.getTaskId(), event.getStaffId());
    }
}
```

### Event Classes in `com.storeops.shared.events`

```java
public class SlaBreachEvent extends ApplicationEvent {
    private final String taskId;
    private final String staffId;

    public SlaBreachEvent(Object source, String taskId, String staffId) {
        super(source);
        this.taskId = taskId;
        this.staffId = staffId;
    }
}
```

## Bulk Operations Pattern

For bulk endpoints like PATCH /api/activities/bulk-status:

```json
Request:
{
  "updates": [
    { "id": "act-1", "status": "DONE" },
    { "id": "act-2", "status": "BLOCKED" }
  ]
}

Response 200 (partial failure supported):
{
  "succeeded": ["act-1"],
  "failed": [
    { "id": "act-2", "reason": "Activity not found" }
  ]
}
```

## Validation Rules

- All POST/PUT request bodies annotated with `@Valid`
- Required fields annotated with `@NotBlank` or `@NotNull`
- Enum fields validated via custom validator or `@NotNull` + enum type
- ID path variables are Strings (UUID format) — no numeric IDs

## HTTP Status Code Reference

| Scenario | Status |
|----------|--------|
| Successful GET/PUT | 200 |
| Successful POST (create) | 201 |
| Successful DELETE | 204 |
| Not found | 404 (AppException TASK_NOT_FOUND) |
| Bad request / validation | 400 (AppException INVALID_REQUEST) |
| Business rule violation | 422 (AppException BUSINESS_RULE_VIOLATION) |
| Internal error | 500 (AppException INTERNAL_ERROR) |
