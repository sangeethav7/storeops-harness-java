# Generator Agent

## Responsibility

Implement the current sprint contract — produce working Java/Spring Boot code and tests that satisfy all acceptance criteria. Write a self-assessment summary after implementation.

## Skill Files to Read Before Acting

1. `.harness/skills/app-context/SKILL.md`
2. `.harness/skills/architecture-principles/SKILL.md`
3. `.harness/skills/coding-conventions/SKILL.md`
4. `.harness/skills/api-integration/SKILL.md`
5. `.harness/skills/how-to-test/SKILL.md`

Read all five files completely before writing any code.

## Input

- `.harness/output/sprint-N-contract.md` — current sprint to implement
- `.harness/output/evaluator-feedback.md` — only present on retry iterations; apply all feedback before regenerating

## Output

### 1. Code in `src/`

Produce or modify files listed in the sprint contract:
- `src/main/java/com/storeops/<module>/controller/`
- `src/main/java/com/storeops/<module>/service/`
- `src/main/java/com/storeops/<module>/repository/`
- `src/test/java/com/storeops/<module>/`

### 2. `.harness/output/generator-summary.md`

```
SPRINT: N
ITERATION: <1, 2, or 3>
GENERATOR SUMMARY

## Acceptance Criteria Self-Check
| AC | Criterion | Status | Notes |
|----|-----------|--------|-------|
| AC-1 | <title> | PASS / FAIL | <reason if FAIL> |
| AC-2 | <title> | PASS / FAIL | <reason if FAIL> |

## Files Changed
| File | Action | Description |
|------|--------|-------------|
| src/main/java/com/storeops/activities/controller/ActivityController.java | MODIFIED | Added PATCH /bulk-status endpoint |

## Architecture Compliance Self-Check
- [ ] No raw RuntimeException throws in services or controllers
- [ ] No cross-module repository imports
- [ ] ApplicationEventPublisher used for cross-module events
- [ ] Controller → Service → Repository layer order maintained
- [ ] AppException used for all error cases

## Known Gaps
<list anything not implemented or any AC not fully satisfied>
```

## Rules

1. Never import another module's repository — only import its service if a read-only lookup is needed
2. All exceptions in service and controller layers must use AppException — never raw RuntimeException
3. Use ApplicationEventPublisher for any event that crosses module boundaries
4. Constructor injection only — never @Autowired on fields
5. Tests must assert business rules, not just HTTP status codes
6. On retry iterations, address every item in evaluator-feedback.md before regenerating
7. Do not modify files outside the sprint contract scope
