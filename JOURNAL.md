# Architecture Journal

## Day 1 — Generating the Baseline and First Observations

Generated the StoreOps scaffold using Claude Code's bootstrap prompt. First observation: Claude Code correctly structured all five modules with Controller → Service → Repository layers but used `throw new RuntimeException` in two service methods rather than `AppException`. This was not prompted — it was Claude Code's default behaviour when no error contract was specified.

**Decision recorded:** The error contract rule must be in both the Generator skill file AND the architecture-principles skill file — not just one. The Generator needs it as a coding instruction; the Evaluator needs it as a check criterion. A rule in only one place creates a gap.

**Insight:** The bootstrap generation experience directly informed the hard gate design. Every violation Claude Code produced without guardrails became a hard gate in the Evaluator.

---

## Day 2–3 — Designing the Planner Agent

Initial design had the Planner produce a single large spec with all sprints combined. Rejected this because the Generator's context window would be loaded with requirements for sprints it wasn't implementing yet — wasting tokens and increasing the risk of the Generator implementing future sprint features prematurely.

**Decision:** Planner produces one `spec.md` (approved once) and individual `sprint-N-contract.md` files. Generator reads only the current sprint contract — not the full spec.

**Trade-off noted:** This means the Generator doesn't see future sprint requirements. Risk: it may make implementation choices in Sprint 1 that conflict with Sprint 2 requirements. Mitigation: the Planner's sprint contracts include an "Architecture Constraints" section that flags any cross-sprint dependencies.

---

## Day 4–6 — Writing Skill Files

Realised that generic skill files are worse than no skill files — they consume context tokens without improving output quality. A skill file that says "follow REST best practices" tells the Generator nothing it doesn't already know.

**Decision:** Every skill file rule must name a specific StoreOps class, package, or pattern. "Use AppException(errorCode, message, httpStatus)" is actionable. "Handle errors properly" is not.

**Observation on skill file length:** The coding-conventions file initially ran 6 pages with full Java examples. Trimmed to 4 pages by removing examples that duplicated what Spring Boot's own conventions already enforce. Insight: skill files should encode project-specific decisions, not framework documentation.

---

## Day 7–9 — Building the Evaluator

The hardest design decision was where to draw the line between hard gates and scored dimensions. Initial design had 6 hard gates — one per architecture rule. This was too strict: a single minor Checkstyle violation would FAIL the entire sprint, even if all business logic was correct.

**Decision:** Reduced to 4 hard gates (compile, test, Checkstyle, raw exceptions). Module boundary and event bus violations were initially hard gates but moved to the Architecture Compliance dimension (scored, not gated) because:
1. They are LLM-assessed, not tool-checked — introducing LLM judgement into a hard gate creates verdict variability
2. A module boundary violation that scores 0/30 on that check will still FAIL on total score if it's severe enough

**Lesson:** Hard gates must be deterministic. If an automated tool cannot confirm the violation with a non-zero exit code, it belongs in a scored dimension — not a hard gate.

---

## Day 10–11 — Full Harness Run

Sprint 1 completed in 1 iteration — PASS. Sprint 2 required 2 iterations:
- Iteration 1: FAIL — Generator imported AlertsService directly (module boundary violation, scored 0/30 on Architecture Compliance, total dropped below 60%)
- Iteration 2: PASS — Generator used ApplicationEventPublisher, all hard gates passed, total score 82%

**Observation:** The Evaluator's file-and-line feedback in iteration 1 was specific enough that the Generator fixed the exact violation without any additional human input. This is the correct behaviour — the harness ran autonomously without developer intervention between iterations.

**Gap identified:** The test quality check did not catch status-code-only assertions in Sprint 1 controller tests. Documented in REFLECTION.md as the one concrete improvement.

---

## Day 12–13 — Deployment

AWS Elastic Beanstalk deployment required two non-obvious fixes:
1. The `spring-boot-maven-plugin` was missing from `pom.xml` — Maven produced a plain JAR without the executable manifest. Fixed by adding the plugin.
2. `application.properties` was missing — port 5000 was never applied. Fixed by creating the file.

**Architectural observation:** Deployment configuration gaps are not caught by the harness — the Evaluator runs `mvn compile` and `mvn test` locally, but does not test the packaged JAR's deployability. A future improvement would add a deployment smoke test as a Monitor check: build the JAR, start it in a subprocess, hit one endpoint, confirm 200 OK, then stop it. This would catch the manifest and port issues before deployment.

---

## Key Insight — Skill Files Are Governance Documents, Not Prompts

The biggest mindset shift during this build: skill files are not instructions to the AI — they are governance documents that encode architectural decisions. A good skill file answers: "Why does this rule exist, and what breaks if it's removed?" A bad skill file answers: "Here is how to write Java code."

The architecture-principles skill file is strong because every rule names the failure mode it prevents. The coding-conventions skill file is weaker because some rules (constructor injection, DTO usage) are Spring Boot best practices rather than StoreOps-specific decisions. In a production harness, those generic rules would be removed and replaced with the actual project-specific choices the team debated and agreed on.
