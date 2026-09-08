# How to Test — StoreOps Java

## Purpose

Define the testing standards for StoreOps. Tests must assert business rules, not just HTTP status codes. Every new class must meet the 80% line coverage threshold.

## Test Stack

- **JUnit 5** — test framework
- **MockMvc** — controller layer tests (via `@WebMvcTest`)
- **Mockito** — mocking service/repository dependencies
- **spring-boot-starter-test** — includes all of the above

## Controller Tests (`@WebMvcTest`)

```java
@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityService activityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createActivity_withValidRequest_returns201AndActivityId() throws Exception {
        // Arrange
        CreateActivityRequest request = new CreateActivityRequest();
        request.setTitle("Restock dairy section");
        request.setPriority(TaskPriority.HIGH);

        ActivityResponse response = new ActivityResponse();
        response.setId("act-123");
        response.setTitle("Restock dairy section");
        response.setStatus(TaskStatus.TODO);

        when(activityService.createActivity(any(CreateActivityRequest.class)))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                // Assert business rule: new activity must have TODO status
                .andExpect(jsonPath("$.status").value("TODO"))
                // Assert response body content — not just status code
                .andExpect(jsonPath("$.id").value("act-123"))
                .andExpect(jsonPath("$.title").value("Restock dairy section"));
    }

    @Test
    void getActivity_withNonExistentId_returns404WithErrorCode() throws Exception {
        // Arrange
        when(activityService.getActivity("nonexistent"))
                .thenThrow(new AppException("TASK_NOT_FOUND", "Activity not found", HttpStatus.NOT_FOUND));

        // Act + Assert
        mockMvc.perform(get("/api/activities/nonexistent"))
                .andExpect(status().isNotFound())
                // Assert error contract: must return typed errorCode
                .andExpect(jsonPath("$.errorCode").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }
}
```

## Service Tests (`@ExtendWith(MockitoExtension.class)`)

```java
@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void createActivity_withValidRequest_savesToRepositoryAndReturnsDto() {
        // Arrange
        CreateActivityRequest request = new CreateActivityRequest();
        request.setTitle("Restock dairy section");
        request.setPriority(TaskPriority.HIGH);

        Activity savedActivity = new Activity();
        savedActivity.setId("act-123");
        savedActivity.setTitle("Restock dairy section");
        savedActivity.setStatus(TaskStatus.TODO); // business rule: always starts TODO

        when(activityRepository.save(any(Activity.class))).thenReturn(savedActivity);

        // Act
        ActivityResponse result = activityService.createActivity(request);

        // Assert business rule: status must be TODO on creation
        assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(result.getId()).isEqualTo("act-123");

        // Assert repository interaction
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void getActivity_withNonExistentId_throwsAppExceptionWithCorrectCode() {
        // Arrange
        when(activityRepository.findById("bad-id")).thenReturn(Optional.empty());

        // Act + Assert — verify AppException thrown, not RuntimeException
        AppException exception = assertThrows(AppException.class,
                () -> activityService.getActivity("bad-id"));

        assertThat(exception.getErrorCode()).isEqualTo("TASK_NOT_FOUND");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateStatus_toCriticalOverdue_publishesSlaBreachEvent() {
        // Arrange — verifying cross-module event is published
        Activity activity = new Activity();
        activity.setId("act-123");
        activity.setStatus(TaskStatus.IN_PROGRESS);
        activity.setPriority(TaskPriority.CRITICAL);

        when(activityRepository.findById("act-123")).thenReturn(Optional.of(activity));
        when(activityRepository.save(any())).thenReturn(activity);

        // Act
        activityService.markAsOverdue("act-123");

        // Assert event published — not direct service call
        verify(eventPublisher, times(1)).publishEvent(any(SlaBreachEvent.class));
    }
}
```

## Test Naming Convention

```
methodName_scenario_expectedResult

Examples:
createActivity_withValidRequest_returns201AndActivityId
getActivity_withNonExistentId_returns404WithErrorCode
updateStatus_fromDoneToTodo_throwsBusinessRuleViolation
bulkUpdate_withPartialFailure_returnsSucceededAndFailedLists
```

## Coverage Requirements

- **Minimum 80% line coverage** on all new classes introduced by the Generator
- Run coverage report: `mvn test jacoco:report`
- Coverage report location: `target/site/jacoco/index.html`

## What Tests MUST Assert (not optional)

| Scenario | Must Assert |
|----------|-------------|
| Create entity | Response body contains ID, status is correct initial value |
| Get non-existent | errorCode field in response, correct HTTP status |
| Business rule violation | AppException errorCode matches the rule violated |
| Cross-module trigger | `verify(eventPublisher).publishEvent(any(ExpectedEvent.class))` |
| Bulk operation | Both succeeded and failed lists in response |

## What Tests Must NOT Do

- Assert only `status().isOk()` without checking response body
- Use `@SpringBootTest` for unit tests (use `@WebMvcTest` or `@ExtendWith` instead)
- Mock the repository in controller tests (mock the service)
- Leave test methods empty or with only a `assertTrue(true)`
