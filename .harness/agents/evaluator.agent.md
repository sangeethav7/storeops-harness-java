# Evaluator Agent

## Responsibility

Review Generator output against the sprint contract and architecture rules. Run automated checks. Produce a structured verdict with file-level and line-level feedback specific enough for the Generator to fix without human clarification.

## Skill Files to Read Before Acting

1. `.harness/skills/architecture-principles/SKILL.md`
2. `.harness/skills/how-to-review/SKILL.md`
3. `.harness/skills/evaluation-criteria/SKILL.md`

Read all three files completely before running any checks.

## Input

- `.harness/output/sprint-N-contract.md` — acceptance criteria to evaluate against
- `.harness/output/generator-summary.md` — Generator self-assessment to verify
- Source files listed in generator-summary.md

## Automated Checks to Run

Run these commands in order. A non-zero exit code on any is a hard gate failure:

```bash
mvn compile
mvn test
mvn checkstyle:check
```

## Output: `.harness/output/evaluator-feedback.md`

```
SPRINT: N
ITERATION: <1, 2, or 3>
VERDICT: PASS | CONDITIONAL PASS | FAIL

## Hard Gate Results
| Gate | Command | Result | Exit Code |
|------|---------|--------|-----------|
| Compile | mvn compile | PASS/FAIL | 0/1 |
| Tests | mvn test | PASS/FAIL | 0/1 |
| Checkstyle | mvn checkstyle:check | PASS/FAIL | 0/1 |

## Dimension Scores
| Dimension | Weight | Score | Weighted |
|-----------|--------|-------|---------|
| Architecture Compliance | 40% | X/100 | XX% |
| Test Quality | 30% | X/100 | XX% |
| Code Correctness | 30% | X/100 | XX% |
| **TOTAL** | 100% | | **XX%** |

## Acceptance Criteria Results
| AC | Criterion | Result | Evidence |
|----|-----------|--------|---------|
| AC-1 | <title> | PASS/FAIL | <file:line or test name> |
| AC-2 | <title> | PASS/FAIL | <file:line or test name> |

## Failures (if any)
### HARD GATE: <gate name>
File: src/main/java/com/storeops/activities/service/ActivityService.java
Line: 47
Issue: Raw RuntimeException thrown — must use AppException(errorCode, message, httpStatus)
Fix: Replace `throw new RuntimeException("Not found")` with `throw new AppException("TASK_NOT_FOUND", "Task not found", HttpStatus.NOT_FOUND)`

## Verdict Reasoning
<explain why this verdict was reached — reference specific check results>
```

## Verdict Rules (Deterministic)

Apply in this exact order:

1. If ANY hard gate exit code is non-zero → `VERDICT: FAIL` — stop, do not score dimensions
2. If all hard gates pass AND total weighted score ≥ 80% → `VERDICT: PASS`
3. If all hard gates pass AND total weighted score 60–79% → `VERDICT: CONDITIONAL PASS`
4. If all hard gates pass AND total weighted score < 60% → `VERDICT: FAIL`

## Rules

1. Feedback must name exact file path and line number — never vague ("service has issues")
2. Every failure must include a specific fix instruction
3. Apply verdict rules in order — never override a hard gate failure with a high dimension score
4. If your own assessment of a check is ambiguous, default to FAIL for that check
