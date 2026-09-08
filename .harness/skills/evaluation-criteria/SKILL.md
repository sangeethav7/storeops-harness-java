# Evaluation Criteria — StoreOps

## Purpose

Define the evaluation dimensions, weights, hard gates, and verdict rules for the Evaluator. These rules are deterministic — given the same check results, the verdict is always the same.

---

## Hard Gate Conditions

Hard gates cause an immediate `VERDICT: FAIL` regardless of dimension scores. They are checked before scoring begins.

| Gate | Check | Why it cannot be a soft check |
|------|-------|-------------------------------|
| Compile | `mvn compile` exit code = 0 | Non-compiling code cannot be reviewed for correctness |
| Tests | `mvn test` exit code = 0 | Failing tests mean the contract is not met |
| Checkstyle | `mvn checkstyle:check` exit code = 0 | Style violations indicate systemic quality issues |
| Raw exceptions | Zero `throw new RuntimeException` in services/controllers | Raw exceptions bypass GlobalExceptionHandler — clients receive 500 instead of typed errors |

If any hard gate fails → `VERDICT: FAIL`. Do not score dimensions. Record which gate failed and why.

---

## Evaluation Dimensions

All dimensions sum to 100%. Score each dimension 0–100 independently.

### Dimension 1: Architecture Compliance (Weight: 40%)

| Check | Points | How to assess |
|-------|--------|---------------|
| No cross-module repository imports | 30 | grep imports in service classes — zero violations = 30 pts |
| ApplicationEventPublisher used for cross-module events | 30 | Review cross-module triggers — event bus used = 30 pts |
| Layer separation maintained | 20 | No HTTP classes in services/repositories = 20 pts |
| Read-only reports module | 20 | No write calls from reports to other modules = 20 pts |

### Dimension 2: Test Quality (Weight: 30%)

| Check | Points | How to assess |
|-------|--------|---------------|
| Tests assert business rules (not just status codes) | 40 | Review test assertions — body/event checks present = 40 pts |
| All ACs covered by tests | 30 | One test per AC minimum = 30 pts |
| 80% line coverage on new classes | 20 | `mvn test jacoco:report` — coverage ≥ 80% = 20 pts |
| Test naming convention followed | 10 | methodName_scenario_expectedResult = 10 pts |

### Dimension 3: Code Correctness (Weight: 30%)

| Check | Points | How to assess |
|-------|--------|---------------|
| AppException used for all error cases | 40 | No raw throws in services/controllers = 40 pts |
| DTOs used in controllers (no entity exposure) | 30 | Controller methods use Request/Response DTOs = 30 pts |
| Constructor injection only | 20 | No @Autowired field injection = 20 pts |
| No unused imports | 10 | Checkstyle passes = 10 pts (linked to hard gate) |

---

## Verdict Rules

Apply in this exact order — do not skip steps:

```
1. IF any hard gate failed:
      VERDICT: FAIL
      (stop — do not calculate dimension scores)

2. Calculate weighted total:
      total = (dim1_score * 0.40) + (dim2_score * 0.30) + (dim3_score * 0.30)

3. IF total >= 80:
      VERDICT: PASS

4. IF total >= 60 AND total < 80:
      VERDICT: CONDITIONAL PASS
      (list specific items that reduced score below 80)

5. IF total < 60:
      VERDICT: FAIL
```

---

## Worked Example

| Check | Result | Score |
|-------|--------|-------|
| mvn compile | Exit 0 | Hard gate PASS |
| mvn test | Exit 0 | Hard gate PASS |
| mvn checkstyle:check | Exit 0 | Hard gate PASS |
| Raw exceptions | 0 found | Hard gate PASS |
| Cross-module repo imports | 0 found | 30/30 |
| Event bus used | Yes | 30/30 |
| Layer separation | HTTP in service line 47 | 0/20 |
| Reports read-only | Yes | 20/20 |
| **Dim 1 score** | | **80/100** |
| Tests assert business rules | Partially | 20/40 |
| All ACs covered | Yes | 30/30 |
| 80% coverage | 75% | 0/20 |
| Test naming | Yes | 10/10 |
| **Dim 2 score** | | **60/100** |
| AppException used | Yes | 40/40 |
| DTOs in controllers | Yes | 30/30 |
| Constructor injection | Yes | 20/20 |
| No unused imports | Yes | 10/10 |
| **Dim 3 score** | | **100/100** |

**Weighted total:** (80 × 0.40) + (60 × 0.30) + (100 × 0.30) = 32 + 18 + 30 = **80%**

**VERDICT: PASS** (exactly 80%)

---

## Handling Evaluator Ambiguity

If the Evaluator's own assessment of a check is ambiguous (e.g. unclear whether an import violates module boundary):

- Default to **FAIL** for that check
- Record the ambiguity in evaluator-feedback.md
- A human can override on review — do not guess PASS when uncertain
