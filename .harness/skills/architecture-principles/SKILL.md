# Architecture Principles — StoreOps

## Purpose

Define the four non-negotiable architecture rules for StoreOps. Every rule maps to a specific failure mode observed in prior AI-generated code. Violating any rule is a HARD GATE FAILURE.

---

## Rule 1: Module Boundary — No Cross-Module Repository Imports

**What it prohibits:** A service in module X must never import a repository from module Y.

**Why:** Direct repository imports bypass service layer contracts and expose internal data structures across module boundaries, creating tight coupling that breaks when either module changes.

**Correct pattern:**
```java
// In ActivitiesService — read-only lookup of staff is OK via StaffService
@Service
public class ActivitiesService {
    private final ActivityRepository activityRepository;
    private final StaffService staffService; // OK — service layer only

    // NEVER: private final StaffRepository staffRepository; ← HARD GATE FAILURE
}
```

**Hard gate check:** Search imports in all service classes:
```
grep -r "import com.storeops" src/main/java --include="*.java" | grep "repository" | grep -v "own module"
```
Zero results required.

---

## Rule 2: Event Bus — Cross-Module Side Effects via ApplicationEventPublisher Only

**What it prohibits:** A service must never directly import or call a service from another module to trigger a side effect.

**Why:** Direct service-to-service calls for side effects create hidden dependencies between modules. If NotificationService changes its method signature, every caller breaks. Event bus decouples the producer from the consumer.

**Correct pattern:**
```java
// Firing an SLA breach alert from ActivitiesService
@Service
public class ActivitiesService {
    private final ApplicationEventPublisher eventPublisher;

    public void checkSlaBreaches() {
        // CORRECT: publish event — AlertsModule listens independently
        eventPublisher.publishEvent(new SlaBreachEvent(this, taskId, assignedStaffId));

        // NEVER: alertsService.sendSlaBreachAlert(taskId) ← HARD GATE FAILURE
        // NEVER: import com.storeops.alerts.service.AlertsService ← HARD GATE FAILURE
    }
}
```

**Hard gate check:** Any direct import of another module's service for write/trigger operations is a failure.

---

## Rule 3: Error Contract — AppException Only, No Raw Throws

**What it prohibits:** No `throw new RuntimeException(...)` or `throw new Exception(...)` in any service or controller class.

**Why:** Raw exceptions produce inconsistent HTTP responses and bypass the GlobalExceptionHandler mapping. AppException ensures every error has a typed errorCode, human-readable message, and correct HTTP status code.

**Correct pattern:**
```java
// CORRECT
throw new AppException("TASK_NOT_FOUND", "Task with id " + id + " not found", HttpStatus.NOT_FOUND);
throw new AppException("INVALID_STATUS", "Cannot transition from DONE to TODO", HttpStatus.BAD_REQUEST);

// HARD GATE FAILURE
throw new RuntimeException("Task not found");
throw new Exception("Something went wrong");
```

**AppException structure:**
```java
public class AppException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public AppException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
```

**Hard gate check:**
```
grep -rn "throw new RuntimeException\|throw new Exception" src/main/java --include="*.java"
```
Zero results required in service and controller packages.

---

## Rule 4: Layer Separation — Controller → Service → Repository, No Skipping

**What it prohibits:**
- Controllers must not contain business logic
- Services must not contain HTTP-specific code (ResponseEntity, HttpServletRequest, HttpStatus)
- Repositories must not call external services or contain business logic

**Why:** Layer separation ensures each layer is independently testable and replaceable. Business logic in controllers cannot be unit-tested without an HTTP context. HTTP logic in services breaks reusability.

**Correct pattern:**
```java
// Controller — HTTP only, delegates to service
@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable String id) {
        ActivityResponse response = activityService.getActivity(id); // delegate
        return ResponseEntity.ok(response); // HTTP wrapping here only
    }
}

// Service — business logic only, no HTTP classes
@Service
public class ActivityService {
    public ActivityResponse getActivity(String id) {
        // NEVER: return ResponseEntity.ok(...) ← HARD GATE FAILURE
        // NEVER: import org.springframework.http.ResponseEntity ← in service
        Activity activity = activityRepository.findById(id)
            .orElseThrow(() -> new AppException("TASK_NOT_FOUND", "Not found", HttpStatus.NOT_FOUND));
        return ActivityMapper.toResponse(activity);
    }
}
```

**Hard gate check:** No `ResponseEntity` or `HttpServletRequest` imports in service or repository classes.

---

## Rule 5: Read-Only Reports Module

**What it prohibits:** The reports module must never write to activities, programmes, or staff repositories.

**Why:** Reports aggregate existing data — they are a read-only view. Write operations from reports would create unexpected mutations in other modules' data.

**Check:** No write methods (save, update, delete) called on non-reports repositories from within `com.storeops.reports`.
