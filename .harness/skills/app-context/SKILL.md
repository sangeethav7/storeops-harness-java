# App Context — StoreOps

## Purpose

Provide every agent with the domain context, module structure, and technical stack of the StoreOps project before it acts.

## Domain

StoreOps is a REST API for retail store operations management. It allows store teams to create operational programmes, assign and track activities across departments, coordinate staff, and surface performance reports by store and region.

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Build tool | Maven |
| Testing | JUnit 5 + MockMvc |
| Static analysis | Checkstyle |
| Storage | In-memory (ConcurrentHashMap) |

## Module Structure

Each module lives under `com.storeops.<module>` and follows a strict 3-layer model:

```
com.storeops.
├── activities/
│   ├── controller/   ← HTTP layer
│   ├── service/      ← Business logic
│   ├── repository/   ← Data access (ConcurrentHashMap)
│   ├── model/        ← Entity classes
│   └── dto/          ← Request/Response objects
├── programmes/
├── staff/
├── alerts/
├── reports/
└── shared/
    ├── exception/    ← AppException, GlobalExceptionHandler
    └── events/       ← Spring application events
```

## Module Responsibilities

| Module | Retail Responsibility | Key Entities |
|--------|----------------------|--------------|
| activities | Operational tasks — restocking, planogram resets, compliance checks | Task, TaskStatus (TODO, IN_PROGRESS, DONE, BLOCKED), TaskPriority (LOW, MEDIUM, HIGH, CRITICAL) |
| programmes | Store programmes — seasonal rollouts, compliance drives | Project, ProjectMember, ProjectRole (STORE_MANAGER, DEPARTMENT_LEAD, ASSOCIATE) |
| staff | Staff registration, authentication, profiles | User, StaffRole (REGIONAL_MANAGER, STORE_MANAGER, DEPARTMENT_LEAD, ASSOCIATE) |
| alerts | In-app alerts from operational events | Notification, AlertType (INVENTORY, SLA_BREACH, SHIFT_HANDOVER, ESCALATION) |
| reports | Store and regional performance summaries | Report, ReportType (STORE_SUMMARY, REGIONAL_ROLLUP, DEPARTMENT_PERFORMANCE) |

## Shared Components

- `com.storeops.shared.exception.AppException` — typed error base class
- `com.storeops.shared.exception.GlobalExceptionHandler` — @ControllerAdvice maps AppException to HTTP responses
- `com.storeops.shared.events.*` — Spring ApplicationEvent subclasses for cross-module communication

## Storage Pattern

All repositories use `ConcurrentHashMap<String, Entity>` with `UUID.randomUUID().toString()` as the key. No database is configured — all data is in-memory.

## API Base Path

All endpoints are prefixed with `/api`:
- `/api/activities`
- `/api/programmes`
- `/api/staff`
- `/api/alerts`
- `/api/reports`
