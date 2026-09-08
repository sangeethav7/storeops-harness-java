# Coding Conventions — StoreOps Java

## Purpose

Define the Java/Spring Boot coding standards for StoreOps. Generated code must follow these conventions to pass Checkstyle and maintain consistency across all five modules.

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Classes | PascalCase | `ActivityController`, `TaskService` |
| Methods | camelCase | `createActivity()`, `findById()` |
| Variables | camelCase | `activityRepository`, `taskId` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `DEFAULT_STATUS` |
| Packages | lowercase | `com.storeops.activities.service` |
| Enums | PascalCase, values UPPER_SNAKE_CASE | `TaskStatus.IN_PROGRESS` |

## Controller Conventions

```java
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    // Constructor injection — never @Autowired on field
    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getAllActivities() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(
            @Valid @RequestBody CreateActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.createActivity(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable String id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> updateActivity(
            @PathVariable String id,
            @Valid @RequestBody UpdateActivityRequest request) {
        return ResponseEntity.ok(activityService.updateActivity(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable String id) {
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }
}
```

## Service Conventions

```java
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ActivityService(ActivityRepository activityRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.activityRepository = activityRepository;
        this.eventPublisher = eventPublisher;
    }

    public ActivityResponse getActivity(String id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "TASK_NOT_FOUND",
                        "Activity with id " + id + " not found",
                        HttpStatus.NOT_FOUND));
        return ActivityMapper.toResponse(activity);
    }
}
```

## Repository Conventions

```java
@Repository
public class ActivityRepository {

    private final ConcurrentHashMap<String, Activity> store = new ConcurrentHashMap<>();

    public Activity save(Activity activity) {
        if (activity.getId() == null) {
            activity.setId(UUID.randomUUID().toString());
        }
        store.put(activity.getId(), activity);
        return activity;
    }

    public Optional<Activity> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Activity> findAll() {
        return new ArrayList<>(store.values());
    }

    public void deleteById(String id) {
        store.remove(id);
    }
}
```

## DTO Conventions

```java
// Request DTO
public class CreateActivityRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;
    // getters and setters
}

// Response DTO — never expose entity directly
public class ActivityResponse {
    private String id;
    private String title;
    private TaskStatus status;
    private TaskPriority priority;
    // getters and setters
}
```

## Exception Handling

```java
// GlobalExceptionHandler in com.storeops.shared.exception
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        ErrorResponse error = new ErrorResponse(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }
}
```

## Checkstyle Rules

The project must pass `mvn checkstyle:check` with zero violations. Key rules enforced:
- No wildcard imports (`import com.storeops.activities.*`)
- Maximum line length: 120 characters
- All public methods must have Javadoc (or suppress with annotation)
- No unused imports
- Braces required for all control structures
