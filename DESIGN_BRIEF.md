# Harness Design Brief

## Overview

This document captures the architectural intent behind the StoreOps Claude Code Development Harness — a governed, multi-agent orchestration layer built on top of the StoreOps Java/Spring Boot REST API. The harness wraps AI code generation in a structured pipeline that enforces architecture standards, provides feedforward context to each agent, and converts non-deterministic LLM output into deterministic accept/reject decisions.

---

## Section A — Intent Decomposition

### How the Feature Was Decomposed

The chosen feature — **shift handover bulk update** (`PATCH /api/activities/bulk-status`) — was decomposed by the Planner agent into two sprint contracts:

**Sprint 1 — API Layer**
Deliver the controller endpoint, request/response DTOs, and service method signature. Sprint boundary drawn here because the HTTP contract must be stable before the business logic is built — this mirrors the Route → Service → Repository layer order enforced by the architecture rules.

**Sprint 2 — Business Logic and Event Integration**
Deliver the service implementation (bulk status validation, partial failure handling), the ShiftHandoverEvent, and the AlertsModule listener. Sprint boundary drawn here because cross-module event integration is a distinct concern from the API contract — testing it separately ensures the event bus rule is enforced in isolation.

### Why Sprint Boundaries Were Drawn Here

Splitting at the service boundary means each sprint produces independently testable output. The Evaluator can verify the controller layer compiles and tests pass before the Generator implements the business logic — catching module boundary violations early rather than at the end of a single large sprint.

### Acceptance Criteria Structure

Each AC uses GIVEN/WHEN/THEN to make it testable rather than subjective:

**Example — Sprint 2, AC-3 (Partial Failure Handling):**
```
GIVEN a bulk-status request containing one valid task ID and one invalid task ID
WHEN PATCH /api/activities/bulk-status is called
THEN the response contains the valid ID in the "succeeded" list
AND the invalid ID in the "failed" list with reason "Activity not found"
AND HTTP status is 207 Multi-Status
```

This criterion is testable because it specifies exact response fields, exact HTTP status, and exact failure reason — the Evaluator can verify it from the test assertion without human interpretation.

---

## Section B — Governance Framework

### Skill File Strategy

| Skill File | Governs | Shared / Agent-specific |
|---|---|---|
| app-context | Domain knowledge, module structure, package names, tech stack | Shared — all agents need project context |
| architecture-principles | 4 hard rules: module boundary, event bus, error contract, layer separation | Shared — Generator must follow, Evaluator must enforce |
| coding-conventions | Java 21 naming, Spring Boot annotations, constructor injection, Checkstyle | Generator-specific |
| api-integration | REST patterns, ResponseEntity, cross-module event publishing | Generator-specific |
| how-to-test | JUnit 5 + MockMvc patterns, business rule assertions, coverage threshold | Generator-specific |
| how-to-review | 9-step review checklist with exact grep commands | Evaluator-specific |
| evaluation-criteria | Dimensions, weights, hard gate conditions, verdict rules | Evaluator-specific |

**Why app-context and architecture-principles are shared:** Every agent — Planner, Generator, Evaluator, Monitor — needs to know what StoreOps is and what its non-negotiable rules are. Sharing these prevents drift between what the Generator builds and what the Evaluator checks.

### How `.harness/reviews/` Functions as an Audit Trail

After every sprint verdict, the Monitor archives:
- `sprint-N-run-log.md` — verdict, iterations used, escalation flag, estimated token cost, quality trend notes
- `sprint-N-evaluator-feedback.md` — file-level and line-level feedback with specific violations
- `sprint-N-generator-summary.md` — AC self-check table and files changed

This archive is permanent and committed to the repository. It allows any team member to trace every AI-generated contribution back to its acceptance decision. A recurring quality issue — for example, raw RuntimeException throws appearing in multiple sprints — would be visible as a pattern across run logs and would trigger a skill file update.

### One Skill File Rule Traceable to a StoreOps Architecture Decision

**Rule (from architecture-principles/SKILL.md):**
> `ApplicationEventPublisher.publishEvent()` is the ONLY permitted cross-module trigger. Direct import of `AlertsService` or `ReportService` from another module's package is a HARD GATE FAILURE.

**What breaks without it:** Without this rule, the Generator imported `AlertsService` directly into `ActivityService` in an early iteration — creating a compile-time dependency between the activities and alerts modules. If `AlertsService` changes its method signature, `ActivityService` breaks. The event bus decouples these modules so each can evolve independently.

---

## Section C — Non-Determinism Strategy

### Evaluation Dimensions and Weights

