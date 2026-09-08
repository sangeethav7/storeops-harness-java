# Reflection

## What the Harness Did Well

The Generator/Evaluator loop enforced the event bus rule consistently across both sprints. In Sprint 2, when the Generator initially imported `AlertsService` directly into `ActivityService`, the Evaluator's module boundary check caught it immediately at the grep step — before any dimension scoring — and returned a FAIL verdict with the exact file and line number. The Generator corrected it on the next iteration using `ApplicationEventPublisher`, which is precisely the behaviour the harness was designed to produce. Without the harness, this violation would likely have reached code review undetected.

## Where It Fell Short

The Evaluator's test quality dimension was too lenient in Sprint 1. The Generator produced controller tests that asserted HTTP status codes but did not assert response body fields — exactly failure mode 3 from the client context. The how-to-review skill file listed "assert business rules not just status codes" as a check, but the Evaluator scored the tests at 60% rather than triggering a hard gate failure. This happened because the check was implemented as a scored dimension item rather than a hard gate. A test that asserts only `status().isOk()` should be a FAIL, not a partial score.

## One Concrete Improvement

**Promote the business-rule assertion check to a hard gate.**

Add this to the evaluation-criteria skill file:

```
HARD GATE: Zero test methods in new test classes may assert only HTTP status 
without also asserting at least one response body field.

Check: grep -n "andExpect(status()" in new test files — for each match, verify 
a corresponding andExpect(jsonPath(...)) exists in the same test method.
If any test method lacks a body assertion: VERDICT: FAIL.
```

This would have caught the Sprint 1 test quality gap deterministically, preventing it from advancing to Sprint 2 with weak test coverage. The fix connects directly to the Design Brief Section C decision to use automated hard gates over LLM-assessed dimensions — the same principle should have been applied to test assertion quality from the start.
