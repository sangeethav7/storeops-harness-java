# How to Review — StoreOps Evaluator Checklist

## Purpose

Step-by-step review process for the Evaluator agent. Follow these steps in order. Do not skip steps. A failure at any hard gate step ends the review with VERDICT: FAIL.

---

## Step 1: Run Compile Check (Hard Gate)

```bash
mvn compile
```

- Exit code 0 → continue to Step 2
- Exit code non-zero → **VERDICT: FAIL**
  - Record exact compiler error with file and line number
  - Do not proceed to further steps

---

## Step 2: Run Test Suite (Hard Gate)

```bash
mvn test
```

- Exit code 0 → continue to Step 3
- Exit code non-zero → **VERDICT: FAIL**
  - Record which test class and test method failed
  - Record the assertion failure message
  - Do not proceed to further steps

---

## Step 3: Run Checkstyle (Hard Gate)

```bash
mvn checkstyle:check
```

- Exit code 0 → continue to Step 4
- Exit code non-zero → **VERDICT: FAIL**
  - Record each Checkstyle violation with file and line number
  - Do not proceed to further steps

---

## Step 4: Check for Raw Exception Throws (Hard Gate)

Search service and controller packages:

```bash
grep -rn "throw new RuntimeException\|throw new Exception(" \
  src/main/java/com/storeops --include="*.java"
```

- Zero results → continue to Step 5
- Any result → **VERDICT: FAIL**
  - Record exact file path and line number
  - Record the raw throw statement found

---

## Step 5: Check Module Boundary Imports

Search all service classes for cross-module repository imports:

```bash
grep -rn "import com.storeops" \
  src/main/java/com/storeops --include="*Service.java"
```

Review each result:
- Import of own module's classes → OK
- Import of `com.storeops.shared.*` → OK
- Import of another module's service for read-only lookup → OK (note it)
- **Import of another module's repository** → **VERDICT: FAIL**
  - Record file, line, and the illegal import

---

## Step 6: Check Event Bus Usage

Review any cross-module side effects in the changed files:

Look for:
- `applicationEventPublisher.publishEvent(...)` → CORRECT
- Direct import and call of another module's service for write/trigger → **VERDICT: FAIL**

Check specifically:
- Any new alert or notification triggering code
- Any report generation triggering code
- Anything that crosses from activities/programmes/staff into alerts/reports

---

## Step 7: Check Layer Separation

Search service and repository classes for HTTP-specific imports:

```bash
grep -rn "ResponseEntity\|HttpServletRequest\|HttpStatus" \
  src/main/java/com/storeops --include="*Service.java" --include="*Repository.java"
```

- Zero results → PASS this check
- Any result → record file, line, and the HTTP class found (soft failure — reduce Architecture Compliance score)

---

## Step 8: Verify Acceptance Criteria

For each AC in the sprint contract:
1. Find the test that covers it — if no test exists, mark AC as FAIL
2. Verify the test asserts the business rule, not just the HTTP status
3. Check the implementation matches the AC behaviour

---

## Step 9: Score and Verdict

After completing all steps:

1. Apply hard gate results first (Steps 1–4)
   - Any hard gate failure → `VERDICT: FAIL` — skip scoring
2. Score each dimension (Steps 5–8)
3. Apply verdict rules from evaluation-criteria skill file
4. Write evaluator-feedback.md with full results