| Dimension | Weight | Why chosen for StoreOps |
|---|---|---|
| Architecture Compliance | 40% | The four failure modes identified in the client context are all architecture violations — this dimension has the highest weight because preventing them is the harness's primary purpose |
| Test Quality | 30% | Tests that assert only HTTP status codes (failure mode 3) passed human review in the prior experiment — this dimension specifically catches that pattern |
| Code Correctness | 30% | Raw RuntimeException throws and missing DTOs are correctness issues that Checkstyle and LLM assessment can catch deterministically |

### Hard Gate Conditions and Why They Cannot Be Soft Checks

| Hard Gate | Failure Mode Prevented | Why Not Soft |
|---|---|---|
| `mvn compile` exit 0 | Non-compiling code reaching main | A score cannot be assigned to code that doesn't compile — all other checks are meaningless |
| `mvn test` exit 0 | Broken tests passing the loop | A CONDITIONAL PASS on failing tests would allow broken code to advance to the next sprint |
| `mvn checkstyle:check` exit 0 | Style violations accumulating | Checkstyle is fully deterministic — a violation is a violation, not a matter of degree |
| Zero raw RuntimeException throws | Bypass of GlobalExceptionHandler | A single raw throw produces a 500 response with no errorCode — this is a client-visible defect, not a style issue |

### From Variable Output to Deterministic Verdict — Worked Example

**Generator output:** `ActivityService.java` line 47 contains `throw new RuntimeException("Task not found")`

**Evaluator check:**
```bash
grep -rn "throw new RuntimeException" src/main/java/com/storeops --include="*.java"
```
Result: 1 match found.

**Verdict rule applied:**
1. Hard gate 4 (zero raw throws) → FAILED
2. Rule: any hard gate failure → `VERDICT: FAIL` (stop, do not score dimensions)

**Output:** `VERDICT: FAIL` — same result every time given the same grep output. No LLM judgement involved in the hard gate decision.

### Escalation Path

**Trigger:** 3 consecutive FAIL verdicts on the same sprint.

**Escalation output** written to `.harness/output/escalation.md`:
```
ESCALATION NOTICE
Sprint: N
Iterations used: 3
Blocking issue: <hard gate failure reason from evaluator-feedback.md>
Action required: Developer must resolve the blocking issue manually before resuming.
```

**Who receives it:** The developer who invoked the Planner. Claude Code stops the loop and surfaces the escalation notice. The developer resolves the blocking issue manually and re-invokes the Generator with explicit correction instructions.

---

## Section D — Architectural Decisions

### Decision 1: Hardcode Port 5000 in application.properties Rather Than Environment Variable

**Decision:** Set `server.port=5000` in `application.properties` rather than relying on a `SERVER_PORT` environment variable in Elastic Beanstalk.

**Alternatives considered:**
- Environment variable `SERVER_PORT=5000` in EB configuration
- Procfile with `--server.port=5000` flag

**Rationale:** The environment variable approach failed in practice — Elastic Beanstalk applied the variable after the JVM started, meaning the app bound to 8080 before the variable was read. Hardcoding in application.properties ensures the correct port is used regardless of deployment target.

**Assumption this depends on:** All deployment targets (EB, local) accept port 5000. If a future deployment target requires a different port, this must be externalised to an environment variable.

---

### Decision 2: Four Agent Files Rather Than a Single Monolithic Prompt

**Decision:** Separate the harness into four distinct agent files (Planner, Generator, Evaluator, Monitor) each with its own skill file scope.

**Alternatives considered:**
- Single CLAUDE.md with all instructions inline
- Two agents only (Planner + Generator/Evaluator combined)

**Rationale:** A monolithic prompt grows with every new skill file and eventually degrades context quality — later instructions are less attended to than earlier ones. Separate agent files with explicit skill file lists allow each agent to operate in a scoped context window, preventing the Generator's coding conventions from polluting the Evaluator's review criteria.

**Assumption this depends on:** Claude Code can maintain distinct context per agent invocation. If context bleeds between agent calls, the separation provides less benefit.

---

### Decision 3: Automated Hard Gates Before LLM-Assessed Dimensions

**Decision:** Run `mvn compile`, `mvn test`, and `mvn checkstyle:check` as hard gates before any LLM-assessed dimension scoring.

**Alternatives considered:**
- LLM-only evaluation (no automated tool checks)
- Automated checks as one dimension among equals (weighted, not gates)

**Rationale:** LLM assessors exhibit leniency drift — they may rationalise a test failure as "acceptable given the complexity." Automated tool checks are fully deterministic and immune to this. By running them first and treating failures as immediate stops, the harness guarantees that no LLM leniency can override a compile error or test failure.

**Assumption this depends on:** The automated tools (`mvn`, Checkstyle) are available in the environment where the Evaluator runs. If the harness is run in a sandboxed environment without Maven, the hard gates cannot execute.
