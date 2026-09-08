# Monitor Agent

## Responsibility

Record the outcome of each completed sprint run as a structured log entry. The Monitor provides the observability and governance audit trail — its output is the primary input for detecting which skill files need refinement over time.

## Skill Files to Read Before Acting

- `.harness/skills/app-context/SKILL.md`

## Input

- `.harness/output/evaluator-feedback.md` — verdict and check results for the completed sprint
- `.harness/output/generator-summary.md` — Generator self-assessment for the completed sprint

## Output: `.harness/reviews/sprint-N-run-log.md`

```
SPRINT: N
DATE: <ISO date>
FEATURE: <feature name from spec.md>

## Run Summary
| Field | Value |
|-------|-------|
| Verdict | PASS / CONDITIONAL PASS / FAIL |
| Iterations used | 1 / 2 / 3 |
| Escalation flag | YES / NO |
| Estimated token cost | ~<N>k tokens |

## Hard Gate Results
| Gate | Result |
|------|--------|
| mvn compile | PASS / FAIL |
| mvn test | PASS / FAIL |
| mvn checkstyle:check | PASS / FAIL |

## Dimension Scores
| Dimension | Score |
|-----------|-------|
| Architecture Compliance (40%) | XX% |
| Test Quality (30%) | XX% |
| Code Correctness (30%) | XX% |
| Total | XX% |

## Quality Trend Notes
<Note any recurring issues across sprints — e.g. "Generator consistently misses business rule assertions in tests — how-to-test skill file may need strengthening">

## Skill File Observations
<Note any skill file that failed to prevent a violation — e.g. "architecture-principles did not prevent raw RuntimeException in ActivityService.java line 47 — consider adding explicit Java example">
```

## Rules

1. Always archive to `.harness/reviews/` — never overwrite a previous run log
2. Use consistent file naming: `sprint-1-run-log.md`, `sprint-2-run-log.md`
3. Estimated token cost is an approximation — count input + output tokens across Generator and Evaluator for the sprint
4. Quality trend notes must be specific — reference the actual file and line that caused the issue
5. Run after every sprint verdict including CONDITIONAL PASS
